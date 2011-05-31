package org.cumulus4j.keystore.cli;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.cumulus4j.keystore.KeyStore;
import org.kohsuke.args4j.CmdLineException;
import org.kohsuke.args4j.CmdLineParser;

/**
 * Command line tool for the key store.
 *
 * @author Marco หงุ่ยตระกูล-Schulze - marco at nightlabs dot de
 */
public class KeyStoreCLI
{
	public static final List<Class<? extends SubCommand>> subCommandClasses;
	static {
		ArrayList<Class<? extends SubCommand>> l = new ArrayList<Class<? extends SubCommand>>();

		l.add(CreateUserSubCommand.class);
		l.add(HelpSubCommand.class);
		l.add(InfoSubCommand.class);
		l.add(VersionSubCommand.class);

		l.trimToSize();
		subCommandClasses = Collections.unmodifiableList(l);
	};

	public static final List<SubCommand> subCommands;
	public static final Map<String, SubCommand> subCommandName2subCommand;
	static {
		try {
			ArrayList<SubCommand> l = new ArrayList<SubCommand>();
			Map<String, SubCommand> m = new HashMap<String, SubCommand>();
			for (Class<? extends SubCommand> c : subCommandClasses) {
				SubCommand subCommand = c.newInstance();
				l.add(subCommand);
				m.put(subCommand.getSubCommandName(), subCommand);
			}

			l.trimToSize();
			subCommands = Collections.unmodifiableList(l);
			subCommandName2subCommand = Collections.unmodifiableMap(m);
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	private static final String CMD_PREFIX = "java -jar org.cumulus4j.keystore-VERSION.one-jar.jar";

	private static final String[] stripSubCommand(String[] args)
	{
		String[] result = new String[args.length - 1];
		for (int i = 0; i < result.length; i++)
			result[i] = args[i + 1];

		return result;
	}

	/**
	 * Main method providing a command line interface (CLI) to the {@link KeyStore}.
	 *
	 * @param args the program arguments.
	 */
	public static void main(String[] args)
	{
		int programExitStatus = 1;
		boolean displayHelp = true;
		String subCommandName = null;
		SubCommand subCommand = null;

		if (args.length > 0) {
			subCommandName = args[0];

			if ("help".equals(subCommandName)) {
				if (args.length > 1) {
					subCommandName = args[1];
					subCommand = subCommandName2subCommand.get(subCommandName);
					if (subCommand == null) {
						System.err.println("Unknown sub-command: " + subCommandName);
						subCommandName = null;
					}
				}
			}
			else {
				subCommand = subCommandName2subCommand.get(subCommandName);
				if (subCommand == null) {
					System.err.println("Unknown sub-command: " + subCommandName);
					subCommandName = null;
				}
				else {
					displayHelp = false;

					CmdLineParser parser = new CmdLineParser(subCommand);
					try {
						String[] argsWithoutSubCommand = stripSubCommand(args);
						parser.parseArgument(argsWithoutSubCommand);
						subCommand.prepare();
						subCommand.run();
						programExitStatus = 0;
					} catch (CmdLineException e) {
						// handling of wrong arguments
						programExitStatus = 2;
						displayHelp = true;
						System.err.println("Error: " + e.getMessage());
						System.err.println();
					} catch (Exception x) {
						programExitStatus = 3;
						x.printStackTrace();
					}
				}
			}
		}

		if (displayHelp) {
			if (subCommand == null) {
				System.err.println("Syntax: " + CMD_PREFIX + " <sub-command> <options>");
				System.err.println();
				System.err.println("Get help for a specific sub-command: " + CMD_PREFIX + " help <sub-command>");
				System.err.println();
				System.err.println("Available sub-commands:");
				for (SubCommand sc : subCommands) {
					System.err.println("  " + sc.getSubCommandName());
				}
			}
			else {
				CmdLineParser parser = new CmdLineParser(subCommand);
				System.err.println(subCommand.getSubCommandName() + ": " + subCommand.getSubCommandDescription());
				System.err.println();
				System.err.print("Syntax: " + CMD_PREFIX + " " + subCommand.getSubCommandName());
				parser.printSingleLineUsage(System.err);
				System.err.println();
				System.err.println();
				System.err.println("Options:");
				parser.printUsage(System.err);
			}
		}

		System.exit(programExitStatus);
	}

}
