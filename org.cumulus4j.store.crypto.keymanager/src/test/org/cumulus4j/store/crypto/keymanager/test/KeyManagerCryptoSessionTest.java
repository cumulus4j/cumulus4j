/*
 * Cumulus4j - Securing your data in the cloud - http://cumulus4j.org
 * Copyright (C) 2011 NightLabs Consulting GmbH
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.cumulus4j.store.crypto.keymanager.test;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.security.SecureRandom;
import java.util.UUID;

import javax.jdo.PersistenceManager;

import org.cumulus4j.store.EncryptionCoordinateSetManager;
import org.cumulus4j.store.PersistenceManagerConnection;
import org.cumulus4j.store.crypto.Ciphertext;
import org.cumulus4j.store.crypto.CryptoContext;
import org.cumulus4j.store.crypto.Plaintext;
import org.cumulus4j.store.crypto.keymanager.KeyManagerCryptoManager;
import org.cumulus4j.store.crypto.keymanager.KeyManagerCryptoSession;
import org.cumulus4j.store.crypto.keymanager.messagebroker.MessageBrokerRegistry;
import org.datanucleus.store.ExecutionContext;
import org.junit.Assert;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

public class KeyManagerCryptoSessionTest
{
	private static SecureRandom random = new SecureRandom();

	@BeforeClass
	public static void beforeClass()
	{
		MockMessageBroker.setMockSharedInstance();
		if (!(MessageBrokerRegistry.sharedInstance().getActiveMessageBroker() instanceof MockMessageBroker))
			Assert.fail("Setting MockMessageBroker failed!");
	}

	@Before
	public void before()
	{
		KeyManagerCryptoManager cryptoManager = new KeyManagerCryptoManager();
//		session = new KeyManagerCryptoSession();
		session = (KeyManagerCryptoSession) cryptoManager.getCryptoSession(UUID.randomUUID().toString());
//		session.setCryptoManager(cryptoManager);
//		session.setCryptoSessionID(UUID.randomUUID().toString());

		ExecutionContext executionContext = (ExecutionContext) Proxy.newProxyInstance(
				this.getClass().getClassLoader(),
				new Class[] { ExecutionContext.class },
				new InvocationHandler() {
					@Override
					public Object invoke(Object proxy, Method method, Object[] args)
					throws Throwable
					{
						return null;
					}
				}
		);

		PersistenceManager pm = (PersistenceManager) Proxy.newProxyInstance(
				this.getClass().getClassLoader(),
				new Class[] { PersistenceManager.class },
				new InvocationHandler() {
					@Override
					public Object invoke(Object proxy, Method method, Object[] args)
					throws Throwable
					{
						return null;
					}
				}
		);

		PersistenceManagerConnection persistenceManagerConnection = new PersistenceManagerConnection(pm, null);
		cryptoContext = new CryptoContext(
				new EncryptionCoordinateSetManager(),
				executionContext, persistenceManagerConnection
		);
	}

	private CryptoContext cryptoContext;
	private KeyManagerCryptoSession session;

	@Test
	public void encryptDecryptWithHelpfulDebugData()
	{
		Plaintext plaintext = new Plaintext();
		{
			byte[] data = new byte[64];
			for (int i = 0; i < data.length; ++i)
				data[i] = (byte)( data.length - i );

			data[data.length - 1] = (byte)255;
			data[data.length - 2] = (byte)255;
			data[data.length - 3] = (byte)255;

			plaintext.setData(data);
		}

		Ciphertext ciphertext = session.encrypt(cryptoContext, plaintext);
		Plaintext decrypted = session.decrypt(cryptoContext, ciphertext);

		Assert.assertArrayEquals(plaintext.getData(), decrypted.getData());
	}

	@Test
	public void encryptDecryptWithRandomData()
	{
		Plaintext plaintext = new Plaintext();
		{
			byte[] data = new byte[random.nextInt(1024 * 1024)];
			random.nextBytes(data);
			plaintext.setData(data);
		}

		Ciphertext ciphertext = session.encrypt(cryptoContext, plaintext);
		// NOT clearing cache in order to test the cached scenario.
		Ciphertext ciphertext1 = session.encrypt(cryptoContext, plaintext);

		// Clear cache in order to test more code (i.e. ask the MockMessageBroker with a GetKeyRequest).
		((KeyManagerCryptoManager)session.getCryptoManager()).getCipherCache().clear();

		Ciphertext ciphertext2 = session.encrypt(cryptoContext, plaintext);

		int c = countDifferentBits(ciphertext.getData(), ciphertext1.getData());
		Assert.assertTrue("Not enough bits different between ciphertext and ciphertext1! Only " + c + " bits differ!", c > 1024);

		c = countDifferentBits(ciphertext.getData(), ciphertext2.getData());
		Assert.assertTrue("Not enough bits different between ciphertext and ciphertext2! Only " + c + " bits differ!", c > 1024);

		c = countDifferentBits(ciphertext1.getData(), ciphertext2.getData());
		Assert.assertTrue("Not enough bits different between ciphertext1 and ciphertext2! Only " + c + " bits differ!", c > 1024);

		// Clear cache in order to test more code (i.e. ask the MockMessageBroker with a GetKeyRequest).
		((KeyManagerCryptoManager)session.getCryptoManager()).getCipherCache().clear();

		Plaintext decrypted = session.decrypt(cryptoContext, ciphertext);

		Assert.assertArrayEquals(plaintext.getData(), decrypted.getData());

		// NOT clearing cache in order to test the cached scenario.
		Plaintext decrypted1 = session.decrypt(cryptoContext, ciphertext1);

		// Clear cache in order to test more code (i.e. ask the MockMessageBroker with a GetKeyRequest).
		((KeyManagerCryptoManager)session.getCryptoManager()).getCipherCache().clear();
		Plaintext decrypted2 = session.decrypt(cryptoContext, ciphertext2);

		c = countDifferentBits(decrypted.getData(), decrypted1.getData());
		Assert.assertEquals("decrypted does not match decrypted1! " + c + " bits differ!", 0, c);

		c = countDifferentBits(decrypted.getData(), decrypted2.getData());
		Assert.assertEquals("decrypted does not match decrypted2! " + c + " bits differ!", 0, c);
	}

	private int countDifferentBits(byte[] b1, byte[] b2)
	{
		int length = b1.length < b2.length ? b1.length : b2.length;

		int result = 0;

		for (int i = 0; i < length; ++i) {
			for (int bit = 0; bit < 8; ++bit) {
				int bit1 = (b1[i] >>> bit) & 1;
				int bit2 = (b2[i] >>> bit) & 1;

				if (bit1 != bit2)
					++result;
			}
		}

		return result;
	}
}
