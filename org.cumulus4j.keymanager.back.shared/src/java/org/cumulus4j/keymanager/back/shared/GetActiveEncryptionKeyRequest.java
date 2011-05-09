package org.cumulus4j.keymanager.back.shared;

import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement
public class GetActiveEncryptionKeyRequest extends Request
{
	private static final long serialVersionUID = 1L;

	public GetActiveEncryptionKeyRequest() { }

	public GetActiveEncryptionKeyRequest(String cryptoSessionID) {
		super(cryptoSessionID);
	}
}
