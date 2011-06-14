package org.cumulus4j.crypto;

import org.bouncycastle.crypto.CipherParameters;
import org.bouncycastle.crypto.Mac;

public interface MacCalculator
extends Mac
{

	void setAlgorithmName(String algorithmName);

	CipherParameters getParameters();

	/**
	 * Get the required size of the key (in bytes). This is usually the same as the {@link #getIVSize() IV size}.
	 * @return the required size of the key (in bytes).
	 */
	int getKeySize();

	/**
	 * Get the required size of the IV (in bytes). If a MAC supports multiple sizes, this is the optimal (most secure) IV size.
	 * @return the required size of the IV.
	 */
	int getIVSize();

}
