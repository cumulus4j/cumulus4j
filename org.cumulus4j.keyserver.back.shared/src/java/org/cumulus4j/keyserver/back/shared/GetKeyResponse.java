package org.cumulus4j.keyserver.back.shared;

import java.security.Key;

import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement
public class GetKeyResponse extends Response
{
	private static final long serialVersionUID = 1L;

	private String keyAlgorithm;
	private byte[] keyEncoded;

	public GetKeyResponse() { }

	public GetKeyResponse(GetKeyRequest request, Key key) {
		super(request);

		if (key == null)
			throw new IllegalArgumentException("keyEncoded == null");

		this.keyAlgorithm = key.getAlgorithm();
		this.keyEncoded = key.getEncoded();
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
