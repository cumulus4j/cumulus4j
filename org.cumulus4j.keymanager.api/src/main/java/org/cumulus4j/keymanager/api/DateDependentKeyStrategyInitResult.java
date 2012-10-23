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
package org.cumulus4j.keymanager.api;

import java.io.Serializable;

/**
 * Result of the operation <code>org.cumulus4j.keymanager.DateDependentKeyStrategy.init(...)</code>.
 *
 * @author Marco หงุ่ยตระกูล-Schulze - marco at nightlabs dot de
 */
public class DateDependentKeyStrategyInitResult
implements Serializable
{
	private static final long serialVersionUID = 1L;

	private int generatedKeyCount;

	/**
	 * Get the number of keys that was generated by the initialisation.
	 * @return the number of generated keys.
	 * @see #setGeneratedKeyCount(int)
	 */
	public int getGeneratedKeyCount() {
		return generatedKeyCount;
	}

	/**
	 * Set the number of keys that was generated by the initialisation.
	 * @param generatedKeyCount the number of generated keys.
	 * @see #getGeneratedKeyCount()
	 */
	public void setGeneratedKeyCount(int generatedKeyCount) {
		this.generatedKeyCount = generatedKeyCount;
	}
}
