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

import java.lang.ref.WeakReference;
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
 * periodically (see {@link #getCryptoSessionExpiryAge()} and {@link #getCryptoSessionExpiryTimerPeriod()}).
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

	private static volatile Timer closeExpiredSessionsTimer = null;
	private static volatile boolean closeExpiredSessionsTimerInitialised = false;
	private volatile boolean closeExpiredSessionsTaskInitialised = false;

	private static class CloseExpiredSessionsTask extends TimerTask
	{
		private final Logger logger = LoggerFactory.getLogger(CloseExpiredSessionsTask.class);

		private WeakReference<AbstractCryptoManager> abstractCryptoManagerRef;
		private final long expiryTimerPeriodMSec;

		public CloseExpiredSessionsTask(AbstractCryptoManager abstractCryptoManager, long expiryTimerPeriodMSec)
		{
			if (abstractCryptoManager == null)
				throw new IllegalArgumentException("abstractCryptoManager == null");

			this.abstractCryptoManagerRef = new WeakReference<AbstractCryptoManager>(abstractCryptoManager);
			this.expiryTimerPeriodMSec = expiryTimerPeriodMSec;
		}

		@Override
		public void run() {
			try {
				logger.debug("run: entered");
				final AbstractCryptoManager abstractCryptoManager = abstractCryptoManagerRef.get();
				if (abstractCryptoManager == null) {
					logger.info("run: AbstractCryptoManager was garbage-collected. Cancelling this TimerTask.");
					this.cancel();
					return;
				}

				abstractCryptoManager.closeExpiredCryptoSessions(true);

				long currentPeriodMSec = abstractCryptoManager.getCryptoSessionExpiryTimerPeriod();
				if (currentPeriodMSec != expiryTimerPeriodMSec) {
					logger.info(
							"run: The expiryTimerPeriodMSec changed (oldValue={}, newValue={}). Re-scheduling this task.",
							expiryTimerPeriodMSec, currentPeriodMSec
					);
					this.cancel();

					closeExpiredSessionsTimer.schedule(new CloseExpiredSessionsTask(abstractCryptoManager, currentPeriodMSec), currentPeriodMSec, currentPeriodMSec);
				}
			} catch (Throwable x) {
				// The TimerThread is cancelled, if a task throws an exception. Furthermore, they are not logged at all.
				// Since we do not want the TimerThread to die, we catch everything (Throwable - not only Exception) and log
				// it here. IMHO there's nothing better we can do. Marco :-)
				logger.error("run: " + x, x);
			}
		}
	};

	private long cryptoSessionExpiryTimerPeriod = Long.MIN_VALUE;

	private Boolean cryptoSessionExpiryTimerEnabled = null;

	private long cryptoSessionExpiryAge = Long.MIN_VALUE;

	/**
	 * <p>
	 * Get the period in which expired crypto sessions are searched and closed.
	 * </p>
	 * <p>
	 * This value can be configured using the persistence property {@value CryptoManager#PROPERTY_CRYPTO_SESSION_EXPIRY_TIMER_PERIOD}.
	 * </p>
	 *
	 * @return the period in milliseconds.
	 * @see CryptoManager#PROPERTY_CRYPTO_SESSION_EXPIRY_TIMER_PERIOD
	 * @see CryptoManager#PROPERTY_CRYPTO_SESSION_EXPIRY_TIMER_ENABLED
	 */
	protected long getCryptoSessionExpiryTimerPeriod()
	{
		long val = cryptoSessionExpiryTimerPeriod;
		if (val == Long.MIN_VALUE) {
			String propName = PROPERTY_CRYPTO_SESSION_EXPIRY_TIMER_PERIOD;
			String propVal = (String) getCryptoManagerRegistry().getNucleusContext().getPersistenceConfiguration().getProperty(propName);
			propVal = propVal == null ? null : propVal.trim();
			if (propVal != null && !propVal.isEmpty()) {
				try {
					val = Long.parseLong(propVal);
					if (val <= 0) {
						logger.warn("getCryptoSessionExpiryTimerPeriod: Property '{}' is set to '{}', which is an ILLEGAL value (<= 0). Falling back to default value.", propName, propVal);
						val = Long.MIN_VALUE;
					}
					else
						logger.info("getCryptoSessionExpiryTimerPeriod: Property '{}' is set to {} ms.", propName, val);
				} catch (NumberFormatException x) {
					logger.warn("getCryptoSessionExpiryTimerPeriod: Property '{}' is set to '{}', which is an ILLEGAL value (no valid number). Falling back to default value.", propName, propVal);
				}
			}

			if (val == Long.MIN_VALUE) {
				val = 60000L;
				logger.info("getCryptoSessionExpiryTimerPeriod: Property '{}' is not set. Using default value {}.", propName, val);
			}

			cryptoSessionExpiryTimerPeriod = val;
		}
		return val;
	}

	/**
	 * <p>
	 * Get the enabled status of the timer used to cleanup.
	 * </p>
	 * <p>
	 * This value can be configured using the persistence property {@value CryptoManager#PROPERTY_CRYPTO_SESSION_EXPIRY_TIMER_ENABLED}.
	 * </p>
	 *
	 * @return the enabled status.
	 * @see CryptoManager#PROPERTY_CRYPTO_SESSION_EXPIRY_TIMER_ENABLED
	 * @see CryptoManager#PROPERTY_CRYPTO_SESSION_EXPIRY_TIMER_PERIOD
	 */
	protected boolean getCryptoSessionExpiryTimerEnabled()
	{
		Boolean val = cryptoSessionExpiryTimerEnabled;
		if (val == null) {
			String propName = PROPERTY_CRYPTO_SESSION_EXPIRY_TIMER_ENABLED;
			String propVal = (String) getCryptoManagerRegistry().getNucleusContext().getPersistenceConfiguration().getProperty(propName);
			propVal = propVal == null ? null : propVal.trim();
			if (propVal != null && !propVal.isEmpty()) {
				if (propVal.equalsIgnoreCase(Boolean.TRUE.toString()))
					val = Boolean.TRUE;
				else if (propVal.equalsIgnoreCase(Boolean.FALSE.toString()))
					val = Boolean.FALSE;

				if (val == null)
					logger.warn("getCryptoSessionExpiryTimerEnabled: Property '{}' is set to '{}', which is an ILLEGAL value. Falling back to default value.", propName, propVal);
				else
					logger.info("getCryptoSessionExpiryTimerEnabled: Property '{}' is set to '{}'.", propName, val);
			}

			if (val == null) {
				val = Boolean.TRUE;
				logger.info("getCryptoSessionExpiryTimerEnabled: Property '{}' is not set. Using default value {}.", propName, val);
			}

			cryptoSessionExpiryTimerEnabled = val;
		}
		return val;
	}

	/**
	 * <p>
	 * Get the age after which an unused session expires.
	 * </p><p>
	 * This value can be configured using the persistence property {@value CryptoManager#PROPERTY_CRYPTO_SESSION_EXPIRY_AGE}.
	 * </p><p>
	 * A {@link CryptoSession} expires when its {@link CryptoSession#getLastUsageTimestamp() lastUsageTimestamp}
	 * is longer in the past than this expiry age. Note, that the session might be kept longer, because a
	 * timer checks {@link #getCryptoSessionExpiryTimerPeriod() periodically} for expired sessions.
	 * </p>
	 *
	 * @return the expiry age (of non-usage-time) in milliseconds, after which the session should be closed.
	 * @see CryptoManager#PROPERTY_CRYPTO_SESSION_EXPIRY_AGE
	 */
	protected long getCryptoSessionExpiryAge()
	{
		long val = cryptoSessionExpiryAge;
		if (val == Long.MIN_VALUE) {
			String propName = PROPERTY_CRYPTO_SESSION_EXPIRY_AGE;
			String propVal = (String) getCryptoManagerRegistry().getNucleusContext().getPersistenceConfiguration().getProperty(propName);
			propVal = propVal == null ? null : propVal.trim();
			if (propVal != null && !propVal.isEmpty()) {
				try {
					val = Long.parseLong(propVal);
					if (val <= 0) {
						logger.warn("getCryptoSessionExpiryAgeMSec: Property '{}' is set to '{}', which is an ILLEGAL value (<= 0). Falling back to default value.", propName, propVal);
						val = Long.MIN_VALUE;
					}
					else
						logger.info("getCryptoSessionExpiryAgeMSec: Property '{}' is set to {} ms.", propName, val);
				} catch (NumberFormatException x) {
					logger.warn("getCryptoSessionExpiryAgeMSec: Property '{}' is set to '{}', which is an ILLEGAL value (no valid number). Falling back to default value.", propName, propVal);
				}
			}

			if (val == Long.MIN_VALUE) {
				val =  30L * 60000L;
				logger.info("getCryptoSessionExpiryAgeMSec: Property '{}' is not set. Using default value {}.", propName, val);
			}

			cryptoSessionExpiryAge = val;
		}
		return val;
	}

	private Date lastCloseExpiredCryptoSessionsTimestamp = null;

	/**
	 * <p>
	 * Close expired {@link CryptoSession}s. If <code>force == false</code>, it does so only periodically.
	 * </p><p>
	 * This method is called by {@link #getCryptoSession(String)} with <code>force == false</code>, if the timer
	 * is disabled {@link #getCryptoSessionExpiryTimerPeriod() timer-period == 0}. If the timer is enabled,
	 * it is called periodically by the timer with <code>force == true</code>.
	 * </p><p>
	 * </p>
	 *
	 * @param force whether to force the cleanup now or only do it periodically.
	 * @see CryptoManager#PROPERTY_CRYPTO_SESSION_EXPIRY_AGE
	 * @see CryptoManager#PROPERTY_CRYPTO_SESSION_EXPIRY_TIMER_PERIOD
	 */
	protected void closeExpiredCryptoSessions(boolean force)
	{
		synchronized (this) {
			if (
					!force && (
							lastCloseExpiredCryptoSessionsTimestamp != null &&
							lastCloseExpiredCryptoSessionsTimestamp.after(new Date(System.currentTimeMillis() - getCryptoSessionExpiryTimerPeriod()))
					)
			)
			{
				logger.trace("closeExpiredCryptoSessions: force == false and period not yet elapsed. Skipping.");
				return;
			}

			lastCloseExpiredCryptoSessionsTimestamp = new Date();
		}

		Date closeSessionsBeforeThisTimestamp = new Date(
				System.currentTimeMillis() - getCryptoSessionExpiryAge()
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

	private final void initTimerTask()
	{
		if (!closeExpiredSessionsTimerInitialised) {
			synchronized (AbstractCryptoManager.class) {
				if (!closeExpiredSessionsTimerInitialised) {
					if (getCryptoSessionExpiryTimerEnabled())
						closeExpiredSessionsTimer = new Timer();

					closeExpiredSessionsTimerInitialised = true;
				}
			}
		}

		if (!closeExpiredSessionsTaskInitialised) {
			synchronized (this) {
				if (!closeExpiredSessionsTaskInitialised) {
					if (closeExpiredSessionsTimer != null) {
						long periodMSec = getCryptoSessionExpiryTimerPeriod();
						closeExpiredSessionsTimer.schedule(new CloseExpiredSessionsTask(this, periodMSec), periodMSec, periodMSec);
					}
					closeExpiredSessionsTaskInitialised = true;
				}
			}
		}
	}

	@Override
	public CryptoSession getCryptoSession(String cryptoSessionID)
	{
		initTimerTask();

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
			Date sessionExpiredBeforeThisTimestamp = new Date(System.currentTimeMillis() - getCryptoSessionExpiryAge());
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
	public String getMACAlgorithm()
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
				logger.info("getMACAlgorithm: Property '{}' is not set. Using default MAC algorithm '{}'.", macAlgorithmPropName, ma);
			}
			else {
				ma = macAlgorithmPropValue.trim();
				logger.info("getMACAlgorithm: Property '{}' is set to '{}'. Using this MAC algorithm.", macAlgorithmPropName, ma);
			}
			ma = ma.toUpperCase(Locale.ENGLISH);
			macAlgorithm = ma;
		}

		return ma;
	}
	private String macAlgorithm = null;
}
