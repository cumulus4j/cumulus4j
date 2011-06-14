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
package org.cumulus4j.crypto.asymmetric.keypairgenerator;

import org.cumulus4j.crypto.AsymmetricCipherKeyPairGeneratorFactory;

public abstract class AbstractAsymmetricCipherKeyPairGeneratorFactory
implements AsymmetricCipherKeyPairGeneratorFactory
{
	private String algorithmName;

	@Override
	public String getAlgorithmName() {
		return algorithmName;
	}

	@Override
	public void setAlgorithmName(String algorithmName) {
		this.algorithmName = algorithmName;
	}
}
