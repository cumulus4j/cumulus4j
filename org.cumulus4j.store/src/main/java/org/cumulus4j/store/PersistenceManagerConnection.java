package org.cumulus4j.store;

import javax.jdo.PersistenceManager;

/**
 * <p>
 * Connection to the underlying datastore(s).
 * </p><p>
 * Cumulus4j can be used with either one or two underlying datastores.
 * If it is used with two datastores, the {@link #getDataPM() actual data} and the {@link #getIndexPM() index information} is
 * stored separately. The meta-data of the persistence-capable classes is replicated (and thus present in both datastores).
 * </p>
 *
 * @author Andy Jefferson
 * @author Marco หงุ่ยตระกูล-Schulze - marco at nightlabs dot de (added javadoc)
 */
public class PersistenceManagerConnection
{
	/** PM for data (never null). */
	private PersistenceManager pmData;
	/** PM for indexes, could be null in which case use pmData */
	private PersistenceManager pmIndex;

	public PersistenceManagerConnection(PersistenceManager pmData, PersistenceManager pmIndex)
	{
		if (pmData == null)
			throw new IllegalArgumentException("pmData == null");

		if (pmIndex == pmData)
			throw new IllegalArgumentException("pmIndex == pmData :: If there is no pmIndex, it should be null and not the same as pmData!");

		this.pmData = pmData;
		this.pmIndex = pmIndex;
	}

	/**
	 * Determine, if there is a separate index-PM.
	 *
	 * @return <code>true</code>, if there is a separate index-PM configured (i.e. {@link #getDataPM()} and {@link #getIndexPM()}
	 * return different objects); <code>false</code> otherwise (i.e. {@link #getDataPM()} and {@link #getIndexPM()} return the same PM).
	 */
	public boolean indexHasOwnPM() {
		return pmIndex != null;
	}

	/**
	 * Accessor for the PM to use for data.
	 * @return The PM to use for data
	 */
	public PersistenceManager getDataPM() {
		return pmData;
	}

	/**
	 * Accessor for the PM to use for indexes. This method falls back to the {@link #getDataPM() data-PM},
	 * if there is no separate index-PM. To determine, if there is a separate index-PM, it is recommended
	 * to use {@link #indexHasOwnPM()}.
	 * @return the PM to use for indexes. If there is no separate index-PM, this method returns the same
	 * as {@link #getDataPM()}.
	 */
	public PersistenceManager getIndexPM() {
		if (pmIndex != null) {
			return pmIndex;
		}
		return pmData;
	}
}