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
package org.cumulus4j.store.util;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.jar.Manifest;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Utility to read a <code>MANIFEST.MF</code>.
 * @author Marco หงุ่ยตระกูล-Schulze - marco at nightlabs dot de
 */
public final class ManifestUtil
{
	private static final Logger logger = LoggerFactory.getLogger(ManifestUtil.class);

	private ManifestUtil() { }

	/**
	 * Read the <code>MANIFEST.MF</code> of the JAR file containing the given class.
	 * @param clazz a class located in the JAR whose MANIFEST.MF should be read.
	 * @return the manifest; never <code>null</code>.
	 * @throws IOException if reading the manifest fails.
	 */
	public static Manifest readManifest(Class<?> clazz)
	throws IOException
	{
		if (clazz == null)
			throw new IllegalArgumentException("clazz must not be null!");

		Manifest manifest = new Manifest();

		String referenceClassRelativePath = "/" + clazz.getName().replace('.', '/') + ".class";
		URL referenceClassURL = clazz.getResource(referenceClassRelativePath);
		logger.trace("readManifest: referenceClassURL={}", referenceClassURL);

		String referenceClassURLBase = referenceClassURL.toExternalForm();
		if (!referenceClassURLBase.endsWith(referenceClassRelativePath))
			throw new IllegalStateException("referenceClassURL does not end on \"" + referenceClassRelativePath + "\": " + referenceClassURLBase);

		referenceClassURLBase = referenceClassURLBase.substring(0, referenceClassURLBase.length() - referenceClassRelativePath.length());
		logger.trace("readManifest: referenceClassURLBase={}", referenceClassURLBase);

		String manifestResourceName = "/META-INF/MANIFEST.MF";
		URL manifestResourceUrl = new URL(referenceClassURLBase + manifestResourceName);

		InputStream in = manifestResourceUrl.openStream();
		try {
			manifest.read(in);
		} catch (IOException x) {
			throw new IOException("Cannot read resource: " + manifestResourceUrl, x);
		} finally {
			in.close();
		}

		return manifest;
	}
}
