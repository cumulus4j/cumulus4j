package org.cumulus4j.keyserver.front.webapp;

import java.io.File;
import java.io.IOException;

import org.cumulus4j.keystore.KeyStore;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SessionSingleton
{
	private static final Logger logger = LoggerFactory.getLogger(SessionSingleton.class);

	private KeyStore keyStore;

	private static File getUserHome()
	{
		String userHome = System.getProperty("user.home"); //$NON-NLS-1$
		if (userHome == null)
			throw new IllegalStateException("System property user.home is not set! This should never happen!"); //$NON-NLS-1$

		return new File(userHome);
	}

	public SessionSingleton(File keyStoreFile) throws IOException
	{
		logger.info("Creating instance of SessionSingleton.");

		if (keyStoreFile == null)
			keyStoreFile = new File(new File(getUserHome(), ".cumulus4j"), "cumulus4j.keystore");

		if (!keyStoreFile.getParentFile().isDirectory()) {
			keyStoreFile.getParentFile().mkdirs();
			if (!keyStoreFile.getParentFile().isDirectory())
				throw new IOException("Directory does not exist and could not be created: " + keyStoreFile.getParentFile().getAbsolutePath());
		}

		logger.info("Opening keyStoreFile \"{}\"...", keyStoreFile.getAbsolutePath());
		keyStore = new KeyStore(keyStoreFile);
	}

}
