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

/**
 * Default index entry factory, using the passed in index class for what it generates.
 */
public class DefaultIndexEntryFactory extends IndexEntryFactory {

	final Class<? extends IndexEntry> indexEntryClass;

	public DefaultIndexEntryFactory(Class<? extends IndexEntry> idxClass) {
		this.indexEntryClass = idxClass;
	}

	/* (non-Javadoc)
	 * @see org.cumulus4j.store.model.IndexEntryFactory#getIndexEntryClass()
	 */
	@Override
	public Class<? extends IndexEntry> getIndexEntryClass() {
		return indexEntryClass;
	}
}