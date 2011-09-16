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
import org.cumulus4j.benchmark.framework.ModelDAO;
import org.cumulus4j.benchmark.framework.PropertyHandler;
import org.nightlabs.util.Stopwatch;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Path("Test")
@Consumes({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
@Produces({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
public class TestService
{

	private Logger logger = LoggerFactory.getLogger(TestService.class);

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
		Stopwatch stopwatch = new Stopwatch();
		for(int i = 0; i < PropertyHandler.OBJECT_COUNT; i++){
			Person p1 = new Person(System.currentTimeMillis() + "-"+ Long.toString(random.nextLong(), 36), 
					System.currentTimeMillis() + "-" + Long.toString(random.nextLong(), 36));

			stopwatch.start("store");
			ModelDAO.sharedInstance().storePerson(cryptoManagerID, cryptoSessionID, p1);
			stopwatch.stop("store");
		}
		
		logger.info(stopwatch.createHumanReport(true));

		return "OK:";
	}

	@GET
	@Path("getAllPersons")
	@Produces(MediaType.TEXT_PLAIN)
	public String getAllPersons(			
			@QueryParam("cryptoManagerID") String cryptoManagerID,
			@QueryParam("cryptoSessionID") String cryptoSessionID
	)
	{
		for(Person person : ModelDAO.sharedInstance().getAllPersons(cryptoManagerID, cryptoSessionID)){
			person.setFirstName("Jonny");
			ModelDAO.sharedInstance().storePerson(cryptoManagerID, cryptoSessionID, person);
		}
		return ModelDAO.sharedInstance().getAllPersons(cryptoManagerID, cryptoSessionID).toString();
	}
}
