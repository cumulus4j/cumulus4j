package org.cumulus4j.keymanager.front.shared;

import java.io.Serializable;

import javax.xml.bind.annotation.XmlRootElement;

/**
 * @author Marco หงุ่ยตระกูล-Schulze - marco at nightlabs dot de
 */
@XmlRootElement
public class User
implements Serializable
{
	private static final long serialVersionUID = 1L;

	private String userName;

	public User() { }

	public User(String userName) {
		this.userName = userName;
	}

	public String getUserName() {
		return userName;
	}
	public void setUserName(String userName) {
		this.userName = userName;
	}
}
