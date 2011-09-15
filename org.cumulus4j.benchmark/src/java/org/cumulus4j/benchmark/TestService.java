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

import java.security.SecureRandom;

import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;

import org.cumulus4j.benchmark.entities.Person;
import org.cumulus4j.benchmark.framework.Service;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Path("Test")
@Consumes({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
@Produces({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
public class TestService extends Service
{

	private static final Logger logger = LoggerFactory
	.getLogger(TestService.class);
	
	private static SecureRandom random = new SecureRandom();
	
	public TestService(){
		super();
	}

	@POST
	@Produces(MediaType.TEXT_PLAIN)
	public String testPost(
			@QueryParam("cryptoManagerID") String cryptoManagerID,
			@QueryParam("cryptoSessionID") String cryptoSessionID
	)
	{
//
//		if (cryptoManagerID == null || cryptoManagerID.isEmpty())
//			cryptoManagerID = "keyManager";

//		PersistenceManager pm = getPersistenceManager(cryptoManagerID, cryptoSessionID);
		
//		try {
				
//			Stopwatch stopwatch = new Stopwatch();
//			stopwatch.start(PERSONS_TO_CREATE);
//			pm.getExtent(Person.class);
			
//			Person person = new Person();
			
			for(int i = 0; i < objectCount; i++){
				Person p1 = new Person(System.currentTimeMillis() + "-"+ Long.toString(random.nextLong(), 36), 
						System.currentTimeMillis() + "-" + Long.toString(random.nextLong(), 36));
				p1.setMoney(System.currentTimeMillis());
				logger.info("---------------------------");
				logger.info(p1.toString());
				logger.info("---------------------------");
//				stopwatch.start("first Person"+i);
//				stopwatch.start("create Person");
				ModelDAO.sharedInstance().storePerson(cryptoManagerID, cryptoSessionID, p1);
//				pm.currentTransaction().begin();
//				pm.makePersistent(p1);
//				pm.currentTransaction().commit();
//				logger.info("Person "+i);
//				stopwatch.stop("create Person");
//				stopwatch.stop("first Person"+i);
				
//				person = p1;
			}
			
//			stopwatch.stop(PERSONS_TO_CREATE);
			
//			pm.currentTransaction().begin();
////			pm.getExtent(Entity.class);
//			pm.makePersistent(new Entity(random.nextInt()));
//			pm.currentTransaction().commit();
			
//			pm.currentTransaction().begin();
//			stopwatch.start("delete");
//			pm.deletePersistent(person);
//			stopwatch.stop("delete");
//			pm.currentTransaction().commit();
			
//			pm.currentTransaction().begin();
//			stopwatch.start("update");
//			Person pp = pm.detachCopy(person);
//			pp.setFirstName("Johnny");
//			pp.setLastName("Bohnny");
//			pm.makePersistent(pp);
//			stopwatch.start("update");
//			pm.currentTransaction().commit();
			
//			stopwatch.start("get all");
//			pm.currentTransaction().begin();
//			Extent<Person> e = pm.getExtent(Person.class);
//			Iterator<Person> iter = e.iterator();
//			if(iter.hasNext()){
//				logger.info("---------------------------");
//				logger.info("---------------------------");
//				logger.info("---------------------------");
//				logger.info("---------------------------");
//				logger.info(((Person)iter.next()).toString());
//				logger.info("---------------------------");
//				logger.info("---------------------------");
//				logger.info("---------------------------");
//				logger.info("---------------------------");
//			}
//			Query q = pm.newQuery(Person.class);
//			logger.info("---------------------------");
//			logger.info("---------------------------");
//			logger.info(q.execute().toString());
//			logger.info("---------------------------");
//			logger.info("---------------------------");
//			pm.currentTransaction().commit();
//			stopwatch.stop("get all");
//			
//			logger.info(stopwatch.createHumanReport(false));

//			return "OK: " + this.getClass().getName() + "\n\nSome persons:\n" + resultSB;
			return "OK:";// + stopwatch.createHumanReport(true);
//		} finally {
//			if (pm.currentTransaction().isActive())
//				pm.currentTransaction().rollback();
//
//			pm.close();
//		}
	}
	
	@GET
	@Path("getAllPersons")
	@Produces(MediaType.TEXT_PLAIN)
	public String geAllPersons(			
			@QueryParam("cryptoManagerID") String cryptoManagerID,
			@QueryParam("cryptoSessionID") String cryptoSessionID
	)
	{

//		if (cryptoManagerID == null || cryptoManagerID.isEmpty())
//			cryptoManagerID = "keyManager";
//
//		PersistenceManager pm = getPersistenceManager(cryptoManagerID, cryptoSessionID);
//		
////		Person p1 = new Person(System.currentTimeMillis() + "-"+ Long.toString(random.nextLong(), 36), 
////				System.currentTimeMillis() + "-" + Long.toString(random.nextLong(), 36));
////		pm.currentTransaction().begin();
////		pm.makePersistent(p1);
////		pm.currentTransaction().commit();
////		
////		Stopwatch stopwatch = new Stopwatch();
////		stopwatch.start("get all");
//		try{
//			pm.currentTransaction().begin();
//			Query q = pm.newQuery(Person.class);
//			String result = q.execute().toString();
//			logger.info("---------------------------");
//			logger.info("---------------------------");
//			logger.info(q.execute().toString());
//			logger.info("---------------------------");
//			logger.info("---------------------------");
//			pm.currentTransaction().commit();
////			stopwatch.stop("get all");
//			
//			return result;
//		}
//		finally{
//			if (pm.currentTransaction().isActive())
//				pm.currentTransaction().rollback();
//
//			pm.close();
//		}
		for(Person person : ModelDAO.sharedInstance().getAllPersons(cryptoManagerID, cryptoSessionID))
			ModelDAO.sharedInstance().storePerson(cryptoManagerID, cryptoSessionID, person);
		return ModelDAO.sharedInstance().getAllPersons(cryptoManagerID, cryptoSessionID).toString();
//		return o(cryptoManagerID, cryptoSessionID);
	}
	
}
