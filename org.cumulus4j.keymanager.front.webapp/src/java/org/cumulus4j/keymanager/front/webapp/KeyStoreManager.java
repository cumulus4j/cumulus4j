package org.cumulus4j.keymanager.front.webapp;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import org.cumulus4j.keymanager.AppServerManager;
import org.cumulus4j.keymanager.back.shared.SystemPropertyUtil;
import org.cumulus4j.keystore.KeyStore;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class KeyStoreManager
{
	private static final Logger logger = LoggerFactory.getLogger(KeyStoreManager.class);

	/**
	 * <p>
	 * System property to control which directory is used to manage key store files. If not specified,
	 * the directory "${user.home}/.cumulus4j/" will be used.
	 * </p>
	 * <p>
	 * You can use system properties in this system-property's value. For example
	 * passing "-Dcumulus4j.keyStoreDir=${java.io.tmpdir}/cumulus4j-key-stores"
	 * to the java command will be resolved to "/tmp/cumulus4j-key-stores" on GNU+Linux.
	 * </p>
	 */
	public static final String SYSTEM_PROPERTY_KEY_STORE_DIR = "cumulus4j.keyStoreDir";

	private static File getUserHome()
	{
		String userHome = System.getProperty("user.home"); //$NON-NLS-1$
		if (userHome == null)
			throw new IllegalStateException("System property user.home is not set! This should never happen!"); //$NON-NLS-1$

		return new File(userHome);
	}

	private Map<String, KeyStore> keyStoreID2keyStore = new HashMap<String, KeyStore>();

	private Map<String, AppServerManager> keyStoreID2appServerManager = new HashMap<String, AppServerManager>();

	private File getKeyStoreDir() throws IOException
	{
		String keyStoreDirSysPropVal = System.getProperty(SYSTEM_PROPERTY_KEY_STORE_DIR);
		File keyStoreDir;

		if (keyStoreDirSysPropVal == null || keyStoreDirSysPropVal.trim().isEmpty()) {
			keyStoreDir = new File(getUserHome(), ".cumulus4j");
			logger.info(
					"getSingletons: System property '{}' is empty or not specified. Using default keyStoreDir '{}'.",
					SYSTEM_PROPERTY_KEY_STORE_DIR, keyStoreDir.getAbsolutePath()
			);
		}
		else {
			String keyStoreDirSysPropValResolved = SystemPropertyUtil.resolveSystemProperties(keyStoreDirSysPropVal);
			keyStoreDir = new File(keyStoreDirSysPropValResolved);
			logger.info(
					"getSingletons: System property '{}' was set to '{}'. Using keyStoreDir '{}'.",
					new Object[] { SYSTEM_PROPERTY_KEY_STORE_DIR, keyStoreDirSysPropVal, keyStoreDir.getAbsolutePath() }
			);
		}

		if (!keyStoreDir.isDirectory()) {
			keyStoreDir.mkdirs();

			if (!keyStoreDir.isDirectory())
				throw new IOException("Creating directory \"" + keyStoreDir.getAbsolutePath() + "\" failed! Check permissions!");
		}
		return keyStoreDir;
	}

	public synchronized KeyStore getKeyStore(String keyStoreID) throws IOException
	{
		if (keyStoreID == null)
			throw new IllegalArgumentException("keyStoreID == null");

		KeyStore keyStore = keyStoreID2keyStore.get(keyStoreID);
		if (keyStore == null) {
			File keyStoreFile = new File(getKeyStoreDir(), keyStoreID + ".keystore");
			keyStore = new KeyStore(keyStoreFile);
			keyStoreID2keyStore.put(keyStoreID, keyStore);
		}
		return keyStore;
	}

	public synchronized AppServerManager getAppServerManager(String keyStoreID) throws IOException
	{
		if (keyStoreID == null)
			throw new IllegalArgumentException("keyStoreID == null");

		AppServerManager appServerManager = keyStoreID2appServerManager.get(keyStoreID);
		if (appServerManager == null) {
			appServerManager = new AppServerManager(getKeyStore(keyStoreID));
			keyStoreID2appServerManager.put(keyStoreID, appServerManager);
		}
		return appServerManager;
	}
}
