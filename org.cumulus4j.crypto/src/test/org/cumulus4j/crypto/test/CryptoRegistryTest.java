package org.cumulus4j.crypto.test;

import org.cumulus4j.crypto.CryptoRegistry;
import org.junit.Test;

/**
 * @author Marco หงุ่ยตระกูล-Schulze - marco at nightlabs dot de
 */
public class CryptoRegistryTest
{
	@Test
	public void simpleTest()
	{
		CryptoRegistry.sharedInstance();
	}

}
