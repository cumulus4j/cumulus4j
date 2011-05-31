package org.cumulus4j.keystore.cli;

import org.kohsuke.args4j.Option;

/**
 * @author Marco หงุ่ยตระกูล-Schulze - marco at nightlabs dot de
 */
public class CreateUserSubCommand extends SubCommandWithKeyStore
{
	@Option(name="-userName", required=true, usage="The new user to be created.")
	private String userName;

	@Option(name="-password", required=false, usage="The password of the new user. If omitted, the user will be asked for it interactively.")
	private String password;

	@Override
	public String getSubCommandName() {
		return "createUser";
	}

	@Override
	public String getSubCommandDescription() {
		return "Create a new user by encrypting the master-key with the new user's password.";
	}

	@Override
	public void prepare() throws Exception {
		super.prepare();

		if (password == null)
			password = promptPassword("password: ");
	}

	@Override
	public void run() throws Exception {
		getKeyStore().createUser(getAuthUserName(), getAuthPasswordAsCharArray(), userName, password.toCharArray());
	}
}
