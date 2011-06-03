package org.cumulus4j.crypto.mode;

import org.bouncycastle.crypto.BlockCipher;
import org.bouncycastle.crypto.modes.CFBBlockCipher;

public class C4jCFBBlockCipher extends CFBBlockCipher
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

	public C4jCFBBlockCipher(BlockCipher engine, String modeName) {
		super(engine, determineBitBlockSize(engine, modeName));
	}
}
