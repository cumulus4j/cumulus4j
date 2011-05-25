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
	private String keyAlgorithm;
	private byte[] keyEncodedEncrypted;

	public GetKeyResponse() { }

	public GetKeyResponse(Request request, long keyID, String keyAlgorithm, byte[] keyEncodedEncrypted) {
		super(request);

		if (keyAlgorithm == null)
			throw new IllegalArgumentException("keyAlgorithm == null");

		if (keyEncodedEncrypted == null)
			throw new IllegalArgumentException("keyEncodedEncrypted == null");

		this.keyID = keyID;
		this.keyAlgorithm = keyAlgorithm;
		this.keyEncodedEncrypted = keyEncodedEncrypted;
	}

	public long getKeyID() {
		return keyID;
	}
	public void setKeyID(long keyID) {
		this.keyID = keyID;
	}

	public String getKeyAlgorithm() {
		return keyAlgorithm;
	}
	public void setKeyAlgorithm(String keyAlgorithm) {
		this.keyAlgorithm = keyAlgorithm;
	}

	public byte[] getKeyEncodedEncrypted() {
		return keyEncodedEncrypted;
	}
	public void setKeyEncodedEncrypted(byte[] key) {
		this.keyEncodedEncrypted = key;
	}
}
