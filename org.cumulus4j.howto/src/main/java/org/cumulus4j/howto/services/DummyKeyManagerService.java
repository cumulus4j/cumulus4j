package org.cumulus4j.howto.services;

import java.util.Properties;
import java.util.UUID;

import javax.jdo.JDOHelper;
import javax.jdo.PersistenceManager;
import javax.jdo.PersistenceManagerFactory;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

import org.cumulus4j.howto.BaseService;
import org.cumulus4j.store.crypto.CryptoSession;

@Path("DummyKeyManagerService")
public class DummyKeyManagerService extends BaseService {

	protected static synchronized PersistenceManagerFactory getPersistenceManagerFactory() {
		if (pmf == null) {

			Properties props = loadProperties("datanucleus.properties");
			props.putAll(loadProperties("cumulus4j.properties"));

			props.put("cumulus4j.cryptoManagerID", "dummy");

			pmf = JDOHelper
					.getPersistenceManagerFactory(props);
		}

		return pmf;
	}

	protected PersistenceManager getPersistenceManager() {
		PersistenceManager pm = getPersistenceManagerFactory()
				.getPersistenceManager();

		pm.setProperty(
				CryptoSession.PROPERTY_CRYPTO_SESSION_ID,
				"dummyKeyStoreID_" + UUID.randomUUID() + '*'
						+ UUID.randomUUID());

		return pm;
	}

	@POST
	@Produces(MediaType.TEXT_PLAIN)
	public String testPost() {
		// We enforce a fresh start every time, because we execute sometimes
		// with
		// different key-servers / embedded key-stores:
		if (pmf != null) {
			pmf.close();
			pmf = null;
		}

		PersistenceManager pm = getPersistenceManager();

		return storeEntities(pm);
	}
}
