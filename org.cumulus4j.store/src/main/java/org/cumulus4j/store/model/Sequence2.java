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
import javax.jdo.annotations.NullValue;
import javax.jdo.annotations.PersistenceCapable;
import javax.jdo.annotations.Persistent;
import javax.jdo.annotations.PrimaryKey;

import org.cumulus4j.store.Cumulus4jIncrementGenerator;
import org.cumulus4j.store.crypto.CryptoContext;

/**
 * Persistent sequence entity used by {@link Cumulus4jIncrementGenerator}.
 * <p>
 * Objects are cached by DataNucleus via their primary key. Accessing an object via its OID therefore does not
 * require any query, if the object is already cached. Therefore, this class encodes the
 * {@link CryptoContext#getKeyStoreRefID() keyStoreRefID} and the <code>sequenceName</code> together in one
 * single {@link #getSequenceID() sequenceID}, which is the (single-field) primary key for this class.
 * <p>
 * We do not use a composite primary key, because this is not supported by all underlying databases. The chosen
 * strategy is thus the most portable and fastest.
 *
 * @author Marco หงุ่ยตระกูล-Schulze - marco at nightlabs dot de
 * @since 1.1.0
 */
@PersistenceCapable(identityType=IdentityType.APPLICATION, detachable="true")
public class Sequence2
{
	@PrimaryKey
	@Persistent(nullValue=NullValue.EXCEPTION)
	@Column(length=255)
	private String sequenceID;

	private long nextValue = 1;

	/**
	 * Default constructor. Should never be used by actual code! It exists only to fulfill the JDO requirements.
	 */
	protected Sequence2() { }

	protected static String createSequenceID(int keyStoreRefID, String sequenceName) {
		return Integer.toString(keyStoreRefID) + '.' + sequenceName;
	}

	/**
	 * Constructor creating a <code>Sequence</code> with the given primary key.
	 * @param sequenceName the name of the sequence; must not be <code>null</code>.
	 */
	protected Sequence2(int keyStoreRefID, String sequenceName)
	{
		if (sequenceName == null)
			throw new IllegalArgumentException("sequenceName == null");

		this.sequenceID = createSequenceID(keyStoreRefID, sequenceName);
	}

	public String getSequenceID() {
		return sequenceID;
	}

	protected String[] splitSequenceID() {
		int dotIndex = sequenceID.indexOf('.');
		if (dotIndex < 0)
			throw new IllegalStateException(String.format("sequenceID \"%s\" does not contain a dot ('.')!", sequenceID));

		String[] result = new String[2];
		result[0] = sequenceID.substring(0, dotIndex);
		result[1] = sequenceID.substring(dotIndex + 1);
		return result;
	}

	public int getKeyStoreRefID() {
		String keyStoreRefIDStr = splitSequenceID()[0];
		try {
			int keyStoreRefID = Integer.parseInt(keyStoreRefIDStr);
			return keyStoreRefID;
		} catch (NumberFormatException x) {
			throw new IllegalStateException(
					String.format(
							"First part of sequenceID \"%s\" is \"%s\", which is not a valid integer: %s",
							sequenceID, keyStoreRefIDStr, x.toString()
					),
					x
			);
		}
	}

	/**
	 * Get the name of the sequence.
	 * @return the name of the sequence.
	 */
	public String getSequenceName() {
		return splitSequenceID()[1];
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
