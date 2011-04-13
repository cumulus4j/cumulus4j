package org.cumulus4j.api.crypto;

import java.util.Comparator;
import java.util.Date;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.ConcurrentSkipListSet;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author Marco หงุ่ยตระกูล-Schulze - marco at nightlabs dot de
 */
public abstract class AbstractCryptoSession implements CryptoSession
{
	private static final Logger logger = LoggerFactory.getLogger(AbstractCryptoSession.class);

	private static long MAX_AGE = 30L * 60000L;

	private static ConcurrentSkipListSet<AbstractCryptoSession> sessionsSortedByLastUsage = new ConcurrentSkipListSet<AbstractCryptoSession>(
			new Comparator<AbstractCryptoSession>() {
				@Override
				public int compare(AbstractCryptoSession o1, AbstractCryptoSession o2) {
					if (o1 == o2)
						return 0;

					int result = o1.getLastUsageTimestamp().compareTo(o2.getLastUsageTimestamp());
					if (result != 0)
						return result;

					if (o1.getCryptoManager() != o2.getCryptoManager()) {
						int cmihc1 = System.identityHashCode(o1.getCryptoManager());
						int cmihc2 = System.identityHashCode(o2.getCryptoManager());

						result = cmihc1 == cmihc2 ? 0 : (cmihc1 < cmihc2 ? -1 : 1);
						if (result != 0)
							return result;
					}

					result = o1.getCryptoSessionID().compareTo(o2.getCryptoSessionID());
					if (result == 0)
						logger.warn("result == 0");

					return result;
				}
			}
	);
	private static Timer releaseOldSessionsTimer = new Timer();
	private static TimerTask releaseOldSessionsTask = new TimerTask() {
		@Override
		public void run() {
			logger.debug("releaseOldSessionsTask.run: entered");
			Date closeSessionsBeforeThisTimestamp = new Date(System.currentTimeMillis() - MAX_AGE);

			for (AbstractCryptoSession session : sessionsSortedByLastUsage) {
				if (session.getLastUsageTimestamp().before(closeSessionsBeforeThisTimestamp)) {
					logger.debug("releaseOldSessionsTask.run: closing expired session: " + session);
					session.close();
				}
				else
					break;
			}
		}
	};
	static {
		long periodMSec = 10000;
		releaseOldSessionsTimer.schedule(releaseOldSessionsTask, periodMSec, periodMSec);
	}

	private Date creationTimestamp = new Date();
	private Date lastUsageTimestamp = creationTimestamp;
	private CryptoManager cryptoManager;
	private String cryptoSessionID;

	private Object inUseMutex = new Object();
	private int inUseCounter = 0;

	@Override
	public CryptoManager getCryptoManager() {
		return cryptoManager;
	}

	@Override
	public void setCryptoManager(CryptoManager cryptoManager)
	{
		if (cryptoManager == null)
			throw new IllegalArgumentException("cryptoManager == null");

		if (cryptoManager == this.cryptoManager)
			return;

		if (this.cryptoManager != null)
			throw new IllegalStateException("this.cryptoManager already assigned! Cannot modify!");

		this.cryptoManager = cryptoManager;
	}

	@Override
	public String getCryptoSessionID()
	{
		return cryptoSessionID;
	}

	@Override
	public void setCryptoSessionID(String cryptoSessionID)
	{
		if (cryptoSessionID == null)
			throw new IllegalArgumentException("cryptoSessionID == null");

		if (cryptoSessionID.equals(this.cryptoSessionID))
			return;

		if (this.cryptoSessionID != null)
			throw new IllegalStateException("this.cryptoSessionID already assigned! Cannot modify!");

		this.cryptoSessionID = cryptoSessionID;
	}

	@Override
	public Date getCreationTimestamp()
	{
		return creationTimestamp;
	}

	@Override
	public Date getLastUsageTimestamp() {
		return lastUsageTimestamp;
	}

	protected void assertUsable()
	{
		synchronized (inUseMutex) {
			if (inUseCounter == Integer.MIN_VALUE)
				throw new IllegalStateException("This CryptoSession was already closed!");

			if (inUseCounter == 0)
				throw new IllegalStateException("This CryptoSession was not correctly obtained via CryptoManager.acquireCryptoSession(...)!");
		}
	}

//	@Override
//	public boolean updateLastUsageTimestamp() {
//		lastUsageTimestamp = new Date();
//		return true;
//	}

	@Override
	public boolean isClosed() {
		synchronized (inUseMutex) {
			return inUseCounter == Integer.MIN_VALUE;
		}
	}

	@Override
	public boolean close() {
		synchronized (inUseMutex) {
			if (inUseCounter == Integer.MIN_VALUE)
				return true;

			if (inUseCounter > 0)
				return false;

			inUseCounter = Integer.MIN_VALUE;
		}
		cryptoManager.onCloseCryptoSession(this);
		sessionsSortedByLastUsage.remove(this);
		return true;
	}

	@Override
	public boolean onAcquire() {
		synchronized (inUseMutex) {
			if (isClosed())
				return false;

			++inUseCounter;

			if (logger.isDebugEnabled())
				logger.debug("onAcquire: inUseCounter={}", inUseCounter);

			return true;
		}
	}

	@Override
	public void release() {
		synchronized (inUseMutex) {
			if (isClosed())
				return;

			if (inUseCounter <= 0)
				throw new IllegalStateException("inUseCounter would become negative!");

			--inUseCounter;

			sessionsSortedByLastUsage.remove(this);
			lastUsageTimestamp = new Date();
			sessionsSortedByLastUsage.add(this);

			cryptoManager.onReleaseCryptoSession(this);

			if (logger.isDebugEnabled())
				logger.debug("release: inUseCounter={}", inUseCounter);
		}
	}
}
