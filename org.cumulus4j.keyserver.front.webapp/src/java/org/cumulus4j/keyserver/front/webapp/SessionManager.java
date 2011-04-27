package org.cumulus4j.keyserver.front.webapp;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Type;

import javax.ws.rs.core.Context;
import javax.ws.rs.ext.Provider;

import org.cumulus4j.keystore.KeyStore;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sun.jersey.core.spi.component.ComponentContext;
import com.sun.jersey.core.spi.component.ComponentScope;
import com.sun.jersey.spi.inject.Injectable;
import com.sun.jersey.spi.inject.InjectableProvider;

@Provider
public class SessionManager
implements InjectableProvider<Context, Type>, Injectable<SessionManager>
{
	private static final Logger logger = LoggerFactory.getLogger(SessionManager.class);

	private KeyStore keyStore;

	private static File getUserHome()
	{
		String userHome = System.getProperty("user.home"); //$NON-NLS-1$
		if (userHome == null)
			throw new IllegalStateException("System property user.home is not set! This should never happen!"); //$NON-NLS-1$

		return new File(userHome);
	}

	public SessionManager(File keyStoreFile) throws IOException
	{
		logger.info("Creating instance of SessionManager.");

		if (keyStoreFile == null)
			keyStoreFile = new File(new File(getUserHome(), ".cumulus4j"), "cumulus4j.keystore");

		if (!keyStoreFile.getParentFile().isDirectory()) {
			keyStoreFile.getParentFile().mkdirs();
			if (!keyStoreFile.getParentFile().isDirectory())
				throw new IOException("Directory does not exist and could not be created: " + keyStoreFile.getParentFile().getAbsolutePath());
		}

		logger.info("Opening keyStoreFile: {}", keyStoreFile.getAbsolutePath());
		keyStore = new KeyStore(keyStoreFile);
	}

	@Override
	public SessionManager getValue() {
		return this;
	}

	@Override
	public ComponentScope getScope() {
		return ComponentScope.Singleton;
	}

	@SuppressWarnings("rawtypes")
	@Override
	public Injectable getInjectable(ComponentContext ic, Context a, Type c) {
		if (SessionManager.class.equals(c))
			return this;
		else
			return null;
	}

}
