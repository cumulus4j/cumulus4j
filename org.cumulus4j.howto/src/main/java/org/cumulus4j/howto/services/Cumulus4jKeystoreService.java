package org.cumulus4j.howto.services;

import java.util.Properties;

import javax.jdo.JDOHelper;
import javax.jdo.PersistenceManager;
import javax.jdo.PersistenceManagerFactory;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;

import org.cumulus4j.howto.BaseService;
import org.cumulus4j.store.crypto.CryptoManager;
import org.cumulus4j.store.crypto.CryptoSession;

/**
 * 
 * @author jmortensen
 *
 */
@Path("KeyStoreService")
public class Cumulus4jKeystoreService extends BaseService {
	
	/*
	 * This service uses Cumulus4j as is should be used in a productive system.
	 */

	private static synchronized PersistenceManagerFactory getPersistenceManagerFactory() {
		if (pmf == null) {

			Properties props = loadProperties("datanucleus.properties");
			
			// Additionally to the DataNucleus settings we add some settings which
			// configure DataNucleus to use Cumulus4j. 
			props.putAll(loadProperties("cumulus4j.properties"));

			pmf = JDOHelper
					.getPersistenceManagerFactory(props);
		}

		return pmf;
	}

	protected PersistenceManager getPersistenceManager(String cryptoManagerID,
			String cryptoSessionID) {

		if (cryptoManagerID == null)
			throw new IllegalArgumentException("cryptoManagerID == null");

		if (cryptoSessionID == null)
			throw new IllegalArgumentException("cryptoSessionID == null");

		PersistenceManager pm = getPersistenceManagerFactory()
				.getPersistenceManager();

		//The crypto manager id and the crypto manager id have to be set by the client.
		pm.setProperty(CryptoManager.PROPERTY_CRYPTO_MANAGER_ID,
				cryptoManagerID);
		pm.setProperty(CryptoSession.PROPERTY_CRYPTO_SESSION_ID,
				cryptoSessionID);

		return pm;
	}

	@POST
	@Produces(MediaType.TEXT_PLAIN)
	//Get the id of the crypto manager and the crypto session id as parameter.
	public String testPost(
			@QueryParam("cryptoManagerID") String cryptoManagerID,
			@QueryParam("cryptoSessionID") String cryptoSessionID) {
		// We enforce a fresh start every time, because we execute this now with
		// different key-servers / embedded key-stores:
		if (pmf != null) {
			pmf.close();
			pmf = null;
		}

		if (cryptoManagerID == null || cryptoManagerID.isEmpty())
			cryptoManagerID = "keyManager";

		PersistenceManager pm = getPersistenceManager(cryptoManagerID,
				cryptoSessionID);
		return storeEntities(pm);
	}
}
