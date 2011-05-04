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

	private String cryptoSessionID;

	private Date expiry;

	/**
	 * <p>
	 * Get the crypto-session's unique identifier.
	 * </p>
	 * <p>
	 * This identifier is composed of 3 parts:
	 * </p>
	 * <ul>
	 * <li><code>cryptoSessionIDPrefix</code>: A random ID of the key server. This is used to optimize communication between app-server
	 * and key server. A new prefix is generated at every startup of the key server.
	 * </li>
	 * <li>Separator '.': A dot is used as separator.</li>
	 * <li>The rest of the cryptoSessionID, which is unique within the scope of the prefix.</li>
	 * </ul>
	 *
	 * @return the crypto-session's unique identifier.
	 */
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
