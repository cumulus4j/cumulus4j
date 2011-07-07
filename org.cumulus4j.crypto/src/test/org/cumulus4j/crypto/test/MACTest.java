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
package org.cumulus4j.crypto.test;

import java.security.SecureRandom;
import java.util.Arrays;

import org.bouncycastle.crypto.CipherParameters;
import org.bouncycastle.crypto.params.KeyParameter;
import org.bouncycastle.crypto.params.ParametersWithIV;
import org.cumulus4j.crypto.CryptoRegistry;
import org.cumulus4j.crypto.MACCalculator;
import org.junit.Assert;
import org.junit.Test;
import org.nightlabs.util.Util;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Marco หงุ่ยตระกูล-Schulze - marco at nightlabs dot de
 */
public class MACTest
{
	private static final Logger logger = LoggerFactory.getLogger(MACTest.class);

	private SecureRandom random = new SecureRandom();

	@Test
	public void testAllSupportedMacs() throws Exception
	{
		byte[] orig = new byte[1024 + random.nextInt(10240)];
		random.nextBytes(orig);
		for (String macAlgorithmName : CryptoRegistry.sharedInstance().getSupportedMACAlgorithms()) {
			logger.debug("------------------------------------------------------------------------");
			logger.debug("testAllSupportedMacs: macAlgorithmName={}", macAlgorithmName);
			MACCalculator macCalculator1 = CryptoRegistry.sharedInstance().createMACCalculator(macAlgorithmName, true);
			Assert.assertNotNull("CryptoRegistry.createMacCalculator(...) returned null for macAlgorithmName=" + macAlgorithmName, macCalculator1);
			byte[] mac1 = new byte[macCalculator1.getMacSize()];
			macCalculator1.update(orig, 0, orig.length);
			macCalculator1.doFinal(mac1, 0);

			byte[] macKey = null;
			byte[] macIV = null;
			CipherParameters macParams = macCalculator1.getParameters();
			if (macParams instanceof ParametersWithIV) {
				ParametersWithIV piv = (ParametersWithIV) macParams;
				macIV = piv.getIV();
				macKey = ((KeyParameter)piv.getParameters()).getKey();
			}
			else if (macParams instanceof KeyParameter) {
				macKey = ((KeyParameter)macParams).getKey();
			}
			else
				throw new IllegalStateException("macParams type unsupported type=" + (macParams == null ? null : macParams.getClass().getName()) + " macParams=" + macParams);

			logger.debug("testAllSupportedMacs: macKey={}", Util.encodeHexStr(macKey));
			logger.debug("testAllSupportedMacs: macIV={}", macIV == null ? null : Util.encodeHexStr(macIV));

			MACCalculator macCalculator2 = CryptoRegistry.sharedInstance().createMACCalculator(macAlgorithmName, false);
			CipherParameters macCipherParameters2 = null;
			if (macIV == null)
				macCipherParameters2 = new KeyParameter(macKey);
			else
				macCipherParameters2 = new ParametersWithIV(new KeyParameter(macKey), macIV);

			byte[] mac2 = new byte[macCalculator2.getMacSize()];
			macCalculator2.init(macCipherParameters2);
			macCalculator2.update(orig, 0, orig.length);
			macCalculator2.doFinal(mac2, 0);

			logger.debug("testAllSupportedMacs: mac1={}", Util.encodeHexStr(mac1));
			logger.debug("testAllSupportedMacs: mac2={}", Util.encodeHexStr(mac2));

			Assert.assertArrayEquals(mac1, mac2);



			if (macIV != null) {
				byte[] wrongMacIV = macIV.clone();
				random.nextBytes(wrongMacIV);
				MACCalculator macCalculator3 = CryptoRegistry.sharedInstance().createMACCalculator(macAlgorithmName, false);
				CipherParameters macCipherParameters3 = new ParametersWithIV(new KeyParameter(macKey), wrongMacIV);
				byte[] mac3 = new byte[macCalculator3.getMacSize()];
				macCalculator3.init(macCipherParameters3);
				macCalculator3.update(orig, 0, orig.length);
				macCalculator3.doFinal(mac3, 0);

				logger.debug("testAllSupportedMacs: wrongIV={}", Util.encodeHexStr(wrongMacIV));
				logger.debug("testAllSupportedMacs: wrongMAC={}", Util.encodeHexStr(mac3));
				Assert.assertFalse("Passed different MAC-IV, but still got the same MAC! It seems the IV is ignored!", Arrays.equals(mac1, mac3));
			}


			{
				byte[] wrongMacKey = macKey.clone();
				random.nextBytes(wrongMacKey);
				MACCalculator macCalculator4 = CryptoRegistry.sharedInstance().createMACCalculator(macAlgorithmName, false);

				CipherParameters macCipherParameters4 = null;
				if (macIV == null)
					macCipherParameters4 = new KeyParameter(wrongMacKey);
				else
					macCipherParameters4 = new ParametersWithIV(new KeyParameter(wrongMacKey), macIV);

				byte[] mac4 = new byte[macCalculator4.getMacSize()];
				macCalculator4.init(macCipherParameters4);
				macCalculator4.update(orig, 0, orig.length);
				macCalculator4.doFinal(mac4, 0);

				logger.debug("testAllSupportedMacs: wrongKey={}", Util.encodeHexStr(wrongMacKey));
				logger.debug("testAllSupportedMacs: wrongMAC={}", Util.encodeHexStr(mac4));
				Assert.assertFalse("Passed different MAC-keys, but still got the same MAC! It seems the key is ignored!", Arrays.equals(mac1, mac4));
			}


			{
				byte[] wrongData = orig.clone();

				// change one arbitrary bit in the data
				int byteIdx = random.nextInt(wrongData.length);
				int bitIdx = random.nextInt(8);

				int v = wrongData[byteIdx] & 0xff;
				v ^= 1 << bitIdx;
				wrongData[byteIdx] = (byte)v;

				byte[] mac5 = new byte[macCalculator2.getMacSize()];
				macCalculator2.update(wrongData, 0, wrongData.length);
				macCalculator2.doFinal(mac5, 0);

				logger.debug("testAllSupportedMacs: MACforWrongData={}", Util.encodeHexStr(mac5));
				Assert.assertFalse("Passed different data, but still got the same MAC!", Arrays.equals(mac1, mac5));


				byte[] mac6 = new byte[macCalculator2.getMacSize()];
				macCalculator2.update(orig, 0, orig.length);
				macCalculator2.doFinal(mac6, 0);
				Assert.assertArrayEquals(mac1, mac6);
			}
		}
	}

}
