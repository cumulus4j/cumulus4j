package org.cumulus4j.keystore;

import java.security.NoSuchAlgorithmException;
import java.util.Arrays;

import org.bouncycastle.crypto.params.KeyParameter;
import org.bouncycastle.crypto.params.ParametersWithIV;
import org.cumulus4j.crypto.CryptoRegistry;
import org.cumulus4j.crypto.MACCalculator;

/**
 * @author Marco หงุ่ยตระกูล-Schulze - marco at nightlabs dot de
 */
class PlaintextDataAndMac
{
	public PlaintextDataAndMac(byte[] dataAndMac, AbstractEncryptedData encryptedKey)
	{
		this(
				dataAndMac,
				encryptedKey.getMacAlgorithm(), encryptedKey.getMacKeySize(), encryptedKey.getMacIVSize(), encryptedKey.getMacSize()
		);
	}

	private PlaintextDataAndMac(byte[] dataAndMac, String macAlgorithm, short macKeySize, short macIVSize, short macSize)
	{
		if (dataAndMac == null)
			throw new IllegalArgumentException("dataAndMac == null");

		if (macAlgorithm == null)
			throw new IllegalArgumentException("macAlgorithm == null");

		this.macAlgorithm = macAlgorithm;
		this.macKey = new byte[macKeySize];
		this.macIV = new byte[macIVSize];
		this.data = new byte[dataAndMac.length - macSize - macIVSize - macKeySize];
		this.mac = new byte[macSize];

		int srcPos = 0;

		System.arraycopy(dataAndMac, srcPos, this.macKey, 0, this.macKey.length);
		srcPos += this.macKey.length;

		System.arraycopy(dataAndMac, srcPos, this.macIV, 0, this.macIV.length);
		srcPos += this.macIV.length;

		System.arraycopy(dataAndMac, srcPos, this.data, 0, this.data.length);
		srcPos += this.data.length;

		System.arraycopy(dataAndMac, srcPos, this.mac, 0, this.mac.length);
		srcPos += this.mac.length;

		if (srcPos != dataAndMac.length)
			throw new IllegalStateException("srcPos != dataAndMac.length :: " + srcPos + " != " + dataAndMac.length);
	}

	public PlaintextDataAndMac(byte[] data, String macAlgorithm) throws NoSuchAlgorithmException
	{
		if (data == null)
			throw new IllegalArgumentException("data == null");

		if (macAlgorithm == null)
			throw new IllegalArgumentException("macAlgorithm == null");

		this.macAlgorithm = macAlgorithm;
		this.data = data;
		this.macKey = this.macIV = this.mac = new byte[0];

		if (!KeyStore.MAC_ALGORITHM_NONE.equals(macAlgorithm)) {
			MACCalculator macCalculator = CryptoRegistry.sharedInstance().createMACCalculator(this.macAlgorithm, true);
			if (macCalculator.getParameters() == null)
				throw new IllegalStateException("The MACCalculator for macAlgorithm=\"" + macAlgorithm + "\" was NOT initialised!");
			else if (macCalculator.getParameters() instanceof ParametersWithIV) {
				ParametersWithIV pwiv = (ParametersWithIV)macCalculator.getParameters();
				this.macIV = pwiv.getIV();
				KeyParameter kp = (KeyParameter) pwiv.getParameters();
				this.macKey = kp.getKey();
			}
			else if (macCalculator.getParameters() instanceof KeyParameter) {
				KeyParameter kp = (KeyParameter) macCalculator.getParameters();
				this.macKey = kp.getKey();
			}
			else
				throw new IllegalStateException("The MACCalculator for macAlgorithm=\"" + macAlgorithm + "\" was initialised with an unknown parameter (type " + macCalculator.getParameters().getClass().getName() + ")!");

			this.mac = new byte[macCalculator.getMacSize()];
			macCalculator.update(this.data, 0, this.data.length);
			macCalculator.doFinal(this.mac, 0);
		}
	}

	private String macAlgorithm;
	private byte[] macKey;
	private byte[] macIV;
	private byte[] data;
	private byte[] mac;

	public String getMacAlgorithm() {
		return macAlgorithm;
	}

	public byte[] getMacKey() {
		return macKey;
	}
	public byte[] getMacIV() {
		return macIV;
	}
	public byte[] getData() {
		return data;
	}
	public byte[] getMac() {
		return mac;
	}

	public byte[] toByteArray()
	{
		byte[] result = new byte[macKey.length +  macIV.length + data.length + mac.length];

		int destPos = 0;

		System.arraycopy(macKey, 0, result, destPos, macKey.length);
		destPos += macKey.length;

		System.arraycopy(macIV, 0, result, destPos, macIV.length);
		destPos += macIV.length;

		System.arraycopy(data, 0, result, destPos, data.length);
		destPos += data.length;

		System.arraycopy(mac, 0, result, destPos, mac.length);
		destPos += mac.length;

		if (destPos != result.length)
			throw new IllegalStateException("destPos != result.length :: " + destPos + " != " + result.length);

		return result;
	}

	public boolean verifyMAC()
	throws NoSuchAlgorithmException
	{
		boolean result = true;
		if (!KeyStore.MAC_ALGORITHM_NONE.equals(macAlgorithm)) {
			MACCalculator macCalculator = CryptoRegistry.sharedInstance().createMACCalculator(macAlgorithm, false);
			if (this.getMacIV().length > 0)
				macCalculator.init(new ParametersWithIV(new KeyParameter(this.getMacKey()), this.getMacIV()));
			else
				macCalculator.init(new KeyParameter(this.getMacKey()));

			byte[] newMAC = new byte[macCalculator.getMacSize()];
			macCalculator.update(this.getData(), 0, this.getData().length);
			macCalculator.doFinal(newMAC, 0);
			if (!Arrays.equals(this.getMac(), newMAC)) {
				result = false;
				KeyStore.logger.warn(
						"verifyMAC: MAC verification failed! macAlgorithm={} expectedMac={} calculatedMac={}",
						new Object[] { macAlgorithm, KeyStoreUtil.encodeHexStr(this.getMac()), KeyStoreUtil.encodeHexStr(newMAC) }
				);
			}
		}
		return result;
	}
}