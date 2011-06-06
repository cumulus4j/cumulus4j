package org.cumulus4j.keymanager.back.shared;

import java.util.Date;

import javax.xml.bind.annotation.XmlRootElement;

/**
 * @author Marco หงุ่ยตระกูล-Schulze - marco at nightlabs dot de
 */
@XmlRootElement
public class GetActiveEncryptionKeyResponse extends GetKeyResponse
{
	private static final long serialVersionUID = 1L;

	private Date activeUntilExcl;

	public GetActiveEncryptionKeyResponse() { }

	public GetActiveEncryptionKeyResponse(Request request, long keyID, String keyAlgorithm, byte[] keyEncodedEncrypted, Date activeUntilExcl)
	{
		super(request, keyID, keyAlgorithm, keyEncodedEncrypted);
		this.activeUntilExcl = activeUntilExcl;
	}

	public Date getActiveUntilExcl() {
		return activeUntilExcl;
	}
	public void setActiveUntilExcl(Date activeUntilExcl) {
		this.activeUntilExcl = activeUntilExcl;
	}
}
