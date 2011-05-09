package org.cumulus4j.keymanager.back.shared;

import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement
public class GetKeyRequest extends Request
{
	private static final long serialVersionUID = 1L;

	private long keyID;

	public GetKeyRequest() { }

	public GetKeyRequest(String cryptoSessionID, long keyID) {
		super(cryptoSessionID);
		this.keyID = keyID;
	}

	public long getKeyID() {
		return keyID;
	}
	public void setKeyID(long keyID) {
		this.keyID = keyID;
	}
}
