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
package org.cumulus4j.keymanager.back.shared.test;

import static org.junit.Assert.assertEquals;

import java.util.Properties;
import java.util.regex.Pattern;

import org.cumulus4j.keymanager.back.shared.SystemPropertyUtil;
import org.junit.Test;

public class SystemPropertyUtilTest {

	/**
	 * The following setup causes a StringIndexOutOfBoundsException on windows
	 * systems. IMHO the replace function is sufficient here.
	 */
	@Test(expected = StringIndexOutOfBoundsException.class)
	public void stringIndexOutOfBoundsExceptionTest() {
		String result = "jdbc:derby:${java.io.tmpdir}/derby/messagebroker;create=true";

		String key = "java.io.tmpdir";
		String value = "C:\\Users\\Jan\\AppData\\Local\\Temp\\";

		result = result.replaceAll(
				"\\$\\{" + Pattern.quote(String.valueOf(key)) + "\\}",
				String.valueOf(value));
	}

	/**
	 * Implementation of the javadoc example of resolveSystemProperties.
	 */
	@Test
	public void resolvePropertiesTest() {
		Properties properties = new Properties();
		properties.put("aaa", "someString/${bbb}/other");
		properties.put("bbb", "xxx");

		String raw = "yyy/${aaa}/zzz";

		String resolved = SystemPropertyUtil.resolveProperties(raw, properties);

		assertEquals("yyy/someString/xxx/other/zzz", resolved);
	}

	/**
	 * Test using windows paths.
	 */
	@Test
	public void resolvePropertiesWindowsTest() {
		Properties properties = new Properties();
		properties.put("java.io.tmpdir",
				"C:\\Users\\Jan\\AppData\\Local\\Temp\\");

		String raw = "jdbc:derby:${java.io.tmpdir}/derby/messagebroker;create=true";

		String resolved = SystemPropertyUtil.resolveProperties(raw, properties);

		assertEquals(
				"jdbc:derby:C:\\Users\\Jan\\AppData\\Local\\Temp\\/derby/messagebroker;create=true",
				resolved);
	}

	/**
	 * Test using windows paths.
	 */
	@Test
	public void resolveMultiplePropertiesTest() {
		Properties properties = new Properties();
		properties.put("aaa", "fight club");

		String raw = "the first rule of ${aaa} is: you do not talk about ${aaa}.";

		String resolved = SystemPropertyUtil.resolveProperties(raw, properties);

		assertEquals(
				"the first rule of fight club is: you do not talk about fight club.",
				resolved);
	}
}
