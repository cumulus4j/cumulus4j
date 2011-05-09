package org.cumulus4j.keyserver.back.shared;

import java.security.Key;

import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement
public class GetKeyResponse extends Response
{
	private static final long serialVersionUID = 1L;

	private long keyID;
	private String keyAlgorithm;
	private byte[] keyEncoded;

	public GetKeyResponse() { }

	public GetKeyResponse(Request request, long keyID, Key key) {
		super(request);

		if (key == null)
			throw new IllegalArgumentException("keyEncoded == null");

		this.keyID = keyID;
		this.keyAlgorithm = key.getAlgorithm();
		this.keyEncoded = key.getEncoded();
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

	public byte[] getKeyEncoded() {
		return keyEncoded;
	}
	public void setKeyEncoded(byte[] key) {
		this.keyEncoded = key;
	}
}
