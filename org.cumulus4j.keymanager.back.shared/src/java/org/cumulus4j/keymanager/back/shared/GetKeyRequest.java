package org.cumulus4j.keymanager.back.shared;

import javax.xml.bind.annotation.XmlRootElement;

/**
 * @author Marco หงุ่ยตระกูล-Schulze - marco at nightlabs dot de
 */
@XmlRootElement
public class GetKeyRequest extends Request
{
	private static final long serialVersionUID = 1L;

	private long keyID;

	private String keyEncryptionAlgorithm;

	private byte[] keyEncryptionPublicKey;

	public GetKeyRequest() { }

	public GetKeyRequest(String cryptoSessionID, long keyID, String keyEncryptionAlgorithm, byte[] keyEncryptionPublicKey) {
		super(cryptoSessionID);
		this.keyID = keyID;
		this.keyEncryptionAlgorithm = keyEncryptionAlgorithm;
		this.keyEncryptionPublicKey = keyEncryptionPublicKey;
	}

	public long getKeyID() {
		return keyID;
	}
	public void setKeyID(long keyID) {
		this.keyID = keyID;
	}

	public String getKeyEncryptionAlgorithm() {
		return keyEncryptionAlgorithm;
	}
	public void setKeyEncryptionAlgorithm(String keyEncryptionAlgorithm) {
		this.keyEncryptionAlgorithm = keyEncryptionAlgorithm;
	}

	public byte[] getKeyEncryptionPublicKey() {
		return keyEncryptionPublicKey;
	}
	public void setKeyEncryptionPublicKey(byte[] keyEncryptionPublicKey) {
		this.keyEncryptionPublicKey = keyEncryptionPublicKey;
	}
}
