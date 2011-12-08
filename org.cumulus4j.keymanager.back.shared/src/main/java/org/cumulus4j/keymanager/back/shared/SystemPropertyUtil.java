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
package org.cumulus4j.keymanager.back.shared;

import java.util.Map;
import java.util.Properties;

/**
 * Helper class to work with system properties.
 * 
 * @author Marco หงุ่ยตระกูล-Schulze - marco at nightlabs dot de
 */
public final class SystemPropertyUtil {

	private static final int MAX_REPLACE_ITERATIONS = 10000;

	private SystemPropertyUtil() {
	}

	/**
	 * Resolve system properties like "&#36;{java.io.tmpdir}" in a
	 * <code>String</code>. Note, that this method works recursively, i.e. you
	 * can specify a system property "
	 * <code>aaa = someString/&#36;{bbb}/other</code>" and a system property "
	 * <code>bbb = xxx</code>" and the string "<code>yyy/&#36;{aaa}/zzz</code>
	 * " will be resolved to "<code>yyy/someString/xxx/other/zzz</code>".
	 * 
	 * @param raw
	 *            the raw <code>String</code>, e.g.
	 *            "&#36;{java.io.tmpdir}/mydirectory".
	 * @return the resolved <code>String</code>, e.g. "/tmp/mydirectory".
	 */
	public static String resolveSystemProperties(String raw) {
		return resolveProperties(raw, System.getProperties());
	}

	/**
	 * General core of <code>resolveSystemProperties</code> simplifying the
	 * creation of test functions.
	 * 
	 * @param raw
	 *            the raw <code>String</code>, e.g.
	 *            "&#36;{java.io.tmpdir}/mydirectory".
	 * @param properties
	 *            Properties to resolve
	 * @return the resolved <code>String</code>, e.g. "/tmp/mydirectory".
	 */
	public static String resolveProperties(String raw, Properties properties) {
		String result = raw;

		// Counter to prevent running forever if an invalid string was
		// specified.
		for (int i = 0; (result.indexOf('$') >= 0)
				&& (i < MAX_REPLACE_ITERATIONS); i++) {
			for (Map.Entry<?, ?> prop : properties.entrySet()) {
				String target = "${" + String.valueOf(prop.getKey()) + "}";
				result = result
						.replace(target, String.valueOf(prop.getValue()));
			}
		}

		if (result.indexOf('$') >= 0) {
			throw new IllegalArgumentException(
					"Property replacement failed for string " + raw);
		}

		return result;
	}
}
