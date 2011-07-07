package org.cumulus4j.crypto.internal.symmetric.mode;

import org.bouncycastle.crypto.BlockCipher;
import org.bouncycastle.crypto.modes.CBCBlockCipher;
import org.bouncycastle.crypto.modes.CTSBlockCipher;

/**
 * @author Marco หงุ่ยตระกูล-Schulze - marco at nightlabs dot de
 */
public class C4jCBCCTSBlockCipher
extends CTSBlockCipher
{
	public C4jCBCCTSBlockCipher(BlockCipher cipher) {
		super(new CBCBlockCipher(cipher));
	}
}
