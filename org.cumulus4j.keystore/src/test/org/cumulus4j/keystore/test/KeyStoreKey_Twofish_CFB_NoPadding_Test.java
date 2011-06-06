package org.cumulus4j.keystore.test;

import org.cumulus4j.keystore.KeyStore;
import org.junit.AfterClass;
import org.junit.BeforeClass;

public class KeyStoreKey_Twofish_CFB_NoPadding_Test
extends KeyStoreKeyTest
{
	@BeforeClass
	public static void beforeClass()
	{
		System.setProperty(KeyStore.SYSTEM_PROPERTY_ENCRYPTION_ALGORITHM, "Twofish/CFB/NoPadding");
	}

	@AfterClass
	public static void afterClass()
	{
		System.clearProperty(KeyStore.SYSTEM_PROPERTY_ENCRYPTION_ALGORITHM);
	}
}
