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
package org.cumulus4j.store.test.jpa.account;

import java.io.Serializable;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.IdClass;
import javax.persistence.Inheritance;
import javax.persistence.InheritanceType;
import javax.persistence.Table;

import org.cumulus4j.store.test.jpa.account.id.AnchorID;

@Entity
@IdClass(AnchorID.class)
@Table(name="JFireTrade_Anchor")
@Inheritance(strategy=InheritanceType.JOINED)
public abstract class Anchor
	implements Serializable
{
	private static final long serialVersionUID = 1L;

	@Id
	@Column(length=100)
	private String organisationID;

	@Id
	@Column(length=100)
	private String anchorTypeID;

	@Id
	@Column(length=100)
	private String anchorID;

	protected Anchor() { }

	public Anchor(String organisationID, String anchorTypeID, String anchorID)
	{
		this.organisationID = organisationID;
		this.anchorTypeID = anchorTypeID;
		this.anchorID = anchorID;
	}

	public static String getPrimaryKey(String organisationID, String anchorTypeID, String anchorID)
	{
		return organisationID + '/' + anchorTypeID + "/" + anchorID;
	}

	public String getPrimaryKey()
	{
		return getPrimaryKey(organisationID, anchorTypeID, anchorID);
	}

	public String getOrganisationID()
	{
		return organisationID;
	}

	public String getAnchorTypeID()
	{
		return anchorTypeID;
	}

	public String getAnchorID()
	{
		return anchorID;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((organisationID == null) ? 0 : organisationID.hashCode());
		result = prime * result + ((anchorID == null) ? 0 : anchorID.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) return true;
		if (obj == null) return false;
		if (getClass() != obj.getClass()) return false;

		Anchor other = (Anchor) obj;
		if (anchorID == null) {
			if (other.anchorID != null)
				return false;
		} else if (!anchorID.equals(other.anchorID))
			return false;

		if (organisationID == null) {
			if (other.organisationID != null)
				return false;
		} else if (!organisationID.equals(other.organisationID))
			return false;

		return true;
	}

	@Override
	public String toString() {
		return this.getClass().getName() + '@' + Integer.toHexString(System.identityHashCode(this)) + '[' + organisationID + ',' + anchorTypeID + ',' + anchorID + ']';
	}
}
