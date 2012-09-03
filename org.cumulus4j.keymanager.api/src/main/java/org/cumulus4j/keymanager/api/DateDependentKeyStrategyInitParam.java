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
package org.cumulus4j.keymanager.api;

import java.io.Serializable;

/**
 * Options controlling how <code>org.cumulus4j.keymanager.DateDependentKeyStrategy.init(...)</code>
 * should behave.
 *
 * @author Marco หงุ่ยตระกูล-Schulze - marco at nightlabs dot de
 */
public class DateDependentKeyStrategyInitParam
implements Serializable
{
	private static final long serialVersionUID = 1L;

	private long keyActivityPeriodMSec;

	private long keyStorePeriodMSec;

	/**
	 * Get the time (in millisec) how long each key should be valid. If &lt; 1, the
	 * default value of 24 hours (= 86400000 msec) will be used.
	 * @return How long (in millisec) should each key be valid? A value &lt; 1 means to use the default.
	 */
	public long getKeyActivityPeriodMSec() {
		return keyActivityPeriodMSec;
	}
	/**
	 * @param keyActivityPeriodMSec how long (in millisec) should each key be valid. If &lt; 1, the
	 * default value of 24 hours (= 86400000 msec) will be used.
	 */
	public void setKeyActivityPeriodMSec(long keyActivityPeriodMSec) {
		this.keyActivityPeriodMSec = keyActivityPeriodMSec;
	}

	/**
	 * How long (in millisec) should the key store have fresh, unused keys? This number
	 * divided by the <code>keyActivityPeriodMSec</code> determines, how many keys must be generated.
	 * If &lt; 1, the default value of 50 years (50 * 365 days - ignoring leap years!) will be used.
	 * @return How long (in millisec) should the key store have fresh, unused keys?
	 * A value &lt; 1 means to use the default.
	 */
	public long getKeyStorePeriodMSec() {
		return keyStorePeriodMSec;
	}
	/**
	 * @param keyStorePeriodMSec how long should the key store have fresh, unused keys. This number
	 * divided by the <code>keyActivityPeriodMSec</code> determines, how many keys must be generated.
	 * If &lt; 1, the default value of 50 years (50 * 365 days - ignoring leap years!) will be used.
	 */
	public void setKeyStorePeriodMSec(long keyStorePeriodMSec) {
		this.keyStorePeriodMSec = keyStorePeriodMSec;
	}
}
