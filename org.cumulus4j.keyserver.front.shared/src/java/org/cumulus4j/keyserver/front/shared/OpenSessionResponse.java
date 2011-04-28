package org.cumulus4j.keyserver.front.shared;

import java.io.Serializable;

import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement
public class OpenSessionResponse implements Serializable
{
	private static final long serialVersionUID = 1L;

	private String cryptoSessionID;

	public String getCryptoSessionID() {
		return cryptoSessionID;
	}
	public void setCryptoSessionID(String cryptoSessionID) {
		this.cryptoSessionID = cryptoSessionID;
	}

}
