package org.cumulus4j.keystore.cli;

import org.cumulus4j.keystore.DateDependentKeyStrategy;
import org.cumulus4j.keystore.prop.Long2LongSortedMapProperty;

public class InitDateDependentKeyStrategySubCommand extends SubCommandWithKeyStore
{

	@Override
	public String getSubCommandName() {
		return "initDateDependentKeyStrategy";
	}

	@Override
	public String getSubCommandDescription() {
		return "Create and initialise a key store for the usage with Cumulus4j and the date-dependent key-assignment strategy.";
	}

	@Override
	public void run() throws Exception {
		Long2LongSortedMapProperty prop = getKeyStore().getProperty(
				getAuthUserName(), getAuthPasswordAsCharArray(),
				Long2LongSortedMapProperty.class,
				DateDependentKeyStrategy.PROPERTY_VALID_FROM_TIMESTAMP_2_KEY_ID
		);

	}

}
