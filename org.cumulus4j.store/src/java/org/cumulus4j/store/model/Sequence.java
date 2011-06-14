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

import javax.jdo.JDOObjectNotFoundException;
import javax.jdo.PersistenceManager;
import javax.jdo.annotations.Column;
import javax.jdo.annotations.IdentityType;
import javax.jdo.annotations.NullValue;
import javax.jdo.annotations.PersistenceCapable;
import javax.jdo.annotations.Persistent;
import javax.jdo.annotations.PrimaryKey;
import javax.jdo.identity.StringIdentity;

import org.cumulus4j.store.Cumulus4jIncrementGenerator;

/**
 * Persistent sequence entity used by {@link Cumulus4jIncrementGenerator}.
 *
 * @author Marco หงุ่ยตระกูล-Schulze - marco at nightlabs dot de
 */
@PersistenceCapable(identityType=IdentityType.APPLICATION, detachable="true")
public class Sequence
{
	public static Sequence getSequence(PersistenceManager pm, String sequenceName)
	{
		StringIdentity id = new StringIdentity(Sequence.class, sequenceName);
		Sequence sequence;
		try {
			sequence = (Sequence) pm.getObjectById(id);
		} catch (JDOObjectNotFoundException x) {
			sequence = null;
		}
		return sequence;
	}

	public static Sequence createSequence(PersistenceManager pm, String sequenceName)
	{
		Sequence sequence = getSequence(pm, sequenceName);
		if (sequence == null)
			sequence = pm.makePersistent(new Sequence(sequenceName));

		return sequence;
	}

	@PrimaryKey
	@Persistent(nullValue=NullValue.EXCEPTION)
	@Column(length=255)
	private String sequenceName;

	private long nextValue = 1;

	protected Sequence() { }

	protected Sequence(String sequenceName)
	{
		if (sequenceName == null)
			throw new IllegalArgumentException("sequenceName == null");

		this.sequenceName = sequenceName;
	}

	public String getSequenceName() {
		return sequenceName;
	}

	public long getNextValue() {
		return nextValue;
	}

	public void setNextValue(long nextValue) {
		this.nextValue = nextValue;
	}
}
