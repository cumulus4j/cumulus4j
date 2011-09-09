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
package org.cumulus4j.benchmark;

import java.util.Iterator;

import javax.jdo.JDOHelper;
import javax.jdo.PersistenceManager;
import javax.jdo.PersistenceManagerFactory;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;

import org.cumulus4j.benchmark.entities.Person;
import org.cumulus4j.store.crypto.CryptoManager;
import org.cumulus4j.store.crypto.CryptoSession;
import org.cumulus4j.store.test.framework.CleanupUtil;
import org.cumulus4j.store.test.framework.TestUtil;
import org.nightlabs.util.Stopwatch;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Path("Test")
@Consumes({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
@Produces({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
public class TestService
{

	private static final Logger logger = LoggerFactory
	.getLogger(TestService.class);

	private static PersistenceManagerFactory pmf;

	protected static synchronized PersistenceManagerFactory getPersistenceManagerFactory()
	{
		if (pmf == null) {
			try {
				CleanupUtil.dropAllTables();
			} catch (Exception e) {
				throw new RuntimeException(e);
			}
			pmf = JDOHelper.getPersistenceManagerFactory(TestUtil.loadProperties("cumulus4j-test-datanucleus.properties"));
		}

		return pmf;
	}

	protected PersistenceManager getPersistenceManager(String cryptoManagerID, String cryptoSessionID)
	{
		if (cryptoManagerID == null)
			throw new IllegalArgumentException("cryptoManagerID == null");

		if (cryptoSessionID == null)
			throw new IllegalArgumentException("cryptoSessionID == null");

		PersistenceManager pm = getPersistenceManagerFactory().getPersistenceManager();
		pm.setProperty(CryptoManager.PROPERTY_CRYPTO_MANAGER_ID, cryptoManagerID);
		pm.setProperty(CryptoSession.PROPERTY_CRYPTO_SESSION_ID, cryptoSessionID);
		return pm;
	}

	@POST
	@Produces(MediaType.TEXT_PLAIN)
	public String testPost(
			@QueryParam("cryptoManagerID") String cryptoManagerID,
			@QueryParam("cryptoSessionID") String cryptoSessionID
	)
	{
		// We enforce a fresh start every time, because we execute this now with different key-servers / embedded key-stores:
		if (pmf != null) {
			pmf.close();
			pmf = null;
		}

		if (cryptoManagerID == null || cryptoManagerID.isEmpty())
			cryptoManagerID = "keyManager";

		StringBuilder resultSB = new StringBuilder();
		PersistenceManager pm = getPersistenceManager(cryptoManagerID, cryptoSessionID);
		try {

			pm.currentTransaction().begin();
			
			Stopwatch stopwatch = new Stopwatch();
			stopwatch.start("create 1000 persons");
			
			for(int i = 0; i < 40; i++){
				pm.getExtent(Person.class);
				Person p1 = new Person();
				pm.makePersistent(p1);
				logger.info("Person "+i);
			}
				
				
			stopwatch.stop("create 1000 persons");
			logger.info("------------------------------------------------------------------------------------------------------------");
			logger.info(stopwatch.createHumanReport(true));
			logger.info("------------------------------------------------------------------------------------------------------------");

			pm.currentTransaction().commit();

			pm = getPersistenceManager(cryptoManagerID, cryptoSessionID);


			//			 tx2: read some data
			pm.currentTransaction().begin();

			for (Iterator<Person> it = pm.getExtent(Person.class).iterator(); it.hasNext(); ) {
				Person person = it.next();
				resultSB.append(" * ").append(person.getFirstName() + " " + person.getLastName()).append('\n');
			}

			pm.currentTransaction().commit();

			logger.info("OK: " + this.getClass().getName() + "\n\nSome persons:\n" + resultSB);

			return "OK: " + this.getClass().getName() + "\n\nSome persons:\n" + resultSB;
		} finally {
			if (pm.currentTransaction().isActive())
				pm.currentTransaction().rollback();

			pm.close();
		}
	}

	@GET
	@Produces(MediaType.TEXT_PLAIN)
	public String testGet()
	{
		return "OK: " + this.getClass().getName() + ": Use POST on the same URL for a real test.";
	}

}
