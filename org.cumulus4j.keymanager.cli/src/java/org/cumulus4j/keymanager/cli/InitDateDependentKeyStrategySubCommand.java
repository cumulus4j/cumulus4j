/*
 * Cumulus4j - Securing your data in the cloud - http://cumulus4j.org
 * Copyright (C) 2011 NightLabs Consulting GmbH
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.cumulus4j.keymanager.cli;

import org.cumulus4j.keymanager.api.DateDependentKeyStrategyInitParam;
import org.cumulus4j.keymanager.api.DateDependentKeyStrategyInitResult;
import org.cumulus4j.keymanager.api.KeyManagerAPIConfiguration;
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
extends SubCommandWithKeyManagerAPI
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

		KeyManagerAPIConfiguration configuration = new KeyManagerAPIConfiguration(getKeyManagerAPI().getConfiguration());
		configuration.setAuthUserName(userName);
		configuration.setAuthPassword(password == null ? null : password.toCharArray());
		getKeyManagerAPI().setConfiguration(configuration);
	}

	@Override
	public void run() throws Exception {
		DateDependentKeyStrategyInitParam param = new DateDependentKeyStrategyInitParam();
		param.setKeyActivityPeriodMSec(keyActivityPeriodMSec);
		param.setKeyStorePeriodMSec(keyStorePeriodMSec);
		DateDependentKeyStrategyInitResult result = getKeyManagerAPI().initDateDependentKeyStrategy(param);
		System.out.println("Generated " + result.getGeneratedKeyCount() + " keys.");
	}

}
