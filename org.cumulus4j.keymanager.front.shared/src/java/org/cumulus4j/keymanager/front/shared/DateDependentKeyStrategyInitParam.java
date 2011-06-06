package org.cumulus4j.keymanager.front.shared;

import java.io.Serializable;

import javax.xml.bind.annotation.XmlRootElement;

/**
 * Options controlling how <code>org.cumulus4j.keymanager.DateDependentKeyStrategy.init(...)</code>
 * should behave.
 * @author Marco หงุ่ยตระกูล-Schulze - marco at nightlabs dot de
 */
@XmlRootElement
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
