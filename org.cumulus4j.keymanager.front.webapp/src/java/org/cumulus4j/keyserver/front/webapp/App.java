package org.cumulus4j.keyserver.front.webapp;

import java.io.File;
import java.io.IOException;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import javax.ws.rs.ApplicationPath;
import javax.ws.rs.core.Application;

import org.cumulus4j.keymanager.AppServerManager;
import org.cumulus4j.keystore.KeyStore;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Marco หงุ่ยตระกูล-Schulze - marco at nightlabs dot de
 */
@ApplicationPath("/")
public class App
extends Application
{
	private static final Logger logger = LoggerFactory.getLogger(App.class);

	private static File getUserHome()
	{
		String userHome = System.getProperty("user.home"); //$NON-NLS-1$
		if (userHome == null)
			throw new IllegalStateException("System property user.home is not set! This should never happen!"); //$NON-NLS-1$

		return new File(userHome);
	}

	private static final Class<?>[] serviceClassesArray = {
		AppServerService.class,
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

	private KeyStore keyStore;

	@Override
	public Set<Object> getSingletons()
	{
		if (singletons == null) {
			try {
				// TODO make keyStoreFile configurable?!
				File keyStoreFile = new File(new File(getUserHome(), ".cumulus4j"), "cumulus4j.keystore");

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
