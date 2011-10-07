/*
 * Cumulus4j - Securing your data in the cloud - http://cumulus4j.org
 * Copyright (C) 2011 NightLabs Consulting GmbH
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.cumulus4j.keystore.test;

import java.io.File;

import org.cumulus4j.keystore.AuthenticationException;
import org.cumulus4j.keystore.CannotDeleteLastUserException;
import org.cumulus4j.keystore.GeneratedKey;
import org.cumulus4j.keystore.KeyStore;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

public class KeyStoreUserTest
{
	private File keyStoreFile;
	private KeyStore keyStore;

	@Before
	public void before()
	throws Exception
	{
		keyStoreFile = File.createTempFile("test-", ".keystore");
		keyStore = new KeyStore(keyStoreFile);
	}

	@After
	public void after()
	throws Exception
	{
		keyStore = null;
		File f = keyStoreFile;
		if (f != null)
			f.delete();
	}

	@Test
	public void createFirstUser()
	throws Exception
	{
		keyStore.createUser(null, null, "marco", "test12345".toCharArray());
	}

	@Test
	public void create2ndUser()
	throws Exception
	{
		keyStore.createUser(null, null, "marco", "test12345".toCharArray());
		keyStore.createUser("marco", "test12345".toCharArray(), "bieber", "test6789".toCharArray());
	}

	@Test(expected=AuthenticationException.class)
	public void create2ndUserWithoutAuthentication()
	throws Exception
	{
		keyStore.createUser(null, null, "marco", "test12345".toCharArray());
		keyStore.createUser(null, null, "bieber", "test6789".toCharArray());
	}

	@Test(expected=AuthenticationException.class)
	public void create2ndUserWithWrongAuthentication()
	throws Exception
	{
		keyStore.createUser(null, null, "marco", "test12345".toCharArray());
		keyStore.createUser("marco", "test00000".toCharArray(), "bieber", "test6789".toCharArray());
	}

	@Test
	public void create2UsersAndDelete1()
	throws Exception
	{
		keyStore.createUser(null, null, "marco", "test12345".toCharArray());
		keyStore.createUser("marco", "test12345".toCharArray(), "bieber", "test6789".toCharArray());
		keyStore.deleteUser("bieber", "test6789".toCharArray(), "marco");
	}

	@Test
	public void create2UsersAndDeleteSelf()
	throws Exception
	{
		keyStore.createUser(null, null, "marco", "test12345".toCharArray());
		keyStore.createUser("marco", "test12345".toCharArray(), "bieber", "test6789".toCharArray());
		keyStore.deleteUser("bieber", "test6789".toCharArray(), "bieber");
	}

	@Test(expected=CannotDeleteLastUserException.class)
	public void create2UsersAndDeleteBoth()
	throws Exception
	{
		keyStore.createUser(null, null, "marco", "test12345".toCharArray());
		keyStore.createUser("marco", "test12345".toCharArray(), "bieber", "test6789".toCharArray());
		keyStore.deleteUser("bieber", "test6789".toCharArray(), "marco");
		keyStore.deleteUser("bieber", "test6789".toCharArray(), "bieber");
	}

	@Test
	public void create2UsersAndChangePasswordOf2nd()
	throws Exception
	{
		String authUserName = "marco";
		char[] authPassword = "test12345".toCharArray();
		keyStore.createUser(null, null, authUserName, authPassword);

		char[] oldPwBieber = "test6789".toCharArray();
		char[] newPwBieber = "xxxNEWxxx".toCharArray();

		keyStore.createUser(authUserName, authPassword, "bieber", oldPwBieber);

		// validate, that the old password can really be used BEFORE changing it.
		keyStore.generateKey("bieber", oldPwBieber);

		keyStore.changeUserPassword(authUserName, authPassword, "bieber", newPwBieber);

		// validate, that the new password can really be used.
		keyStore.generateKey("bieber", newPwBieber);

		// validate, that the old password cannot be used anymore
		boolean loginExceptionThrown = false;
		try {
			keyStore.generateKey("bieber", oldPwBieber);
		} catch (AuthenticationException x) {
			// great!
			loginExceptionThrown = true;
		}
		Assert.assertTrue("Generating a key with the old password succeeded, but it should have failed!", loginExceptionThrown);
	}

	@Test
	public void createUserAndChangeOwnPassword()
	throws Exception
	{
		String authUserName = "marco";
		char[] oldPassword = "test12345".toCharArray();
		char[] newPassword = "000111222".toCharArray();
		keyStore.createUser(null, null, authUserName, oldPassword);

		// validate, that the old password can really be used BEFORE changing it.
		GeneratedKey generatedKey = keyStore.generateKey(authUserName, oldPassword);

		keyStore.changeUserPassword(authUserName, oldPassword, authUserName, newPassword);

		// validate, that the new password can really be used.
		byte[] key = keyStore.getKey(authUserName, newPassword, generatedKey.getKeyID());

		// check, if it is really the same.
		Assert.assertArrayEquals(generatedKey.getKey(), key);

		// validate, that the old password cannot be used anymore
		boolean loginExceptionThrown = false;
		try {
			keyStore.getKey(authUserName, oldPassword, generatedKey.getKeyID());
		} catch (AuthenticationException x) {
			// great!
			loginExceptionThrown = true;
		}
		Assert.assertTrue("Getting a key with the old password succeeded, but it should have failed!", loginExceptionThrown);
	}
}
