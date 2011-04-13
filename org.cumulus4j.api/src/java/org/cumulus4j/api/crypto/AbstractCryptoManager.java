package org.cumulus4j.api.crypto;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author Marco หงุ่ยตระกูล-Schulze - marco at nightlabs dot de
 */
public abstract class AbstractCryptoManager implements CryptoManager
{
	private static final Logger logger = LoggerFactory.getLogger(AbstractCryptoManager.class);

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

	protected abstract CryptoSession createCryptoSession();

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
}
