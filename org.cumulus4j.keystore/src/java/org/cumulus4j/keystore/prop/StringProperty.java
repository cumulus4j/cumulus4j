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

import java.nio.charset.Charset;

/**
 * {@link Property} implementation for the value type {@link String}.
 *
 * @author Marco หงุ่ยตระกูล-Schulze - marco at nightlabs dot de
 */
public class StringProperty extends Property<String>
{
	private static final Charset UTF8 = Charset.forName("UTF-8");

	/**
	 * {@inheritDoc}
	 * <p>
	 * The implementation in <code>StringProperty</code> returns either <code>null</code>
	 * or a UTF-8-encoded <code>String</code>-representation.
	 * </p>
	 */
	@Override
	public byte[] getValueEncoded()
	{
		String value = getValue();
		if (value == null)
			return null;

		return value.getBytes(UTF8);
	}

	/**
	 * {@inheritDoc}
	 * @param encodedValue a UTF-8-encoded <code>String</code>-representation or <code>null</code>.
	 */
	@Override
	public void setValueEncoded(byte[] encodedValue) {
		if (encodedValue == null)
			setValue(null);
		else
			setValue(new String(encodedValue, UTF8));
	}
}
