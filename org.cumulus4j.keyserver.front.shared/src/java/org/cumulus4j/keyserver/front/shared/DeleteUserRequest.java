package org.cumulus4j.keyserver.front.shared;

import java.io.Serializable;

import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement
public class DeleteUserRequest implements Serializable
{
	private static final long serialVersionUID = 1L;

	private Auth auth;

	private String userName;

	public DeleteUserRequest() { }

	public Auth getAuth() {
		return auth;
	}
	public void setAuth(Auth auth) {
		this.auth = auth;
	}

	public String getUserName() {
		return userName;
	}

	public void setUserName(String userName) {
		this.userName = userName;
	}
}
