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

import java.io.File;
import java.io.IOException;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import javax.ws.rs.ApplicationPath;
import javax.ws.rs.core.Application;

import org.cumulus4j.keymanager.AppServerManager;
import org.cumulus4j.keymanager.back.shared.SystemPropertyUtil;
import org.cumulus4j.keystore.KeyStore;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Marco หงุ่ยตระกูล-Schulze - marco at nightlabs dot de
 */
@ApplicationPath("/")
public class KeyManagerFrontWebApp
extends Application
{
	private static final Logger logger = LoggerFactory.getLogger(KeyManagerFrontWebApp.class);

	/**
	 * <p>
	 * System property to control which key file is to be used. If not specified,
	 * the file "${user.home}/.cumulus4j/cumulus4j.keystore" will be used.
	 * </p>
	 * <p>
	 * You can use system properties in this system-property's value. For example
	 * passing "-Dorg.cumulus4j.keymanager.front.webapp.App.keyStoreFile=${java.io.tmpdir}/test.keystore"
	 * to the java command will be resolved to "/tmp/test.keystore" on GNU+Linux.
	 * </p>
	 */
	public static final String SYSTEM_PROPERTY_KEY_STORE_FILE = "cumulus4j.KeyManagerFrontWebApp.keyStoreFile";

	/**
	 * <p>
	 * System property to control whether to delete the key-store-file on startup.
	 * Possible values are "true" and "false".
	 * </p>
	 * <p>
	 * <b>Important:</b> This feature is for debugging and test reasons only! Never use it
	 * on a productive system or you will loose all your keys (and thus your complete database)!!!
	 * </p>
	 */
	public static final String SYSTEM_PROPERTY_DELETE_KEY_STORE_FILE_ON_STARTUP = "cumulus4j.KeyManagerFrontWebApp.deleteKeyStoreFileOnStartup";

	private static File getUserHome()
	{
		String userHome = System.getProperty("user.home"); //$NON-NLS-1$
		if (userHome == null)
			throw new IllegalStateException("System property user.home is not set! This should never happen!"); //$NON-NLS-1$

		return new File(userHome);
	}

	private static final Class<?>[] serviceClassesArray = {
		AppServerService.class,
		DateDependentKeyStrategyService.class,
		SessionService.class,
		UserService.class
	};

	private static final Set<Class<?>> serviceClassesSet;
	static {
		Set<Class<?>> s = new HashSet<Class<?>>(serviceClassesArray.length);
		for (Class<?> c : serviceClassesArray)
			s.add(c);

		serviceClassesSet = Collections.unmodifiableSet(s);
	}

	@Override
	public Set<Class<?>> getClasses() {
		return serviceClassesSet;
	}

	private Set<Object> singletons;

	private File keyStoreFile;
	private KeyStore keyStore;

	private void initKeyStoreFile()
	{
		String keyStoreFileSysPropVal = System.getProperty(SYSTEM_PROPERTY_KEY_STORE_FILE);
		if (keyStoreFileSysPropVal == null || keyStoreFileSysPropVal.trim().isEmpty()) {
			keyStoreFile = new File(new File(getUserHome(), ".cumulus4j"), "cumulus4j.keystore");
			logger.info(
					"getSingletons: System property '{}' is empty or not specified. Using default keyStoreFile '{}'.",
					SYSTEM_PROPERTY_KEY_STORE_FILE, keyStoreFile.getAbsolutePath()
			);
		}
		else {
			String keyStoreFileSysPropValResolved = SystemPropertyUtil.resolveSystemProperties(keyStoreFileSysPropVal);
			keyStoreFile = new File(keyStoreFileSysPropValResolved);
			logger.info(
					"getSingletons: System property '{}' was set to '{}'. Using keyStoreFile '{}'.",
					new Object[] { SYSTEM_PROPERTY_KEY_STORE_FILE, keyStoreFileSysPropVal, keyStoreFile.getAbsolutePath() }
			);
		}
	}

	private void deleteKeyStoreIfSysPropRequested() throws IOException {
		String deleteKS = System.getProperty(SYSTEM_PROPERTY_DELETE_KEY_STORE_FILE_ON_STARTUP);
		if (Boolean.TRUE.toString().equalsIgnoreCase(deleteKS)) {
			if (keyStoreFile.exists()) {
				logger.warn(
						"getSingletons: System property '{}' was set to 'true'. DELETING keyStoreFile '{}'!!!",
						SYSTEM_PROPERTY_DELETE_KEY_STORE_FILE_ON_STARTUP, keyStoreFile.getAbsolutePath()
				);
				if (!keyStoreFile.delete())
					throw new IOException("Could not delete keyStoreFile '" + keyStoreFile.getAbsolutePath() + "'!");
			}
			else {
				logger.warn(
						"getSingletons: System property '{}' was set to 'true', but keyStoreFile '{}' does NOT exist, hence not deleting it!",
						SYSTEM_PROPERTY_DELETE_KEY_STORE_FILE_ON_STARTUP, keyStoreFile.getAbsolutePath()
				);
			}
		}
	}

	@Override
	public Set<Object> getSingletons()
	{
		if (singletons == null) {
			initKeyStoreFile();

			try {
				deleteKeyStoreIfSysPropRequested();

				if (!keyStoreFile.getParentFile().isDirectory()) {
					keyStoreFile.getParentFile().mkdirs();
					if (!keyStoreFile.getParentFile().isDirectory())
						throw new IOException("Directory does not exist and could not be created: " + keyStoreFile.getParentFile().getAbsolutePath());
				}

				logger.info("Opening keyStoreFile: {}", keyStoreFile.getAbsolutePath());
				keyStore = new KeyStore(keyStoreFile);
			} catch (IOException x) {
				throw new RuntimeException(x);
			}

			Set<Object> s = new HashSet<Object>();
			s.add(new KeyStoreProvider(keyStore));
			s.add(new AppServerManagerProvider(new AppServerManager(keyStore)));
			singletons = Collections.unmodifiableSet(s);
		}

		return singletons;
	}
}
