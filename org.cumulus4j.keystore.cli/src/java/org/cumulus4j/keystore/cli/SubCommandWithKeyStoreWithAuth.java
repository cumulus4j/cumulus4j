package org.cumulus4j.keystore.cli;

import org.kohsuke.args4j.Option;

/**
 * @author Marco หงุ่ยตระกูล-Schulze - marco at nightlabs dot de
 */
public abstract class SubCommandWithKeyStoreWithAuth
extends SubCommandWithKeyStore
{
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

	@Override
	public void prepare() throws Exception
	{
		super.prepare();
		if (authPassword == null && !getKeyStore().isEmpty())
			authPassword = promptPassword("authPassword: ");
	}
}
