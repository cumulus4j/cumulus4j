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

	private static final Timer closeExpiredSessionsTimer = new Timer();

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
					logger.debug("run: closing expired session: " + session);
					session.close();
				}
			}

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

	{
		long periodMSec = getCryptoSessionExpiryTimerPeriodMSec();
		closeExpiredSessionsTimer.schedule(new CloseExpiredSessionsTask(periodMSec), periodMSec, periodMSec);
	}

	/**
	 * <p>
	 * Get the period in which expired crypto sessions are searched and closed.
	 * </p>
	 * @return the period in msec.
	 */
	protected long getCryptoSessionExpiryTimerPeriodMSec()
	{
		return 60000L;
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
		return 30L * 60000L;
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
				// TODO switch to "Twofish/GCM/NoPadding" as soon as the NPE when re-initing a Cipher with null-KeyParameter is fixed.
//				ea = "Twofish/GCM/NoPadding"; // default value, if the property was not defined.
				ea = "Twofish/CBC/PKCS5Padding"; // default value, if the property was not defined.
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
				// TODO switch to NONE as soon as we switched to "Twofish/GCM/NoPadding" above.
//				ma = MAC_ALGORITHM_NONE; // default value, if the property was not defined.
				ma = "HMAC-SHA1";
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
