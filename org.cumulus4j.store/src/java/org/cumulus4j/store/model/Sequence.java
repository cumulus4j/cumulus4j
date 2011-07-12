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
	/**
	 * Get the <code>Sequence</code> identified by the given <code>sequenceName</code>.
	 * If no such <code>Sequence</code> exists, this method returns <code>null</code>.
	 * @param pm the backend-<code>PersistenceManager</code> used to access the underlying datastore; must not be <code>null</code>.
	 * @param sequenceName the name of the sequence; must not be <code>null</code>.
	 * @return the <code>Sequence</code> identified by the given <code>sequenceName</code> or <code>null</code>, if no such
	 * <code>Sequence</code> exists.
	 */
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

	/**
	 * Get the <code>Sequence</code> identified by the given <code>sequenceName</code>.
	 * If no such <code>Sequence</code> exists, this method creates &amp; persists one.
	 * @param pm the backend-<code>PersistenceManager</code> used to access the underlying datastore; must not be <code>null</code>.
	 * @param sequenceName the name of the sequence; must not be <code>null</code>.
	 * @return the <code>Sequence</code> identified by the given <code>sequenceName</code>; never <code>null</code>.
	 */
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

	/**
	 * Default constructor. Should never be used by actual code! It exists only to fulfill the JDO requirements.
	 */
	protected Sequence() { }

	/**
	 * Constructor creating a <code>Sequence</code> with the given primary key.
	 * @param sequenceName the name of the sequence; must not be <code>null</code>.
	 */
	protected Sequence(String sequenceName)
	{
		if (sequenceName == null)
			throw new IllegalArgumentException("sequenceName == null");

		this.sequenceName = sequenceName;
	}

	/**
	 * Get the name of the sequence.
	 * @return the name of the sequence.
	 */
	public String getSequenceName() {
		return sequenceName;
	}

	/**
	 * Get the next value (i.e. the first unused value) for this sequence.
	 * @return the next value (i.e. the first unused value) for this sequence.
	 */
	public long getNextValue() {
		return nextValue;
	}

	/**
	 * Set the next value (i.e. the first unused value) for this sequence.
	 * @param nextValue the next value (i.e. the first unused value) for this sequence.
	 */
	public void setNextValue(long nextValue) {
		this.nextValue = nextValue;
	}
}
