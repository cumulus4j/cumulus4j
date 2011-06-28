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
package org.cumulus4j.store.crypto;

import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;

import org.datanucleus.NucleusContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * <p>
 * Abstract base-class for implementing {@link CryptoManager}s.
 * </p>
 * <p>
 * This class already implements a mechanism to close expired {@link CryptoSession}s
 * periodically (see {@link #getCryptoSessionExpiryAgeMSec()} and {@link #getCryptoSessionExpiryTimerPeriodMSec()}).
 * </p>
 *
 * @author Marco หงุ่ยตระกูล-Schulze - marco at nightlabs dot de
 */
public abstract class AbstractCryptoManager implements CryptoManager
{
	private static final Logger logger = LoggerFactory.getLogger(AbstractCryptoManager.class);

	private CryptoManagerRegistry cryptoManagerRegistry;

	private String cryptoManagerID;

	private Map<String, CryptoSession> id2session = new HashMap<String, CryptoSession>();

	private static volatile Timer closeExpiredSessionsTimer;
	private static volatile boolean closeExpiredSessionsTimerInitialised = false;
	private volatile boolean closeExpiredSessionsTaskInitialised = false;

	private class CloseExpiredSessionsTask extends TimerTask
	{
		private final Logger logger = LoggerFactory.getLogger(CloseExpiredSessionsTask.class);

		private final long expiryTimerPeriodMSec;

		public CloseExpiredSessionsTask(long expiryTimerPeriodMSec) {
			this.expiryTimerPeriodMSec = expiryTimerPeriodMSec;
		}

		@Override
		public void run() {
			logger.debug("run: entered");
			closeExpiredCryptoSessions(true);

			long currentPeriodMSec = getCryptoSessionExpiryTimerPeriodMSec();
			if (currentPeriodMSec != expiryTimerPeriodMSec) {
				logger.info(
						"run: The expiryTimerPeriodMSec changed (oldValue={}, newValue={}). Re-scheduling this task.",
						expiryTimerPeriodMSec, currentPeriodMSec
				);
				this.cancel();

				closeExpiredSessionsTimer.schedule(new CloseExpiredSessionsTask(currentPeriodMSec), currentPeriodMSec, currentPeriodMSec);
			}
		}
	};

	/**
	 * <p>
	 * Persistence property to control when the timer for cleaning up expired {@link CryptoSession}s is called. The
	 * value configured here is a period, i.e. the timer will be triggered every X ms (roughly).
	 * </p><p>
	 * If this persistence property is not present (or not a valid number), the default is 60000 (1 minute), which means
	 * the timer will wake up once a minute and call {@link #closeExpiredCryptoSessions(boolean)} with <code>force = true</code>.
	 * </p><p>
	 * If this persistence property is set to 0, the timer is deactivated and cleanup happens only synchronously
	 * when {@link #getCryptoSession(String)} is called (periodically - not every time this method is called).
	 * </p>
	 */
	public static final String PROPERTY_CRYPTO_SESSION_EXPIRY_TIMER_PERIOD_MSEC = "cumulus4j.cryptoSessionExpiryTimerPeriodMSec";

	private long cryptoSessionExpiryTimerPeriodMSec = Long.MIN_VALUE;

	/**
	 * <p>
	 * Persistence property to control after which time an unused {@link CryptoSession} expires.
	 * </p><p>
	 * <code>CryptoSession</code>s that are unused for the configured time in milliseconds are considered expired and
	 * either periodically removed by a timer (see property {@value #PROPERTY_CRYPTO_SESSION_EXPIRY_TIMER_PERIOD_MSEC})
	 * or periodically removed synchronously during a call to {@link #getCryptoSession(String)}.
	 * </p><p>
	 * If this property is not present (or not a valid number), the default value is 1800000 (30 minutes).
	 * </p>
	 */
	public static final String PROPERTY_CRYPTO_SESSION_EXPIRY_AGE_MSEC = "cumulus4j.cryptoSessionExpiryAgeMSec";

	private long cryptoSessionExpiryAgeMSec = Long.MIN_VALUE;

	/**
	 * <p>
	 * Get the period in which expired crypto sessions are searched and closed.
	 * </p>
	 * <p>
	 * This value can be configured using the persistence property {@value #PROPERTY_CRYPTO_SESSION_EXPIRY_TIMER_PERIOD_MSEC}.
	 * A value of 0 means to deactivate the timer. In this case, only periodic cleanup during the {@link #getCryptoSession(String)}
	 * occurs.
	 * </p>
	 *
	 * @return the period in msec.
	 */
	protected long getCryptoSessionExpiryTimerPeriodMSec()
	{
		long val = cryptoSessionExpiryTimerPeriodMSec;
		if (val == Long.MIN_VALUE) {
			String propName = PROPERTY_CRYPTO_SESSION_EXPIRY_TIMER_PERIOD_MSEC;
			String propVal = (String) getCryptoManagerRegistry().getNucleusContext().getPersistenceConfiguration().getProperty(propName);
			if (propVal != null && !propVal.trim().isEmpty()) {
				try {
					val = Long.parseLong(propVal.trim());
					logger.info("getCryptoSessionExpiryTimerPeriodMSec: Property '{}' is set to {} ms.", propName, val);
				} catch (NumberFormatException x) {
					logger.warn("getCryptoSessionExpiryTimerPeriodMSec: Property '{}' is set to '{}', which is an ILLEGAL value (no valid number). Falling back to default value.", propName, propVal);
				}
			}

			if (val == Long.MIN_VALUE) {
				val = 60000L;
				logger.info("getCryptoSessionExpiryTimerPeriodMSec: Property '{}' is not set. Using default value {}.", propName, val);
			}

			cryptoSessionExpiryTimerPeriodMSec = val;
		}
		return val;
	}

	/**
	 * <p>
	 * Get the age after which an unused session expires.
	 * </p>
	 * <p>
	 * A {@link CryptoSession} expires when its {@link CryptoSession#getLastUsageTimestamp() lastUsageTimestamp}
	 * is longer in the past than this expiry age. Note, that the session might be kept longer, because a
	 * timer checks {@link #getCryptoSessionExpiryTimerPeriodMSec() periodically} for expired sessions.
	 * </p>
	 *
	 * @return the expiry age (of non-usage-time) in msec, after which the session should be closed.
	 */
	protected long getCryptoSessionExpiryAgeMSec()
	{
		long val = cryptoSessionExpiryAgeMSec;
		if (val == Long.MIN_VALUE) {
			String propName = PROPERTY_CRYPTO_SESSION_EXPIRY_AGE_MSEC;
			String propVal = (String) getCryptoManagerRegistry().getNucleusContext().getPersistenceConfiguration().getProperty(propName);
			if (propVal != null && !propVal.trim().isEmpty()) {
				try {
					val = Long.parseLong(propVal.trim());
					logger.info("getCryptoSessionExpiryAgeMSec: Property '{}' is set to {} ms.", propName, val);
				} catch (NumberFormatException x) {
					logger.warn("getCryptoSessionExpiryAgeMSec: Property '{}' is set to '{}', which is an ILLEGAL value (no valid number). Falling back to default value.", propName, propVal);
				}
			}

			if (val == Long.MIN_VALUE) {
				val =  30L * 60000L;
				logger.info("getCryptoSessionExpiryAgeMSec: Property '{}' is not set. Using default value {}.", propName, val);
			}

			cryptoSessionExpiryAgeMSec = val;
		}
		return val;
	}

	private Date lastCloseExpiredCryptoSessionsTimestamp = null;

	/**
	 * Closes expired {@link CryptoSession}s. If <code>force == false</code>, it does so only periodically.
	 *
	 * @param force whether to force the cleanup now or only do it periodically.
	 */
	protected void closeExpiredCryptoSessions(boolean force)
	{
		synchronized (this) {
			if (
					!force && (
							lastCloseExpiredCryptoSessionsTimestamp != null && lastCloseExpiredCryptoSessionsTimestamp.after(new Date(System.currentTimeMillis() - getCryptoSessionExpiryAgeMSec()))
					)
			)
			{
				logger.trace("closeExpiredCryptoSessions: force == false and period not yet elapsed. Skipping.");
				return;
			}

			lastCloseExpiredCryptoSessionsTimestamp = new Date();
		}

		Date closeSessionsBeforeThisTimestamp = new Date(
				System.currentTimeMillis() - getCryptoSessionExpiryAgeMSec()
				- 60000L // additional buffer, preventing the implicit closing here and the getCryptoSession(...) method getting into a collision
		);

		CryptoSession[] sessions;
		synchronized (id2session) {
			sessions = id2session.values().toArray(new CryptoSession[id2session.size()]);
		}

		for (CryptoSession session : sessions) {
			if (session.getLastUsageTimestamp().before(closeSessionsBeforeThisTimestamp)) {
				logger.debug("closeExpiredCryptoSessions: Closing expired session: " + session);
				session.close();
			}
		}
	}


	@Override
	public CryptoManagerRegistry getCryptoManagerRegistry() {
		return cryptoManagerRegistry;
	}

	@Override
	public void setCryptoManagerRegistry(CryptoManagerRegistry cryptoManagerRegistry) {
		this.cryptoManagerRegistry = cryptoManagerRegistry;
	}

	@Override
	public String getCryptoManagerID() {
		return cryptoManagerID;
	}

	@Override
	public void setCryptoManagerID(String cryptoManagerID)
	{
		if (cryptoManagerID == null)
			throw new IllegalArgumentException("cryptoManagerID == null");

		if (cryptoManagerID.equals(this.cryptoManagerID))
			return;

		if (this.cryptoManagerID != null)
			throw new IllegalStateException("this.keyManagerID is already assigned and cannot be modified!");

		this.cryptoManagerID = cryptoManagerID;
	}

	/**
	 * <p>
	 * Create a new instance of a class implementing {@link CryptoSession}.
	 * </p>
	 * <p>
	 * This method is called by {@link #getCryptoSession(String)}, if it needs a new <code>CryptoSession</code> instance.
	 * </p>
	 * <p>
	 * Implementors should simply instantiate and return their implementation of
	 * <code>CryptoSession</code>. It is not necessary to call {@link CryptoSession#setCryptoSessionID(String)}
	 * and the like here - this is automatically done afterwards by {@link #getCryptoSession(String)}.
	 * </p>
	 *
	 * @return the new {@link CryptoSession} instance.
	 */
	protected abstract CryptoSession createCryptoSession();

	@Override
	public CryptoSession getCryptoSession(String cryptoSessionID)
	{
		if (!closeExpiredSessionsTimerInitialised) {
			synchronized (AbstractCryptoManager.class) {
				if (!closeExpiredSessionsTimerInitialised) {
					if (getCryptoSessionExpiryTimerPeriodMSec() > 0)
						closeExpiredSessionsTimer = new Timer();
					else
						closeExpiredSessionsTimer = null;

					closeExpiredSessionsTimerInitialised = true;
				}
			}
		}

		if (!closeExpiredSessionsTaskInitialised) {
			synchronized (this) {
				if (!closeExpiredSessionsTaskInitialised) {
					if (closeExpiredSessionsTimer != null) {
						long periodMSec = getCryptoSessionExpiryTimerPeriodMSec();
						closeExpiredSessionsTimer.schedule(new CloseExpiredSessionsTask(periodMSec), periodMSec, periodMSec);
					}
					closeExpiredSessionsTaskInitialised = true;
				}
			}
		}

		CryptoSession session = null;
		do {
			synchronized (id2session) {
				session = id2session.get(cryptoSessionID);
				if (session == null) {
					session = createCryptoSession();
					if (session == null)
						throw new IllegalStateException("Implementation error! " + this.getClass().getName() + ".createSession() returned null!");

					session.setCryptoManager(this);
					session.setCryptoSessionID(cryptoSessionID);

					id2session.put(cryptoSessionID, session);
				}
			}

			// The following code tries to prevent the situation that a CryptoSession is returned which is right
			// now simultaneously being closed by the CloseExpiredSessionsTask (the timer above).
			Date sessionExpiredBeforeThisTimestamp = new Date(System.currentTimeMillis() - getCryptoSessionExpiryAgeMSec());
			if (session.getLastUsageTimestamp().before(sessionExpiredBeforeThisTimestamp)) {
				logger.info("getCryptoSession: CryptoSession cryptoSessionID=\"{}\" already expired. Closing it now and repeating lookup.", cryptoSessionID);

				// cause creation of a new session
				session.close();
				session = null;
			}

		} while (session == null);

		session.updateLastUsageTimestamp();

		if (closeExpiredSessionsTimer == null) {
			logger.trace("getCryptoSession: No timer enabled => calling closeExpiredCryptoSessions(false) now.");
			closeExpiredCryptoSessions(false);
		}

		return session;
	}

	@Override
	public void onCloseCryptoSession(CryptoSession cryptoSession)
	{
		synchronized (id2session) {
			id2session.remove(cryptoSession.getCryptoSessionID());
		}
	}

	@Override
	public String getEncryptionAlgorithm()
	{
		String ea = encryptionAlgorithm;

		if (ea == null) {
			NucleusContext nucleusContext = getCryptoManagerRegistry().getNucleusContext();
			if (nucleusContext == null)
				throw new IllegalStateException("NucleusContext already garbage-collected!");

			String encryptionAlgorithmPropName = PROPERTY_ENCRYPTION_ALGORITHM;
			String encryptionAlgorithmPropValue = (String) nucleusContext.getPersistenceConfiguration().getProperty(encryptionAlgorithmPropName);
			if (encryptionAlgorithmPropValue == null || encryptionAlgorithmPropValue.trim().isEmpty()) {
				ea = "Twofish/GCM/NoPadding"; // default value, if the property was not defined.
//				ea = "Twofish/CBC/PKCS5Padding"; // default value, if the property was not defined.
//				ea = "AES/CBC/PKCS5Padding"; // default value, if the property was not defined.
//				ea = "AES/CFB/NoPadding"; // default value, if the property was not defined.
				logger.info("getEncryptionAlgorithm: Property '{}' is not set. Using default algorithm '{}'.", encryptionAlgorithmPropName, ea);
			}
			else {
				ea = encryptionAlgorithmPropValue.trim();
				logger.info("getEncryptionAlgorithm: Property '{}' is set to '{}'. Using this encryption algorithm.", encryptionAlgorithmPropName, ea);
			}
			ea = ea.toUpperCase(Locale.ENGLISH);
			encryptionAlgorithm = ea;
		}

		return ea;
	}
	private String encryptionAlgorithm = null;

	@Override
	public String getMacAlgorithm()
	{
		String ma = macAlgorithm;

		if (ma == null) {
			NucleusContext nucleusContext = getCryptoManagerRegistry().getNucleusContext();
			if (nucleusContext == null)
				throw new IllegalStateException("NucleusContext already garbage-collected!");

			String macAlgorithmPropName = PROPERTY_MAC_ALGORITHM;
			String macAlgorithmPropValue = (String) nucleusContext.getPersistenceConfiguration().getProperty(macAlgorithmPropName);
			if (macAlgorithmPropValue == null || macAlgorithmPropValue.trim().isEmpty()) {
				ma = MAC_ALGORITHM_NONE; // default value, if the property was not defined.
//				ma = "HMAC-SHA1";
				logger.info("getMacAlgorithm: Property '{}' is not set. Using default MAC algorithm '{}'.", macAlgorithmPropName, ma);
			}
			else {
				ma = macAlgorithmPropValue.trim();
				logger.info("getMacAlgorithm: Property '{}' is set to '{}'. Using this MAC algorithm.", macAlgorithmPropName, ma);
			}
			ma = ma.toUpperCase(Locale.ENGLISH);
			macAlgorithm = ma;
		}

		return ma;
	}
	private String macAlgorithm = null;
}
