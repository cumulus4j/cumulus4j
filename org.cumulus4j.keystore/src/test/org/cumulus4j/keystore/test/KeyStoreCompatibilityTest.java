package org.cumulus4j.keystore.test;

import java.io.File;
import java.security.Key;

import junit.framework.Assert;

import org.cumulus4j.keystore.KeyStore;
import org.cumulus4j.keystore.test.resource.ResourceHelper;
import org.junit.Test;
import org.nightlabs.util.IOUtil;

public class KeyStoreCompatibilityTest
{
	protected static final String USER = KeyStoreKeyTest.USER;
	protected static final char[] PASSWORD = KeyStoreKeyTest.PASSWORD;

	@Test
	public void openReferenceKeystore()
	throws Exception
	{
		File keyStoreFile = File.createTempFile("test-", ".keystore");
		IOUtil.copyResource(ResourceHelper.class, "test-1.keystore", keyStoreFile);
		// The KeyStore reads the data immediately after creating a new instance.
		KeyStore keyStore = new KeyStore(keyStoreFile);

		// But nevertheless, we access a key in order to make sure, it's really loaded correctly.
		Key key = keyStore.getKey(USER, PASSWORD, 1);
		Assert.assertNotNull("key must not be null!", key);
	}

}
