package org.cumulus4j.keymanager.front.webapp;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import javax.ws.rs.core.Context;

import org.cumulus4j.keymanager.AppServerManager;
import org.cumulus4j.keymanager.back.shared.SystemPropertyUtil;
import org.cumulus4j.keystore.KeyStore;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * <p>
 * Manager for {@link KeyStore}s mapping a <code>keyStoreID</code> to a file name in the local
 * file system.
 * </p><p>
 * One instance of this class is held as a REST-app-singleton and injected into the REST services
 * via {@link Context} like this example:
 * </p>
 * <pre>
 * private &#64;Context KeyStoreManager keyStoreManager;
 * </pre>
 *
 * @author Marco หงุ่ยตระกูล-Schulze - marco at nightlabs dot de
 */
public class KeyStoreManager
{
	private static final Logger logger = LoggerFactory.getLogger(KeyStoreManager.class);

	/**
	 * <p>
	 * System property to control which directory is used to manage key store files. If not specified,
	 * the directory "&#36;{user.home}/.cumulus4j/" will be used.
	 * </p>
	 * <p>
	 * You can use system properties in this system-property's value. For example
	 * passing "-Dcumulus4j.keyStoreDir=&#36;{java.io.tmpdir}/cumulus4j-key-stores"
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

	/**
	 * Get the <code>KeyStore</code> identified by the given <code>keyStoreID</code>. If it does not exist,
	 * it is implicitely created.
	 * @param keyStoreID the identfier of the {@link KeyStore} to be returned. Must not be <code>null</code>.
	 * @return the <code>KeyStore</code> identified by the given <code>keyStoreID</code>.
	 * @throws IOException if reading from / writing to the local file system failed.
	 */
	public synchronized KeyStore getKeyStore(String keyStoreID) throws IOException
	{
		if (keyStoreID == null)
			throw new IllegalArgumentException("keyStoreID == null");

		KeyStore keyStore = keyStoreID2keyStore.get(keyStoreID);
		if (keyStore == null) {
			File keyStoreFile = new File(getKeyStoreDir(), keyStoreID + ".keystore");
			keyStore = new KeyStore(keyStoreID, keyStoreFile);
			keyStoreID2keyStore.put(keyStoreID, keyStore);
		}
		return keyStore;
	}

	/**
	 * Get the <code>AppServerManager</code> that is assigned (in a 1-1-relation) to the {@link KeyStore}
	 * identified by the given ID.
	 *
	 * @param keyStoreID the identfier of the {@link KeyStore} whose <code>AppServerManager</code> shall be returned. Must not be <code>null</code>.
	 * @return the <code>AppServerManager</code> that is assigned (in a 1-1-relation) to the {@link KeyStore}
	 * identified by the given ID.
	 * @throws IOException if reading from / writing to the local file system failed.
	 */
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
