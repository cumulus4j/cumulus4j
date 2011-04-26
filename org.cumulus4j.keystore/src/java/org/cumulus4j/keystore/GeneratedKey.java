package org.cumulus4j.keystore;

import java.security.Key;

public class GeneratedKey
{
	private long keyID;
	private Key key;

	public GeneratedKey(long keyID, Key key) {
		this.keyID = keyID;
		this.key = key;
	}

	public long getKeyID() {
		return keyID;
	}

	public Key getKey() {
		return key;
	}
}
