package org.cumulus4j.keyserver.front.shared;

import java.io.Serializable;

import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement
public class OpenSessionRequest
implements Serializable
{
	private static final long serialVersionUID = 1L;

	public OpenSessionRequest() { }

	public OpenSessionRequest(Auth auth) {
		this.auth = auth;
	}

	private Auth auth;

	public Auth getAuth() {
		return auth;
	}

	public void setAuth(Auth auth) {
		this.auth = auth;
	}
}
