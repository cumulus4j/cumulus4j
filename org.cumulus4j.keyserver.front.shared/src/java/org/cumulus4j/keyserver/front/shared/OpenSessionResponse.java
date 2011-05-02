package org.cumulus4j.keyserver.front.shared;

import java.io.Serializable;
import java.util.Date;

import javax.xml.bind.annotation.XmlRootElement;

/**
 * @author Marco หงุ่ยตระกูล-Schulze - marco at nightlabs dot de
 */
@XmlRootElement
public class OpenSessionResponse implements Serializable
{
	private static final long serialVersionUID = 1L;

	private String keyServerID;

	private String cryptoSessionID;

	private Date expiry;

	public String getKeyServerID() {
		return keyServerID;
	}
	public void setKeyServerID(String keyServerID) {
		this.keyServerID = keyServerID;
	}

	public String getCryptoSessionID() {
		return cryptoSessionID;
	}
	public void setCryptoSessionID(String cryptoSessionID) {
		this.cryptoSessionID = cryptoSessionID;
	}

	public Date getExpiry() {
		return expiry;
	}
	public void setExpiry(Date expiry) {
		this.expiry = expiry;
	}

}
