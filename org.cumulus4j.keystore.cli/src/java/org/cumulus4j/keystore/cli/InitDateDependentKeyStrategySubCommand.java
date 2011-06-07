package org.cumulus4j.keystore.cli;

import java.util.SortedSet;

import org.cumulus4j.keystore.DateDependentKeyStrategy;
import org.cumulus4j.keystore.KeyStore;
import org.kohsuke.args4j.Option;

/**
 * <p>
 * {@link SubCommand} implementation for creating & initialising a new key-store with the {@link DateDependentKeyStrategy}.
 * </p>
 * @author Marco หงุ่ยตระกูล-Schulze - marco at nightlabs dot de
 */
public class InitDateDependentKeyStrategySubCommand
extends SubCommandWithKeyStore
{
	@Option(
			name="-userName", required=true,
			usage="The first user, which is automatically created when initialising the key store."
	)
	private String userName;

	@Option(
			name="-password", required=false,
			usage="The password of the first user. If omitted, the user will be asked for it interactively."
	)
	private String password;

	@Option(
			name="-keyActivityPeriod", required=false, handler=TimePeriodOptionHandler.class,
			usage="How long should each key be valid. This must be a positive number followed by a unit symbol " +
					"(ms = millisecond, s = second, min = minute, h = hour, d = day, a = y = year). " +
					"If omitted, the default value '24h' will be used."
	)
	private long keyActivityPeriodMSec;

	@Option(
			name="-keyStorePeriod", required=false, handler=TimePeriodOptionHandler.class,
			usage="How long should the key store have fresh, unused keys. This number divided by the 'keyActivityPeriodMSec' " +
					"determines, how many keys must be generated. This must be a positive number followed by a unit symbol " +
					"(ms = millisecond, s = second, min = minute, h = hour, d = day, a = y = year). If omitted, the default value '50a' will be used.")
	private long keyStorePeriodMSec;

	@Option(
			name="-keySize", required=false,
			usage="Set the key size of all generated keys (including the master-key). This is synonymous to the system property '" +
					KeyStore.SYSTEM_PROPERTY_KEY_SIZE + "'. If both are present, this overwrites the system property."
	)
	private int keySize = -1;

	@Option(
			name="-encryptionAlgorithm", required=false,
			usage="Set the encryption algorithm to be used. This is synonymous to the system property '" +
					KeyStore.SYSTEM_PROPERTY_ENCRYPTION_ALGORITHM + "'. If both are present, this overwrites the system property."
	)
	private String encryptionAlgorithm;

	@Override
	public String getSubCommandName() {
		return "initDateDependentKeyStrategy";
	}

	@Override
	public String getSubCommandDescription() {
		return "Create and initialise a key store for the usage with Cumulus4j and the date-dependent key-assignment strategy.";
	}

	@Override
	public void prepare() throws Exception {
		super.prepare();

		if (password == null)
			password = promptPassword("password: ");

		if (keySize > 0)
			System.setProperty(KeyStore.SYSTEM_PROPERTY_KEY_SIZE, String.valueOf(keySize));

		if (encryptionAlgorithm != null)
			System.setProperty(KeyStore.SYSTEM_PROPERTY_ENCRYPTION_ALGORITHM, encryptionAlgorithm);
	}

	@Override
	public void run() throws Exception {
		DateDependentKeyStrategy strategy = new DateDependentKeyStrategy(getKeyStore());
		strategy.init(userName, password.toCharArray(), keyActivityPeriodMSec, keyStorePeriodMSec);
		SortedSet<Long> keyIDs = getKeyStore().getKeyIDs(userName, password.toCharArray());
		System.out.println("Generated " + keyIDs.size() + " keys.");
	}

}
