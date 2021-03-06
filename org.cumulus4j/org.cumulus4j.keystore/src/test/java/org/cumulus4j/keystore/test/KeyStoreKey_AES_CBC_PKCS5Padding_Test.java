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
package org.cumulus4j.keystore.test;

import org.cumulus4j.keystore.KeyStore;
import org.junit.AfterClass;
import org.junit.BeforeClass;

public class KeyStoreKey_AES_CBC_PKCS5Padding_Test
extends KeyStoreKeyTest
{
	@BeforeClass
	public static void beforeClass()
	{
		System.setProperty(KeyStore.SYSTEM_PROPERTY_ENCRYPTION_ALGORITHM, "AES/CBC/PKCS5Padding");
		System.setProperty(KeyStore.SYSTEM_PROPERTY_MAC_ALGORITHM, "HMACSHA1");
	}

	@AfterClass
	public static void afterClass()
	{
		System.clearProperty(KeyStore.SYSTEM_PROPERTY_ENCRYPTION_ALGORITHM);
		System.clearProperty(KeyStore.SYSTEM_PROPERTY_MAC_ALGORITHM);
	}
}
