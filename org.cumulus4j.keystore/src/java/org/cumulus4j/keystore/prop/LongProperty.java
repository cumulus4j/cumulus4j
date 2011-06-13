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

package org.cumulus4j.keystore.prop;

/**
 * {@link Property} implementation for the value type {@link Long}.
 *
 * @author Marco หงุ่ยตระกูล-Schulze - marco at nightlabs dot de
 */
public class LongProperty extends Property<Long>
{
	/**
	 * {@inheritDoc}
	 * <p>
	 * The implementation in <code>LongProperty</code> returns either <code>null</code> or 8 bytes containing
	 * the long value in a pretty raw form (copied byte-by-byte).
	 * </p>
	 */
	@Override
	public byte[] getValueEncoded()
	{
		Long value = getValue();
		if (value == null)
			return null;
		else {
			long val = value;
			byte[] result = new byte[8];
			for (int i = 0; i < result.length; ++ i)
				result[i] = (byte)(val >>> (i * 8));

			return result;
		}
	}

	/**
	 * {@inheritDoc}
	 * @throws IllegalArgumentException if the <code>encodedValue</code> is neither <code>null</code> nor a byte-array
	 * with a length of exactly 8.
	 */
	@Override
	public void setValueEncoded(byte[] encodedValue)
	throws IllegalArgumentException
	{
		if (encodedValue == null)
			setValue(null);
		else {
			if (encodedValue.length != 8)
				throw new IllegalArgumentException("encodedValue.length != 8 :: encodedValue must either be null or an array with the correct length!");

			long val = 0;
			for (int i = 0; i < encodedValue.length; ++ i)
				val |= ((long)encodedValue[i] & 0xff) << (i * 8);

			setValue(val);
		}
	}

}
