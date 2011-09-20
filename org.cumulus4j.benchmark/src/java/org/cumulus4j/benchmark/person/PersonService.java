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
package org.cumulus4j.benchmark.person;

import java.security.SecureRandom;

import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;

import org.cumulus4j.benchmark.framework.BaseService;
import org.cumulus4j.benchmark.framework.PropertyHandler;
import org.nightlabs.util.Stopwatch;

/**
 * 
 * @author Jan Mortensen - jmortensen at nightlabs dot de
 *
 */
@Path("Person")
@Consumes({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
@Produces({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
public class PersonService extends BaseService
{
	
	private static SecureRandom random = new SecureRandom();
	
	private String persistObjects(String cryptoManagerID, String cryptoSessionID, String action, int objects){
		
		Stopwatch stopwatch = new Stopwatch();
		for(int i = 0; i < objects; i++){
			Person person = new Person(System.currentTimeMillis() + "-"+ Long.toString(random.nextLong(), 36), 
					System.currentTimeMillis() + "-" + Long.toString(random.nextLong(), 36));

			stopwatch.start(action);
			PersonDAO.sharedInstance().storePerson(cryptoManagerID, cryptoSessionID, person);
			stopwatch.stop(action);
		}		
		
		return stopwatch.createHumanReport(true);
	}

	@GET
	@Path("warmup")
	@Produces(MediaType.TEXT_PLAIN)
	public String warmup(
			@QueryParam("cryptoManagerID") String cryptoManagerID,
			@QueryParam("cryptoSessionID") String cryptoSessionID
	)
	{		
//		String without = persistObjects(cryptoManagerID, cryptoSessionID, "warmup", PropertyHandler.WARMUP_OBJECTS);
		
//		PersonDAO.sharedInstance().reconfigure();
		
		return "Warmup with " + PropertyHandler.WARMUP_OBJECTS + " objects:" 
		+ persistObjects(cryptoManagerID, cryptoSessionID, "warmup", PropertyHandler.WARMUP_OBJECTS) + "\n";
//		+ "an without cumulus4j: " + without;
	}
	
	@GET
	@Path("persistPersons")
	@Produces(MediaType.TEXT_PLAIN)
	public String persistPersons(			
			@QueryParam("cryptoManagerID") String cryptoManagerID,
			@QueryParam("cryptoSessionID") String cryptoSessionID
	)
	{
		return "Saving of " + PropertyHandler.TEST_OBJECTS + " objects:" 
		+ persistObjects(cryptoManagerID, cryptoSessionID, "save Person", PropertyHandler.TEST_OBJECTS) + "\n";
	}

//	@GET
//	@Path("getAllPersons")
//	@Produces(MediaType.TEXT_PLAIN)
//	public String getAllPersons(			
//			@QueryParam("cryptoManagerID") String cryptoManagerID,
//			@QueryParam("cryptoSessionID") String cryptoSessionID
//	)
//	{
//		return PersonDAO.sharedInstance().getAllPersons(cryptoManagerID, cryptoSessionID).toString();
//	}
	
	@GET
	@Path("readAllPersons")
	@Produces(MediaType.TEXT_PLAIN)
	public String readAllPersons(
			@QueryParam("cryptoManagerID") String cryptoManagerID,
			@QueryParam("cryptoSessionID") String cryptoSessionID){
		
		Stopwatch stopwatch = new Stopwatch();
		stopwatch.start("reading");
		int count = PersonDAO.sharedInstance().getAllPersons(cryptoManagerID, cryptoSessionID).size();
		stopwatch.stop("reading");
		
		return "Reading of " + count + " objects:"
		+ stopwatch.createHumanReport(true) + "\n";
	}
	
//	@GET
//	@Path("reconfigure")
//	@Produces(MediaType.TEXT_PLAIN)
//	public String reconfigure(){
//		
//		PersonDAO.sharedInstance().reconfigure();
//		return "";
//	}
}
