package org.cumulus4j.keymanager.back.shared;

import javax.xml.bind.annotation.XmlRootElement;

/**
 * @author Marco หงุ่ยตระกูล-Schulze - marco at nightlabs dot de
 */
@XmlRootElement
public class GetActiveEncryptionKeyRequest extends Request
{
	private static final long serialVersionUID = 1L;

	private String keyEncryptionAlgorithm;

	private byte[] keyEncryptionPublicKey;

	public GetActiveEncryptionKeyRequest() { }

	public GetActiveEncryptionKeyRequest(String cryptoSessionID, String keyEncryptionAlgorithm, byte[] keyEncryptionPublicKey) {
		super(cryptoSessionID);
		this.keyEncryptionAlgorithm = keyEncryptionAlgorithm;
		this.keyEncryptionPublicKey = keyEncryptionPublicKey;
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
