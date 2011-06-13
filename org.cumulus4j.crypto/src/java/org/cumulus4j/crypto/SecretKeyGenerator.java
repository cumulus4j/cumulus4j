package org.cumulus4j.crypto;

import org.bouncycastle.crypto.KeyGenerationParameters;
import org.bouncycastle.crypto.params.KeyParameter;

public interface SecretKeyGenerator
{
	void init(KeyGenerationParameters params);

	KeyParameter generateKey();
}
