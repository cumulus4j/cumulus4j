package org.cumulus4j.core.crypto;

public class UnknownCryptoManagerIDException extends IllegalArgumentException
{
	private static final long serialVersionUID = 1L;
	private String cryptoManagerID;

	public UnknownCryptoManagerIDException(String cryptoManagerID) {
		super("There is no CryptoManager registered with cryptoManagerID=\"" + cryptoManagerID + "\"!");
		this.cryptoManagerID = cryptoManagerID;
	}

	public String getCryptoManagerID() {
		return cryptoManagerID;
	}
}
