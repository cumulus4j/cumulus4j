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
package org.cumulus4j.store.model;

import javax.jdo.PersistenceManager;

/**
 * Helper to find an {@link IndexEntry} for an object relation (1-1, 1-n or m-n).
 * Even though {@link DefaultIndexEntryFactory} and {@link IndexEntryLong} are used for such relations, these
 * classes should <b>not</b> be directly accessed in order to make refactorings easier (if this class is used for all
 * object relations, it is possible to search for references of this class).
 *
 * @author Marco หงุ่ยตระกูล-Schulze - marco at nightlabs dot de
 */
public class IndexEntryObjectRelationHelper
{
	private static final IndexEntryFactory indexEntryFactoryLong = new DefaultIndexEntryFactory(IndexEntryLong.class);

	public static IndexEntryFactory getIndexEntryFactory() {
		return indexEntryFactoryLong;
	}

	/**
	 * Get an existing {@link IndexEntry} or <code>null</code>, if it does not exist.
	 * This method looks up an <code>IndexEntry</code> for a relation to the object referenced
	 * by the given <code>indexedDataEntryID</code> and the relation-type specified by the given <code>fieldMeta</code>.
	 *
	 * @param pmIndex the backend-<code>PersistenceManager</code> used to access the index-datastore.
	 * @param fieldMeta the field pointing to the referenced object.
	 * @param indexedDataEntryID the {@link DataEntry#getDataEntryID() DataEntry.dataEntryID} of the referenced object.
	 * @return the appropriate {@link IndexEntry} or <code>null</code>.
	 */
	public static IndexEntry getIndexEntry(PersistenceManager pmIndex, FieldMeta fieldMeta, Long indexedDataEntryID)
	{
		return indexEntryFactoryLong.getIndexEntry(pmIndex, fieldMeta, indexedDataEntryID);
	}

	/**
	 * Get an existing {@link IndexEntry} or create it, if it does not yet exist. This method behaves
	 * just like {@link #getIndexEntry(PersistenceManager, FieldMeta, Long)}, but instead of returning <code>null</code>,
	 * it creates an <code>IndexEntry</code>, if it does not yet exist.
	 *
	 * @param pmIndex the backend-<code>PersistenceManager</code> used to access the index-datastore.
	 * @param fieldMeta the field pointing to the referenced object.
	 * @param keyStoreRefID TODO
	 * @param indexedDataEntryID the {@link DataEntry#getDataEntryID() DataEntry.dataEntryID} of the referenced object.
	 * @return the appropriate {@link IndexEntry}; never <code>null</code>.
	 */
	public static IndexEntry createIndexEntry(PersistenceManager pmIndex, FieldMeta fieldMeta, int keyStoreRefID, Long indexedDataEntryID)
	{
		return indexEntryFactoryLong.createIndexEntry(pmIndex, fieldMeta, keyStoreRefID, indexedDataEntryID);
	}
}
