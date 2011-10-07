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
package org.cumulus4j.keystore.test;

import java.io.File;

import junit.framework.Assert;

import org.cumulus4j.keystore.KeyStore;
import org.cumulus4j.keystore.prop.Long2LongSortedMapProperty;
import org.cumulus4j.keystore.prop.LongProperty;
import org.cumulus4j.keystore.prop.StringProperty;
import org.cumulus4j.keystore.test.resource.ResourceHelper;
import org.junit.Test;
import org.nightlabs.util.IOUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class KeyStoreCompatibilityTest
{
	private static final Logger logger = LoggerFactory.getLogger(KeyStoreCompatibilityTest.class);

	protected static final String USER = KeyStoreKeyTest.USER;
	protected static final char[] PASSWORD = KeyStoreKeyTest.PASSWORD;

	private String[] ALGORITHMS = {
			"default",
			"AES/CFB/NoPadding:128:HMAC/SHA1",
			"AES/CBC/PKCS5Padding:128:HMAC/SHA1",
			"AES/CFB/NoPadding:256:HMAC/SHA1",
			"AES/CBC/PKCS5Padding:256:HMAC/SHA1",
			"Twofish/CFB/NoPadding:128:HMAC/SHA1",
			"Twofish/CBC/PKCS5Padding:128:HMAC/SHA1",
			"Twofish/CFB/NoPadding:256:HMAC/SHA1",
			"Twofish/CBC/PKCS5Padding:256:HMAC/SHA1"
	};

	@Test
	public void createNewReferenceKeyStores()
	throws Exception
	{
		File newReferenceKeyStoreDir = IOUtil.createUniqueIncrementalFolder(IOUtil.getTempDir(), "new-reference-key-store.");

		for (String algorithm : ALGORITHMS) {
			String algoFileNameInfix = algorithm.replace('/', '-').replace(':', '.');

			if (!"default".equals(algorithm)) {
				String[] algoParts = algorithm.split(":");
				System.setProperty(KeyStore.SYSTEM_PROPERTY_ENCRYPTION_ALGORITHM, algoParts[0]);
				System.setProperty(KeyStore.SYSTEM_PROPERTY_KEY_SIZE, algoParts[1]);
				System.setProperty(KeyStore.SYSTEM_PROPERTY_MAC_ALGORITHM, algoParts[2]);
			}
			try {
				File keyStoreFile = new File(newReferenceKeyStoreDir, "reference." + algoFileNameInfix + ".keystore");
				KeyStore keyStore = new KeyStore(keyStoreFile);

				keyStore.createUser(null, null, USER, PASSWORD);
				keyStore.createUser(USER, PASSWORD, "eddie", "da-pass".toCharArray());
				keyStore.createUser(USER, PASSWORD, "berny", "mai sikret".toCharArray());

				// We populate the key store with all things that are supported.
				// This should be kept complete when adding new Property implementations!
				keyStore.generateKey(USER, PASSWORD);
				keyStore.generateKey(USER, PASSWORD);
				keyStore.generateKeys(USER, PASSWORD, 1000);

				{
					LongProperty p = keyStore.getProperty(USER, PASSWORD, LongProperty.class, "long1");
					p.setValue(12345L);
					keyStore.setProperty(USER, PASSWORD, p);
				}

				{
					LongProperty p = keyStore.getProperty(USER, PASSWORD, LongProperty.class, "long2");
					p.setValue(-5557347874L);
					keyStore.setProperty(USER, PASSWORD, p);
				}

				{
					StringProperty p = keyStore.getProperty(USER, PASSWORD, StringProperty.class, "string1");
					p.setValue(
							"Freude, schöner Götterfunken,\n" +
							"Tochter aus Elysium,\n" +
							"Wir betreten feuertrunken,\n" +
							"Himmlische, dein Heiligthum.\n"
					);
					keyStore.setProperty(USER, PASSWORD, p);
				}

				{
					StringProperty p = keyStore.getProperty(USER, PASSWORD, StringProperty.class, "string2");
					p.setValue(
							"Freude, schöner Götterfunken,\n" +
							"Tochter aus Elysium,\n" +
							"Wir betreten feuertrunken,\n" +
							"Himmlische, dein Heiligthum.\n"
					);
					keyStore.setProperty(USER, PASSWORD, p);
				}

				{
					String propertyName = "Long2LongSortedMapProperty1";
					Long2LongSortedMapProperty property = keyStore.getProperty(USER, PASSWORD, Long2LongSortedMapProperty.class, propertyName);
					property.getValue().put(System.currentTimeMillis(), 1L);
					Long exampleMapKey = System.currentTimeMillis() + 24 * 3600 * 1000;
					Long exampleMapValue = 113344L;
					property.getValue().put(exampleMapKey, exampleMapValue);
					property.getValue().put(System.currentTimeMillis() + 2 * 24 * 3600 * 1000, 375438972L);
					keyStore.setProperty(USER, PASSWORD, property);
				}
			} finally {
				System.clearProperty(KeyStore.SYSTEM_PROPERTY_ENCRYPTION_ALGORITHM);
				System.clearProperty(KeyStore.SYSTEM_PROPERTY_KEY_SIZE);
				System.clearProperty(KeyStore.SYSTEM_PROPERTY_MAC_ALGORITHM);
			}
		}
	}

	@Test
	public void openReferenceKeyStores()
	throws Exception
	{
		for (String algorithm : ALGORITHMS) {
			String algoFileNameInfix = algorithm.replace('/', '-').replace(':', '.');

			if (!"default".equals(algorithm)) {
				String[] algoParts = algorithm.split(":");
				System.setProperty(KeyStore.SYSTEM_PROPERTY_ENCRYPTION_ALGORITHM, algoParts[0]);
				System.setProperty(KeyStore.SYSTEM_PROPERTY_KEY_SIZE, algoParts[1]);
				System.setProperty(KeyStore.SYSTEM_PROPERTY_MAC_ALGORITHM, algoParts[2]);
			}
			try {
				File keyStoreFile = File.createTempFile("reference." + algoFileNameInfix + '.', ".keystore");
				IOUtil.copyResource(ResourceHelper.class, "reference." + algoFileNameInfix + ".keystore", keyStoreFile);
				// The KeyStore reads the data immediately after creating a new instance.
				KeyStore keyStore = new KeyStore(keyStoreFile);

				// But nevertheless, we access a key in order to make sure, it's really loaded correctly.
				byte[] key = keyStore.getKey(USER, PASSWORD, 1);
				Assert.assertNotNull("key must not be null!", key);

				logger.info("openReferenceKeystore: Reference-KeyStore was successfully opened.");
				keyStore.generateKey(USER, PASSWORD);
				logger.info("openReferenceKeystore: New key was successfully generated.");
				keyStoreFile.delete();
			} finally {
				System.clearProperty(KeyStore.SYSTEM_PROPERTY_ENCRYPTION_ALGORITHM);
				System.clearProperty(KeyStore.SYSTEM_PROPERTY_KEY_SIZE);
				System.clearProperty(KeyStore.SYSTEM_PROPERTY_MAC_ALGORITHM);
			}
		}
	}

}
