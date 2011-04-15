package org.cumulus4j.keystore;

import java.security.Key;

public class MasterKey
{
	public MasterKey(Key key)
	{
		if (key == null)
			throw new IllegalArgumentException("key == null");

		this.key = key;
	}

	private Key key;

	public Key getKey() {
		return key;
	}
}
