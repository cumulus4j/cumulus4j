package org.cumulus4j.core.util;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.jar.Manifest;

public class ManifestUtil
{
	public static Manifest readManifest(Class<?> clazz)
	throws IOException
	{
		if (clazz == null)
			throw new IllegalArgumentException("clazz must not be null!");

		Manifest manifest = new Manifest();

		String referenceClassRelativePath = "/" + clazz.getName().replace('.', '/') + ".class";
		URL referenceClassURL = clazz.getResource(referenceClassRelativePath);
		String referenceClassURLBase = referenceClassURL.toExternalForm();
		if (!referenceClassURLBase.endsWith(referenceClassRelativePath))
			throw new IllegalStateException("referenceClassURL does not end on \"" + referenceClassRelativePath + "\": " + referenceClassURLBase);

		referenceClassURLBase = referenceClassURLBase.substring(0, referenceClassURLBase.length() - referenceClassRelativePath.length());

		// strip the EJB-JAR-inside part of the URL ("jar:" at the beginning and "!" at the end)
		String jarPrefix = "jar:";
		if (referenceClassURLBase.startsWith(jarPrefix)) {
			if (!referenceClassURLBase.endsWith("!"))
				throw new IllegalStateException("referenceClassURLBase starts with \"" + jarPrefix + "\" but does not end with \"!\": " + referenceClassURLBase);

			referenceClassURLBase = referenceClassURLBase.substring(jarPrefix.length(), referenceClassURLBase.length() - 1);

			// strip the name of the JAR at the end
			if (referenceClassURLBase.endsWith("/") || referenceClassURLBase.endsWith(File.separator))
				throw new IllegalStateException("Expected a normal character but found \"/\" at the end: " + referenceClassURLBase);

			int lastSlashIdx = referenceClassURLBase.replace(File.separatorChar, '/').lastIndexOf('/'); // We find both, slashes and backslashes this way.
			if (lastSlashIdx < 0)
				throw new IllegalStateException("referenceClassURLBase does not contain any EAR! Cannot find separator anymore: " + referenceClassURLBase);

			referenceClassURLBase = referenceClassURLBase.substring(0, lastSlashIdx);
		}

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
