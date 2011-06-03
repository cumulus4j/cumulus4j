package org.cumulus4j.crypto.mode;

import org.bouncycastle.crypto.BlockCipher;
import org.bouncycastle.crypto.modes.OFBBlockCipher;

/**
 * @author Marco หงุ่ยตระกูล-Schulze - marco at nightlabs dot de
 */
public class C4jOFBBlockCipher extends OFBBlockCipher
{
	private static int determineBitBlockSize(BlockCipher engine, String modeName)
	{
		if (modeName.length() != 3)
    {
        int wordSize = Integer.parseInt(modeName.substring(3));
        return wordSize;
    }
		else
			return 8 * engine.getBlockSize();
	}

	public C4jOFBBlockCipher(BlockCipher engine, String modeName) {
		super(engine, determineBitBlockSize(engine, modeName));
	}
}
