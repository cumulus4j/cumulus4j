package org.cumulus4j.keystore.cli;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;

import org.cumulus4j.keystore.KeyStore;
import org.kohsuke.args4j.Option;

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
			authPassword = promptPassword("authPassword");
	}

	protected String promptPassword(String display)
	{
		System.err.print(display + ": ");
		try {
			return new BufferedReader(new InputStreamReader(System.in)).readLine();
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}

}
