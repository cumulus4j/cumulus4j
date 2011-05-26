package org.cumulus4j.keymanager.back.shared;

import javax.xml.bind.annotation.XmlRootElement;

/**
 * @author Marco หงุ่ยตระกูล-Schulze - marco at nightlabs dot de
 */
@XmlRootElement
public abstract class Request extends Message
{
	private static final long serialVersionUID = 1L;

	private String cryptoSessionID;

	public Request() { }

	public Request(String cryptoSessionID)
	{
		if (cryptoSessionID == null)
			throw new IllegalArgumentException("cryptoSessionID == null");

		setRequestID(IdentifierUtil.createRandomID());
		this.cryptoSessionID = cryptoSessionID;
	}

	public String getCryptoSessionID() {
		return cryptoSessionID;
	}
	public void setCryptoSessionID(String cryptoSessionID) {
		this.cryptoSessionID = cryptoSessionID;
	}

	public String getCryptoSessionIDPrefix()
	{
		String id = cryptoSessionID;
		if (id == null)
			return null;

		int dotIdx = id.indexOf('.');
		if (dotIdx < 0)
			throw new IllegalStateException("cryptoSessionID does not contain a dot ('.')!!!");

		return id.substring(0, dotIdx);
	}

	@Override
	public String toString() {
		return super.toString() + '[' + getRequestID() + ',' + cryptoSessionID + ']';
	}
}
