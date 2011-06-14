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

import org.bouncycastle.crypto.CipherParameters;
import org.bouncycastle.crypto.params.KeyParameter;
import org.bouncycastle.crypto.params.ParametersWithIV;
import org.cumulus4j.crypto.CryptoRegistry;
import org.cumulus4j.crypto.MacCalculator;
import org.junit.Assert;
import org.junit.Test;
import org.nightlabs.util.Util;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class MacTest
{
	private static final Logger logger = LoggerFactory.getLogger(MacTest.class);

	private SecureRandom random = new SecureRandom();

	@Test
	public void testAllSupportedMacs() throws Exception
	{
		byte[] orig = new byte[1024 + random.nextInt(10240)];
		random.nextBytes(orig);
		for (String macAlgorithmName : CryptoRegistry.sharedInstance().getSupportedMacAlgorithms()) {
			logger.debug("testAllSupportedMacs: macAlgorithmName={}", macAlgorithmName);
			MacCalculator macCalculator1 = CryptoRegistry.sharedInstance().createMacCalculator(macAlgorithmName, true);
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

			MacCalculator macCalculator2 = CryptoRegistry.sharedInstance().createMacCalculator(macAlgorithmName, false);
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
		}
	}

}
