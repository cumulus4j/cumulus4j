package org.cumulus4j.keystore;

import java.util.Arrays;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Marco หงุ่ยตระกูล-Schulze - marco at nightlabs dot de
 */
class MasterKey
{
	private static final long serialVersionUID = 1L;

	private static final Logger logger = LoggerFactory.getLogger(MasterKey.class);

	private byte[] keyData;

	public MasterKey(byte[] keyData)
	{
		if (keyData == null)
			throw new IllegalArgumentException("keyData == null");

		this.keyData = keyData;
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

//	@Override
//	public String getAlgorithm() {
//		return algorithm;
//	}

//	@Override
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

//	@Override
//	public String getFormat() {
//		return "RAW";
//	}
}
