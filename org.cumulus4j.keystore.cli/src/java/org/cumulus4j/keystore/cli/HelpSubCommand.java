package org.cumulus4j.keystore.cli;

/**
 * @author Marco หงุ่ยตระกูล-Schulze - marco at nightlabs dot de
 */
public class HelpSubCommand extends SubCommand {

	@Override
	public String getSubCommandName() {
		return "help";
	}

	@Override
	public String getSubCommandDescription() {
		return "Get help.";
	}

	@Override
	public void run() throws Exception {
		throw new UnsupportedOperationException("The help command is handled by the KeyStoreCLI itself.");
	}

}
