package org.cumulus4j.keymanager.gwt.client;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

/**
 * Class that provides the functionality needed to modify the password a user
 * entered when logging into or registering at AxEasy, since that password is
 * used solely for authentication at the application server. For security
 * reasons a different password is used to authenticate at the key server.
 * Because a user should only have to use one password we use this method to
 * transform their chosen password and then use the transformed password for the
 * key server.
 */

public class KeyServerPasswordCreator {

	/**
	 * Implement a more secure and sophisticated password transformation using
	 * SHA512 Hashing
	 * 
	 * @return New password for the key server
	 */
	public static String modifyPassword(String password) {

		MessageDigest messageDigest;
		String keyServerPassword = "";

		try {
			messageDigest = MessageDigest.getInstance("SHA-512");
			messageDigest.update(password.getBytes());

			byte[] messageBytes = messageDigest.digest();

			for (int i = 0; i < messageBytes.length; i++) {
				byte temp = messageBytes[i];

				String s = Integer.toHexString(new Byte(temp));

				while (s.length() < 2) {
					s = "0" + s;
				}

				s = s.substring(s.length() - 2);
				keyServerPassword += s;
			}
		} catch (NoSuchAlgorithmException exception) {
			exception.printStackTrace();
		}

		return keyServerPassword;
	}
}
