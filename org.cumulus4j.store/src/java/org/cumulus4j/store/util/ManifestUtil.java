package org.cumulus4j.store.util;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.jar.Manifest;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ManifestUtil
{
	private static final Logger logger = LoggerFactory.getLogger(ManifestUtil.class);

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
