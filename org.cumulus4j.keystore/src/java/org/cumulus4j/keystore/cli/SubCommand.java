package org.cumulus4j.keystore.cli;

import java.io.File;
import java.io.IOException;

import org.cumulus4j.keystore.KeyStore;
import org.kohsuke.args4j.Option;

public abstract class SubCommand
{
	@Option(name="-keyStoreFile", required=true, usage="Specifies the key-store-file to work with.")
	private File keyStoreFile;

	public File getKeyStoreFile() {
		return keyStoreFile;
	}

	@Option(name="-authUserName", required=false, usage="The authenticated user authorizing this action. If the very first user is created, this value is ignored. If omitted, the user will be asked interactively (if required, i.e. if not creating the very first user).")
	private String authUserName;

	public String getAuthUserName() {
		return authUserName;
	}

	@Option(name="-authPassword", required=false, usage="The password for authenticating the user specified by -authUserName. If the very first user is created, this value is ignored. If omitted, the user will be asked interactively (if required, i.e. if not creating the very first user).")
	private String authPassword;

	public String getAuthPassword() {
		return authPassword;
	}

	private KeyStore keyStore;

	public KeyStore getKeyStore() throws IOException
	{
		if (keyStore == null)
			keyStore = new KeyStore(keyStoreFile);

		return keyStore;
	}

	/**
	 * Get the name of the sub-command, i.e. what the user has to write in the command line.
	 * @return the name of the sub-command.
	 */
	public abstract String getSubCommandName();

	/**
	 * Get the description for this sub-command.
	 * @return the description.
	 */
	public abstract String getSubCommandDescription();

	public abstract void run()
	throws Exception;
}
