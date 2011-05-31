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
	}

	protected String promptPassword(String fmt, Object ... args)
	{
		Console console = System.console();
		if (console == null)
			throw new IllegalStateException("There is no system console! Cannot prompt \"" + String.format(fmt, args) + "\"!!!");

		char[] pw = console.readPassword(fmt, args);
		if (pw == null)
			return null;
		else
			return new String(pw);
	}

}
