package org.cumulus4j.keyserver.back.shared;

import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement
public class GetKeyRequest extends Request
{
	private static final long serialVersionUID = 1L;

	private String cryptoSessionID;
	private long keyID;

	public GetKeyRequest() { }

	public GetKeyRequest(String keyServerID, String cryptoSessionID, long keyID) {
		super(keyServerID);
		this.cryptoSessionID = cryptoSessionID;
		this.keyID = keyID;
	}

	public String getCryptoSessionID() {
		return cryptoSessionID;
	}
	public void setCryptoSessionID(String cryptoSessionID) {
		this.cryptoSessionID = cryptoSessionID;
	}
	public long getKeyID() {
		return keyID;
	}
	public void setKeyID(long keyID) {
		this.keyID = keyID;
	}
}
