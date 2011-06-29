package org.cumulus4j.keymanager.api.internal.local;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import org.cumulus4j.keymanager.AppServer;
import org.cumulus4j.keymanager.AppServerManager;
import org.cumulus4j.keymanager.api.AuthenticationException;
import org.cumulus4j.keymanager.api.DateDependentKeyStrategyInitParam;
import org.cumulus4j.keymanager.api.KeyManagerAPIConfiguration;
import org.cumulus4j.keymanager.api.KeyManagerAPIInstantiationException;
import org.cumulus4j.keymanager.api.KeyStoreNotEmptyException;
import org.cumulus4j.keymanager.api.Session;
import org.cumulus4j.keymanager.api.internal.AbstractKeyManagerAPI;
import org.cumulus4j.keystore.DateDependentKeyStrategy;
import org.cumulus4j.keystore.KeyStore;
import org.cumulus4j.keystore.UserNotFoundException;

public class LocalKeyManagerAPI extends AbstractKeyManagerAPI
{
	private KeyStore keyStore;
	private AppServerManager appServerManager;
	private Map<String, String> appServerBaseURL2appServerID = Collections.synchronizedMap(new HashMap<String, String>());

	private KeyStore getKeyStore() throws IOException
	{
		KeyStore ks = this.keyStore;

		if (ks == null) {
			ks = new KeyStore(getKeyStoreFile());
			this.keyStore = ks;
		}

		return ks;
	}

	private AppServerManager getAppServerManager() throws IOException
	{
		AppServerManager asm = this.appServerManager;

		if (asm == null) {
			asm = new AppServerManager(getKeyStore());
			this.appServerManager = asm;
		}

		return asm;
	}

	private File getKeyStoreFile() throws IOException
	{
		File keyStoreDir;
		String keyManagerBaseURL = getKeyManagerBaseURL();

		if (keyManagerBaseURL == null) {
			String userHome = System.getProperty("user.home"); //$NON-NLS-1$
			if (userHome == null)
				throw new IllegalStateException("System property user.home is not set! This should never happen!"); //$NON-NLS-1$

			keyStoreDir = new File(userHome, ".cumulus4j");
		}
		else {
			if (!keyManagerBaseURL.startsWith(FILE_URL_PREFIX))
				throw new IllegalStateException("keyManagerBaseURL does not start with \"" + FILE_URL_PREFIX + "\"!!!");

			// see: http://weblogs.java.net/blog/kohsuke/archive/2007/04/how_to_convert.html
			try {
				keyStoreDir = new File(new URI(keyManagerBaseURL));
			} catch (URISyntaxException x) {
				try {
					keyStoreDir = new File(new URL(keyManagerBaseURL).getPath());
				} catch (MalformedURLException e) {
					throw new IllegalStateException("keyManagerBaseURL is malformed: " + e, e);
				}
			}
		}

		if (!keyStoreDir.isDirectory()) {
			keyStoreDir.mkdirs();

			if (!keyStoreDir.isDirectory())
				throw new IOException("Creating directory \"" + keyStoreDir.getAbsolutePath() + "\" failed! Check permissions!");
		}

		return new File(keyStoreDir, getKeyStoreID() + ".keystore");
	}

	@Override
	public void setConfiguration(KeyManagerAPIConfiguration configuration) throws IllegalArgumentException, KeyManagerAPIInstantiationException
	{
		super.setConfiguration(configuration);
		this.keyStore = null;
		this.appServerManager = null;
		appServerBaseURL2appServerID.clear();
	}

	public LocalKeyManagerAPI()
	throws KeyManagerAPIInstantiationException
	{
		// We test here, whether the KeyStore is accessible. If it is not, it means the local stuff is not deployed
		// and it should not be possible to instantiate a LocalKeyManagerAPI.
		KeyStore.class.getConstructors();
	}

	@Override
	public void initDateDependentKeyStrategy(DateDependentKeyStrategyInitParam param)
	throws KeyStoreNotEmptyException, IOException
	{
		try {
			KeyStore keyStore = getKeyStore();
			DateDependentKeyStrategy keyStrategy = new DateDependentKeyStrategy(keyStore);
			keyStrategy.init(getAuthUserName(), getAuthPassword(), param.getKeyActivityPeriodMSec(), param.getKeyStorePeriodMSec());
		} catch (org.cumulus4j.keystore.KeyStoreNotEmptyException e) {
			throw new KeyStoreNotEmptyException(e);
		}
	}

	@Override
	public void putUser(String userName, char[] password) throws AuthenticationException, IOException
	{
		KeyStore keyStore = getKeyStore();
		try {
			try {
				keyStore.createUser(getAuthUserName(), getAuthPassword(), userName, password);
			} catch (org.cumulus4j.keystore.UserAlreadyExistsException e) {
				try {
					keyStore.changeUserPassword(getAuthUserName(), getAuthPassword(), userName, password);
				} catch (UserNotFoundException e1) {
					throw new RuntimeException("What the hell?! Just caught a UserAlreadyExistsException - why is the user not existing now?!", e1);
				}
			}
		} catch (org.cumulus4j.keystore.AuthenticationException e) {
			throw new AuthenticationException(e);
		}

		// If we changed the current user's password, we automatically re-configure this API instance.
		KeyManagerAPIConfiguration conf = getConf();
		if (conf.getAuthUserName() != null && conf.getAuthUserName().equals(userName)) {
			KeyManagerAPIConfiguration newConf = new KeyManagerAPIConfiguration(conf);
			newConf.setAuthPassword(password);
			try {
				setConfiguration(newConf);
			} catch (KeyManagerAPIInstantiationException e) {
				throw new RuntimeException(e); // Shouldn't happen, because we copied the old configuration.
			}
		}
	}

	@Override
	public Session getSession(String appServerBaseURL) throws IOException, AuthenticationException
	{
		try {
			AppServerManager appServerManager = getAppServerManager();
			AppServer appServer;
			synchronized (appServerBaseURL2appServerID) {
				String appServerID = appServerBaseURL2appServerID.get(appServerBaseURL);
				if (appServerID == null) {
					appServer = new AppServer(appServerManager, appServerID, appServerBaseURL);
					appServerManager.putAppServer(appServer);
					appServer.getAppServerID();
				}
				else
					appServer = appServerManager.getAppServerForAppServerID(appServerID);
			}

			// Try to open the session already now, so that we know already here, whether this works.
			appServer.getSessionManager().openSession(getAuthUserName(), getAuthPassword());

			return new LocalSession(this, appServer);
		} catch (org.cumulus4j.keystore.AuthenticationException e) {
			throw new AuthenticationException(e);
		}
	}

}
