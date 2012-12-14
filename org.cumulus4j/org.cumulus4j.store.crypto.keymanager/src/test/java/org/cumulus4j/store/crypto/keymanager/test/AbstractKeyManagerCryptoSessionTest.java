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

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.security.SecureRandom;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import javax.jdo.JDOHelper;
import javax.jdo.PersistenceManager;
import javax.jdo.PersistenceManagerFactory;

import org.cumulus4j.keymanager.back.shared.IdentifierUtil;
import org.cumulus4j.store.EncryptionCoordinateSetManager;
import org.cumulus4j.store.KeyStoreRefManager;
import org.cumulus4j.store.PersistenceManagerConnection;
import org.cumulus4j.store.crypto.CryptoContext;
import org.cumulus4j.store.crypto.CryptoManager;
import org.cumulus4j.store.crypto.CryptoManagerRegistry;
import org.cumulus4j.store.crypto.keymanager.KeyManagerCryptoManager;
import org.cumulus4j.store.crypto.keymanager.KeyManagerCryptoSession;
import org.cumulus4j.store.crypto.keymanager.messagebroker.MessageBrokerRegistry;
import org.datanucleus.NucleusContext;
import org.datanucleus.api.jdo.JDOPersistenceManagerFactory;
import org.datanucleus.store.ExecutionContext;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.BeforeClass;
import org.nightlabs.util.IOUtil;

public abstract class AbstractKeyManagerCryptoSessionTest
{
	protected static SecureRandom random = new SecureRandom();

	protected static byte[] getBytesFromResource(String resourceName) throws IOException
	{
		InputStream in = AbstractKeyManagerCryptoSessionTest.class.getResourceAsStream(resourceName);
		ByteArrayOutputStream out = new ByteArrayOutputStream();
		IOUtil.transferStreamData(in, out);
		in.close();
		out.close();
		return out.toByteArray();
	}

	@BeforeClass
	public static void beforeClass()
	{
		MockMessageBroker.setMockSharedInstance();
		if (!(MessageBrokerRegistry.sharedInstance().getActiveMessageBroker() instanceof MockMessageBroker))
			Assert.fail("Setting MockMessageBroker failed!");
	}

	private PersistenceManagerFactory pmf;

	private File tmpDir;
	{
		File f = new File(System.getProperty("java.io.tmpdir"));
		tmpDir = new File(f, "cumulus4j-test." + IdentifierUtil.createRandomID(8));
	}

	protected abstract String getEncryptionAlgorithm();

	protected abstract String getMACAlgorithm();

	@Before
	public void before()
	{
		tmpDir.mkdir();

		Map<String, Object> pmfProps = new HashMap<String, Object>();
		pmfProps.put("javax.jdo.PersistenceManagerFactoryClass", "org.datanucleus.api.jdo.JDOPersistenceManagerFactory");
		pmfProps.put("javax.jdo.option.ConnectionDriverName", "org.apache.derby.jdbc.EmbeddedDriver");
		pmfProps.put("javax.jdo.option.ConnectionURL", "jdbc:derby:" + tmpDir.getAbsolutePath() + "/derby/cumulus4j;create=true");
		pmfProps.put("javax.jdo.option.ConnectionUserName", "root");
		pmfProps.put("datanucleus.autoCreateTables", "true");

		if (getEncryptionAlgorithm() != null)
			pmfProps.put(CryptoManager.PROPERTY_ENCRYPTION_ALGORITHM, getEncryptionAlgorithm());

		if (getMACAlgorithm() != null)
			pmfProps.put(CryptoManager.PROPERTY_MAC_ALGORITHM, getMACAlgorithm());

		pmf = JDOHelper.getPersistenceManagerFactory(pmfProps);

		JDOPersistenceManagerFactory dnpmf = (JDOPersistenceManagerFactory) pmf;
		NucleusContext nucleusContext = dnpmf.getNucleusContext();

		CryptoManagerRegistry cryptoManagerRegistry = CryptoManagerRegistry.sharedInstance(nucleusContext);
		KeyManagerCryptoManager cryptoManager = new KeyManagerCryptoManager();
		cryptoManager.setCryptoManagerRegistry(cryptoManagerRegistry);
		cryptoSession = (KeyManagerCryptoSession) cryptoManager.getCryptoSession(UUID.randomUUID().toString());
//		cryptoSession.setCryptoManager(cryptoManager);
//		cryptoSession.setCryptoSessionID(UUID.randomUUID().toString());

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

		PersistenceManager pm = pmf.getPersistenceManager();

		PersistenceManagerConnection persistenceManagerConnection = new PersistenceManagerConnection(pm, null);
		cryptoContext = new CryptoContext(
				new EncryptionCoordinateSetManager(),
				new KeyStoreRefManager(),
				executionContext, persistenceManagerConnection
		);
	}

	@After
	public void after()
	{
		if (pmf != null)
			pmf.close();
		pmf = null;

		try {
			DriverManager.getConnection("jdbc:derby:;shutdown=true");
		} catch (SQLException x) {
			// ignore, because this is to be expected according to http://db.apache.org/derby/docs/dev/devguide/tdevdvlp40464.html
			doNothing(); // Remove warning from PMD report: http://cumulus4j.org/pmd.html
		}

		if (tmpDir != null)
			IOUtil.deleteDirectoryRecursively(tmpDir);
		tmpDir = null;
	}

	private static final void doNothing() { }

	protected CryptoContext cryptoContext;
	protected KeyManagerCryptoSession cryptoSession;

	protected int countDifferentBits(byte[] b1, byte[] b2)
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
