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

import javax.jdo.annotations.Column;
import javax.jdo.annotations.IdentityType;
import javax.jdo.annotations.Inheritance;
import javax.jdo.annotations.InheritanceStrategy;
import javax.jdo.annotations.PersistenceCapable;

/**
 * <p>
 * Persistent index information for fields of type {@link String} being longer than 255 characters (e.g. <code>CLOB</code>).
 * </p>
 * @author Marco หงุ่ยตระกูล-Schulze - marco at nightlabs dot de
 */
@PersistenceCapable(identityType=IdentityType.APPLICATION, detachable="true")
@Inheritance(strategy=InheritanceStrategy.NEW_TABLE)
// TODO The following "@Unique" does not work at all in Derby and only with special syntax in MySQL
// (note the length "(255)" behind the "indexkey"):
//    ALTER TABLE indexentrystringlong ADD CONSTRAINT UNIQUE KEY (indexkey(255), fieldmeta_fieldid_oid)
// => file a bug in DataNucleus' issue tracker (should log a warning, if the underlying DB doesn't support
// it and should use RDBMS-specific syntax, if necessary).
//@Unique(members={"fieldMeta", "indexKey"})
public class IndexEntryStringLong
extends IndexEntry
{
	@Column(jdbcType="CLOB")
	private String indexKey;

	@Override
	public String getIndexKey() {
		return indexKey;
	}

	@Override
	protected void setIndexKey(Object indexKey) {
		this.indexKey = (String) indexKey;
	}
}
