package org.cumulus4j.keystore.cli;

public class InfoSubCommand extends SubCommandWithKeyStore {

	@Override
	public String getSubCommandName() {
		return "info";
	}

	@Override
	public String getSubCommandDescription() {
		return "Display infos about the key-store.";
	}

	@Override
	public void run() throws Exception {
		// TODO Auto-generated method stub

	}

}
