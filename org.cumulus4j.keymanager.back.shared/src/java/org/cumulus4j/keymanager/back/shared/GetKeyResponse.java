package org.cumulus4j.keymanager.back.shared;

import javax.xml.bind.annotation.XmlRootElement;

/**
 * @author Marco หงุ่ยตระกูล-Schulze - marco at nightlabs dot de
 */
@XmlRootElement
public class GetKeyResponse extends Response
{
	private static final long serialVersionUID = 1L;

	private long keyID;
	private byte[] keyEncodedEncrypted;

	public GetKeyResponse() { }

	public GetKeyResponse(Request request, long keyID, byte[] keyEncodedEncrypted) {
		super(request);

		if (keyEncodedEncrypted == null)
			throw new IllegalArgumentException("keyEncodedEncrypted == null");

		this.keyID = keyID;
		this.keyEncodedEncrypted = keyEncodedEncrypted;
	}

	public long getKeyID() {
		return keyID;
	}
	public void setKeyID(long keyID) {
		this.keyID = keyID;
	}

	public byte[] getKeyEncodedEncrypted() {
		return keyEncodedEncrypted;
	}
	public void setKeyEncodedEncrypted(byte[] key) {
		this.keyEncodedEncrypted = key;
	}
}
