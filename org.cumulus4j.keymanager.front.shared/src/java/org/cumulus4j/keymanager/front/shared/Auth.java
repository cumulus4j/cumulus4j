package org.cumulus4j.keymanager.front.shared;

import java.io.Serializable;
import java.util.Arrays;

import javax.xml.bind.annotation.XmlRootElement;

/**
 * @author Marco หงุ่ยตระกูล-Schulze - marco at nightlabs dot de
 */
@XmlRootElement
public class Auth
implements Serializable
{
	private static final long serialVersionUID = 1L;

	private String userName;

	private char[] password;

	public Auth() { }

	public Auth(String userName, char[] password)
	{
		this.userName = userName;
		this.password = password;
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

	public void clear()
	{
		if (password != null)
			Arrays.fill(password, (char)0);

		password = null;
	}

	@Override
	protected void finalize() throws Throwable {
		clear();
	}
}
