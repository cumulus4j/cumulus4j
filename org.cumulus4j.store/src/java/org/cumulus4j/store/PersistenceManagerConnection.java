package org.cumulus4j.store;

import javax.jdo.PersistenceManager;

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
	 * Accessor for the PM to use for indexes.
	 * @return The PM to use for indexes
	 */
	public PersistenceManager getIndexPM() {
		if (pmIndex != null) {
			return pmIndex;
		}
		return pmData;
	}
}