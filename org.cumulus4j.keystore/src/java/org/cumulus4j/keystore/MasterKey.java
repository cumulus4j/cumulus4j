package org.cumulus4j.keystore;

import java.security.spec.KeySpec;
import java.util.Arrays;

import javax.crypto.SecretKey;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Marco หงุ่ยตระกูล-Schulze - marco at nightlabs dot de
 */
class MasterKey
implements SecretKey, KeySpec
{
	private static final Logger logger = LoggerFactory.getLogger(MasterKey.class);

	private byte[] keyData;
	private String algorithm;

	public MasterKey(byte[] keyData, String algorithm)
	{
		if (keyData == null)
			throw new IllegalArgumentException("keyData == null");

		if (algorithm == null)
			throw new IllegalArgumentException("algorithm == null");

		this.keyData = keyData;
		this.algorithm = algorithm;
	}

	public void clear()
	{
		logger.debug("clear: Clearing {}", this);

		// SecretKeySpec returns a copy of the byte array. To erase the data safely,
		// we thus now implement the interfaces in this class ourselves and clear our
		// locally held data.
		byte[] data = keyData;
		if (data != null) {
			Arrays.fill(data, (byte)0);
			keyData = null;
		}
	}

	@Override
	protected void finalize() throws Throwable
	{
		clear();
		super.finalize();
	}

	@Override
	public String getAlgorithm() {
		return algorithm;
	}

	@Override
	public byte[] getEncoded()
	{
		// SecretKeySpec returns a copy of the byte array here and we do the same. I just created & read
		// 10K keys (see KeyStoreKeyTest) and didn't see any performance difference (even though I used
		// 256 bit keys, which are longer than what we'll probably use in productive environment).
		// Obviously System.arraycopy(...) is *extremely* *fast*.
		// Marco :-)
		byte[] result = new byte[keyData.length];
		System.arraycopy(keyData, 0, result, 0, keyData.length);
		return result;
	}

	@Override
	public String getFormat() {
		return "RAW";
	}
}
