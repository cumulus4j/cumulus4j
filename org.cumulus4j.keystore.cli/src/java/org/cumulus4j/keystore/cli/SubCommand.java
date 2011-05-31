package org.cumulus4j.keystore.cli;

public abstract class SubCommand
{
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

	public void prepare()
	throws Exception
	{

	}

	public abstract void run()
	throws Exception;
}
