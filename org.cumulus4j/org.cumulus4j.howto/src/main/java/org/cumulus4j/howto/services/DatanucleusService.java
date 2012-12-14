package org.cumulus4j.howto.services;

import javax.jdo.JDOHelper;
import javax.jdo.PersistenceManager;
import javax.jdo.PersistenceManagerFactory;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

import org.cumulus4j.howto.BaseService;


/**
 * @author jmortensen
 */
@Path("DatanucleusService")
public class DatanucleusService extends BaseService {
	
	/*
	 * This service is just a reference. It uses DataNucleus without Cumulus4j to save data.
	 * You can compare it with the services Cumulus4jDummyKeyManager and Cumulus4jKeyStore
	 * to see what changes when you use Cumulus4j.
	 */

	private static synchronized PersistenceManagerFactory getPersistenceManagerFactory() {
		if (pmf == null) {
			pmf = JDOHelper
					.getPersistenceManagerFactory(loadProperties("datanucleus.properties"));
		}

		return pmf;
	}

	protected PersistenceManager getPersistenceManager() {

		return getPersistenceManagerFactory().getPersistenceManager();
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
