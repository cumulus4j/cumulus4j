package org.cumulus4j.keystore.cli;

import java.io.Console;
import java.io.File;

import org.cumulus4j.keystore.KeyStore;
import org.kohsuke.args4j.Option;

/**
 * @author Marco หงุ่ยตระกูล-Schulze - marco at nightlabs dot de
 */
public abstract class SubCommandWithKeyStore
extends SubCommand
{
	@Option(name="-keyStoreFile", required=true, usage="Specifies the key-store-file to work with.")
	private File keyStoreFile;

	public File getKeyStoreFile() {
		return keyStoreFile;
	}

	@Option(name="-authUserName", required=true, usage="The authenticated user authorizing this action. If the very first user is created, this value is ignored.")
	private String authUserName;

	public String getAuthUserName()
	{
		return authUserName;
	}

	@Option(name="-authPassword", required=false, usage="The password for authenticating the user specified by -authUserName. If the very first user is created, this value is ignored. If omitted, the user will be asked interactively (if required, i.e. if not creating the very first user).")
	private String authPassword;

	public char[] getAuthPasswordAsCharArray()
	{
		return authPassword == null ? null : authPassword.toCharArray();
	}

	public String getAuthPassword()
	{
		return authPassword;
	}

	private KeyStore keyStore;

	public KeyStore getKeyStore()
	{
		return keyStore;
	}

	@Override
	public void prepare() throws Exception
	{
		super.prepare();
		keyStore = new KeyStore(keyStoreFile);

		if (authPassword == null && !keyStore.isEmpty())
			authPassword = promptPassword("authPassword: ");
	}

	protected String promptPassword(String fmt, Object ... args)
	{
		Console console = System.console();
		char[] pw = console.readPassword(fmt, args);
		if (pw == null)
			return null;
		else
			return new String(pw);
	}

}
