package org.cumulus4j.keystore.test;

import java.io.File;
import java.security.Key;

import junit.framework.Assert;

import org.cumulus4j.keystore.KeyStore;
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

	@Test
	public void createNewReferenceKeyStore()
	throws Exception
	{
		File keyStoreFile = File.createTempFile("new-reference-", ".keystore");
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
	}

	@Test
	public void openReferenceKeyStore()
	throws Exception
	{
		File keyStoreFile = File.createTempFile("current-reference-", ".keystore");
		IOUtil.copyResource(ResourceHelper.class, "current-reference.keystore", keyStoreFile);
		// The KeyStore reads the data immediately after creating a new instance.
		KeyStore keyStore = new KeyStore(keyStoreFile);

		// But nevertheless, we access a key in order to make sure, it's really loaded correctly.
		Key key = keyStore.getKey(USER, PASSWORD, 1);
		Assert.assertNotNull("key must not be null!", key);

		logger.info("openReferenceKeystore: Reference-KeyStore was successfully opened.");
		keyStore.generateKey(USER, PASSWORD);
		logger.info("openReferenceKeystore: New key was successfully generated.");
	}

}
