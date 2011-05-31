package org.cumulus4j.keystore.cli;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.jar.Manifest;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class VersionSubCommand
extends SubCommand
{
	private static final Logger logger = LoggerFactory.getLogger(VersionSubCommand.class);

	@Override
	public String getSubCommandName() {
		return "version";
	}

	@Override
	public String getSubCommandDescription() {
		return "Return the version number of the JAR.";
	}

	@Override
	public void run() throws Exception {
		String[] bundleNameAndVersion = getBundleNameAndVersion(VersionSubCommand.class);
		System.err.println(bundleNameAndVersion[0]);
	}

	private static Manifest readManifest(Class<?> clazz)
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

	private static String[] getBundleNameAndVersion(Class<?> clazz)
	{
		String[] result = new String[2];

		Manifest manifest;
		try {
			manifest = readManifest(clazz);
		} catch (IOException e) {
			logger.warn("Could not read MANIFEST.MF from JAR containing " + clazz, e);
			return result;
		}
		String bundleName = manifest.getMainAttributes().getValue("Bundle-SymbolicName");
		if (bundleName != null) {
			// strip options after actual bundle name
			int idx = bundleName.indexOf(';');
			if (idx >= 0)
				bundleName = bundleName.substring(0, idx);
		}

		String version = manifest.getMainAttributes().getValue("Bundle-Version");

		result[0] = bundleName;
		result[1] = version;
		return result;
	}


}
