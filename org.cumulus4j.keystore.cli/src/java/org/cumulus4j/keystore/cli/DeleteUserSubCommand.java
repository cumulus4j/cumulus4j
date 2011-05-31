package org.cumulus4j.keystore.cli;

import org.kohsuke.args4j.Option;

/**
 * @author Marco หงุ่ยตระกูล-Schulze - marco at nightlabs dot de
 */
public class DeleteUserSubCommand extends SubCommandWithKeyStoreWithAuth
{
	@Option(name="-userName", required=true, usage="The user to be deleted.")
	private String userName;

	@Override
	public String getSubCommandName() {
		return "deleteUser";
	}

	@Override
	public String getSubCommandDescription() {
		return "Delete the user, i.e. remove the given user's password slot.";
	}

	@Override
	public void run() throws Exception {
		getKeyStore().deleteUser(getAuthUserName(), getAuthPasswordAsCharArray(), userName);
	}
}
