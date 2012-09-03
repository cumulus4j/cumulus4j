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
package org.cumulus4j.keymanager.front.webapp;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import javax.ws.rs.ApplicationPath;
import javax.ws.rs.core.Application;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * <p>
 * REST application for the key-server.
 * <p></p>
 * This class is the entry point for Jersey where all REST services and their environment is declared.
 * </p>
 *
 * @author Marco หงุ่ยตระกูล-Schulze - marco at nightlabs dot de
 */
@ApplicationPath("/")
public class KeyManagerFrontWebApp
extends Application
{
	private static final Logger logger = LoggerFactory.getLogger(KeyManagerFrontWebApp.class);

//	/**
//	 * <p>
//	 * System property to control which key file is to be used. If not specified,
//	 * the file "&#36;{user.home}/.cumulus4j/cumulus4j.keystore" will be used.
//	 * </p>
//	 * <p>
//	 * You can use system properties in this system-property's value. For example
//	 * passing "-Dorg.cumulus4j.keymanager.front.webapp.App.keyStoreFile=&#36;{java.io.tmpdir}/test.keystore"
//	 * to the java command will be resolved to "/tmp/test.keystore" on GNU+Linux.
//	 * </p>
//	 * @deprecated to be removed due to introduction of keyStoreID
//	 */
//	@Deprecated
//	public static final String SYSTEM_PROPERTY_KEY_STORE_FILE = "cumulus4j.KeyManagerFrontWebApp.keyStoreFile";

//	/**
//	 * <p>
//	 * System property to control whether to delete the key-store-file on startup.
//	 * Possible values are "true" and "false".
//	 * </p>
//	 * <p>
//	 * <b>Important:</b> This feature is for debugging and test reasons only! Never use it
//	 * on a productive system or you will loose all your keys (and thus your complete database)!!!
//	 * </p>
//	 * @deprecated TODO rename this after introduction of keyStoreID as they are not deleted on startup anymore but when first accessed.
//	 */
//	@Deprecated
//	public static final String SYSTEM_PROPERTY_DELETE_KEY_STORE_FILE_ON_STARTUP = "cumulus4j.KeyManagerFrontWebApp.deleteKeyStoreFileOnStartup";

	private static final Class<?>[] serviceClassesArray = {
		AppServerService.class,
		DateDependentKeyStrategyService.class,
		CryptoSessionService.class,
		UserService.class
	};

	private static final Set<Class<?>> serviceClassesSet;
	static {
		Set<Class<?>> s = new HashSet<Class<?>>(serviceClassesArray.length);
		for (Class<?> c : serviceClassesArray)
			s.add(c);

		serviceClassesSet = Collections.unmodifiableSet(s);

		if (logger.isDebugEnabled()) {
			logger.debug("<init>: Service classes:");
			for (Class<?> c : serviceClassesSet)
				logger.debug("<init>: {}", c == null ? null : c.getName());
		}
	}

	@Override
	public Set<Class<?>> getClasses() {
		return serviceClassesSet;
	}

	private Set<Object> singletons;

//	private File keyStoreFile;
//	private KeyStore keyStore;

//	private void initKeyStoreFile()
//	{
//		String keyStoreFileSysPropVal = System.getProperty(SYSTEM_PROPERTY_KEY_STORE_FILE);
//		if (keyStoreFileSysPropVal == null || keyStoreFileSysPropVal.trim().isEmpty()) {
//			keyStoreFile = new File(new File(getUserHome(), ".cumulus4j"), "cumulus4j.keystore");
//			logger.info(
//					"getSingletons: System property '{}' is empty or not specified. Using default keyStoreFile '{}'.",
//					SYSTEM_PROPERTY_KEY_STORE_FILE, keyStoreFile.getAbsolutePath()
//			);
//		}
//		else {
//			String keyStoreFileSysPropValResolved = SystemPropertyUtil.resolveSystemProperties(keyStoreFileSysPropVal);
//			keyStoreFile = new File(keyStoreFileSysPropValResolved);
//			logger.info(
//					"getSingletons: System property '{}' was set to '{}'. Using keyStoreFile '{}'.",
//					new Object[] { SYSTEM_PROPERTY_KEY_STORE_FILE, keyStoreFileSysPropVal, keyStoreFile.getAbsolutePath() }
//			);
//		}
//	}
//
//	private void deleteKeyStoreIfSysPropRequested() throws IOException {
//		String deleteKS = System.getProperty(SYSTEM_PROPERTY_DELETE_KEY_STORE_FILE_ON_STARTUP);
//		if (Boolean.TRUE.toString().equalsIgnoreCase(deleteKS)) {
//			if (keyStoreFile.exists()) {
//				logger.warn(
//						"getSingletons: System property '{}' was set to 'true'. DELETING keyStoreFile '{}'!!!",
//						SYSTEM_PROPERTY_DELETE_KEY_STORE_FILE_ON_STARTUP, keyStoreFile.getAbsolutePath()
//				);
//				if (!keyStoreFile.delete())
//					throw new IOException("Could not delete keyStoreFile '" + keyStoreFile.getAbsolutePath() + "'!");
//			}
//			else {
//				logger.warn(
//						"getSingletons: System property '{}' was set to 'true', but keyStoreFile '{}' does NOT exist, hence not deleting it!",
//						SYSTEM_PROPERTY_DELETE_KEY_STORE_FILE_ON_STARTUP, keyStoreFile.getAbsolutePath()
//				);
//			}
//		}
//	}

//	private void checkForDeprecatedSystemProperties()
//	{
//		checkForDeprecatedSystemProperty(SYSTEM_PROPERTY_KEY_STORE_FILE);
//		checkForDeprecatedSystemProperty(SYSTEM_PROPERTY_DELETE_KEY_STORE_FILE_ON_STARTUP);
//	}
//
//	private void checkForDeprecatedSystemProperty(String sysPropName)
//	{
//		if (System.getProperty(sysPropName) != null) {
//			logger.error("**************************************************************************");
//			logger.error("**************************************************************************");
//			logger.error("**************************************************************************");
//
//			logger.error("*** deprecated system property present (and ignored): " + sysPropName);
//
//			logger.error("**************************************************************************");
//			logger.error("**************************************************************************");
//			logger.error("**************************************************************************");
//		}
//	}

	@Override
	public Set<Object> getSingletons()
	{
		if (singletons == null) {
//			checkForDeprecatedSystemProperties();
//			initKeyStoreFile();
//
//			try {
//				deleteKeyStoreIfSysPropRequested();
//
//				if (!keyStoreFile.getParentFile().isDirectory()) {
//					keyStoreFile.getParentFile().mkdirs();
//					if (!keyStoreFile.getParentFile().isDirectory())
//						throw new IOException("Directory does not exist and could not be created: " + keyStoreFile.getParentFile().getAbsolutePath());
//				}
//
//				logger.info("Opening keyStoreFile: {}", keyStoreFile.getAbsolutePath());
//				keyStore = new KeyStore(keyStoreFile);
//			} catch (IOException x) {
//				throw new RuntimeException(x);
//			}

			Set<Object> s = new HashSet<Object>();
//			s.add(new KeyStoreProvider(keyStore));
//			s.add(new AppServerManagerProvider(new AppServerManager(keyStore)));
			s.add(new KeyStoreManagerProvider(new KeyStoreManager()));
			singletons = Collections.unmodifiableSet(s);
		}

		return singletons;
	}
}
