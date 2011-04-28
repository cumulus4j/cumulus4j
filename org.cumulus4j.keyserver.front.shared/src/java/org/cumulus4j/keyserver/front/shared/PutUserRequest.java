package org.cumulus4j.keyserver.front.shared;

import java.io.Serializable;

import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement
public class PutUserRequest implements Serializable
{
	private static final long serialVersionUID = 1L;

	private Auth auth;

	private String userName;

	private char[] password;

	public PutUserRequest() { }

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

	public char[] getPassword() {
		return password;
	}

	public void setPassword(char[] password) {
		this.password = password;
	}
}
