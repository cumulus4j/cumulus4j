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

import java.util.Collection;
import java.util.Map;

import javax.jdo.annotations.IdentityType;
import javax.jdo.annotations.Inheritance;
import javax.jdo.annotations.InheritanceStrategy;
import javax.jdo.annotations.PersistenceCapable;
import javax.jdo.annotations.Unique;

/**
 * <p>
 * Persistent index information for the size of fields of type {@link Collection}/{@link Map}.
 * </p>
 */
@PersistenceCapable(identityType=IdentityType.APPLICATION, detachable="true")
@Inheritance(strategy=InheritanceStrategy.NEW_TABLE)
@Unique(members={"keyStoreRefID", "fieldMeta_fieldID", "classMeta_classID", "indexKey"})
public class IndexEntryContainerSize
extends IndexEntry
{
	private Long indexKey;

	@Override
	public Long getIndexKey() {
		return indexKey;
	}

	@Override
	protected void setIndexKey(Object indexKey) {
		this.indexKey = (Long) indexKey;
	}

	/**
	 * {@inheritDoc}
	 * <p>
	 * The <code>IndexEntryContainerSize</code> always uses the {@link FieldMeta#getClassMeta() FieldMeta.classMeta}
	 * as <code>classMeta</code>.
	 */
	@Override
	public ClassMeta getClassMeta() {
		return super.getClassMeta();
	}
}
