package org.cumulus4j.keystore.test;

import org.cumulus4j.keystore.KeyStore;
import org.junit.AfterClass;
import org.junit.BeforeClass;

public class KeyStoreKey_AES_CBC_PKCS5Padding_Test
extends KeyStoreKeyTest
{
	@BeforeClass
	public static void beforeClass()
	{
		System.setProperty(KeyStore.SYSTEM_PROPERTY_ENCRYPTION_ALGORITHM, "AES/CBC/PKCS5Padding");
	}

	@AfterClass
	public static void afterClass()
	{
		System.clearProperty(KeyStore.SYSTEM_PROPERTY_ENCRYPTION_ALGORITHM);
	}
}
