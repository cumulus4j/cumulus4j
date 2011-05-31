package org.cumulus4j.keystore.cli;

public class CreateUserSubCommand extends SubCommand
{
	@Override
	public String getSubCommandName() {
		return "createUser";
	}

	@Override
	public String getSubCommandDescription() {
		return "Create a new user by encrypting the master-key with the new user's password.";
	}

	@Override
	public void run() throws Exception {
		// TODO Auto-generated method stub

	}

}
