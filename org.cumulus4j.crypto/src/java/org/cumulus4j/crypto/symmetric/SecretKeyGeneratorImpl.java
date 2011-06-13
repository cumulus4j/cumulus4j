package org.cumulus4j.crypto.symmetric;

import java.security.SecureRandom;

import org.bouncycastle.crypto.KeyGenerationParameters;
import org.bouncycastle.crypto.params.KeyParameter;
import org.cumulus4j.crypto.SecretKeyGenerator;

public class SecretKeyGeneratorImpl implements SecretKeyGenerator
{
	private int strength;
	private int strengthInBytes;
	private SecureRandom random;

	@Override
	public void init(KeyGenerationParameters params)
	{
		strength = 0;
		random = null;

		if (params != null) {
			strength = params.getStrength();
			random = params.getRandom();
		}

		if (strength < 1)
			strength = 256;

		if (random == null)
			random = new SecureRandom();

		strengthInBytes = (strength + 7) / 8;
	}

	@Override
	public KeyParameter generateKey()
	{
		if (random == null)
			throw new IllegalStateException("init(...) was not yet called!");

		byte[] key = new byte[strengthInBytes];
		random.nextBytes(key);
		return new KeyParameter(key);
	}

}
