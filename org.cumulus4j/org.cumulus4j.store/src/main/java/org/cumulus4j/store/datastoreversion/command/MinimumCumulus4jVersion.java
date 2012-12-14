package org.cumulus4j.store.datastoreversion.command;

import org.cumulus4j.store.datastoreversion.AbstractDatastoreVersionCommand;
import org.cumulus4j.store.datastoreversion.CommandApplyParam;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class MinimumCumulus4jVersion extends AbstractDatastoreVersionCommand {
	private static final Logger logger = LoggerFactory.getLogger(MinimumCumulus4jVersion.class);

	private int version = -1;

	/**
	 * {@inheritDoc}
	 * <p>
	 * The version returned by the implementation of this method in {@link MinimumCumulus4jVersion} is the
	 * lowest version of cumulus4j that runs with the current datastore.
	 * <p>
	 * In other words: This version here must be set to the current Cumulus4j version,
	 * if a lower version of Cumulus4j cannot be used with the datastore structure represented by the current source code,
	 * anymore.
	 * <p>
	 * This version is composed of the following parts:
	 * <ul>
	 * <li>major: 1 or 2 digits
	 * <li>minor: 2 digits
	 * <li>release: 2 digits
	 * <li>serial: 3 digits
	 * </ul>
	 * <p>
	 * For example, the 3rd non-downgradable change to the version <i>9.08.07-SNAPSHOT</i> would result in the numeric
	 * result <i>90807002</i> (the serial starts at 0, hence 3rd change = 2).
	 * This number is not incremented at the release of 9.08.07! It is only incremented, if
	 * the data structure changes in a way that prevents older versions from operating!
	 */
	@Override
	public int getCommandVersion() {
		if (this.version >= 0)
			return this.version;

		// BEGIN: maintain this - i.e. manually update the following!!!
		// Last updated: 2012-07-20 by Marco
		// Current version in pom.xml: 1.1.0-SNAPSHOT
		int major   = getCommandVersionMajor();
		int minor   = getCommandVersionMinor();
		int release = getCommandVersionRelease();
		int serial  = getCommandVersionSerial();
		// END

		// Calculate version number from elements.
		int version = (
				major * 100 /*minor*/ * 100 /*release*/ * 1000 /*serial*/ +
				              minor   * 100 /*release*/ * 1000 /*serial*/ +
				                              release   * 1000 /*serial*/ +
				                                                 serial
		);
		logger.info("version={}", version);
		this.version = version;
		return version;
	}

	public int getCommandVersionMajor() {
		return  01;
	}

	public int getCommandVersionMinor() {
		return  01;
	}

	public int getCommandVersionRelease() {
		return  00;
	}

	public int getCommandVersionSerial() {
		return 001;
	}

	@Override
	public boolean isFinal() {
		return false;
	}

	@Override
	public void apply(CommandApplyParam commandApplyParam) {
		// nothing - this command is only used to have a general version and to ensure a minimum database version.
	}

	public static void main(String[] args) {
		System.out.println(new MinimumCumulus4jVersion().getCommandVersion());
	}

}
