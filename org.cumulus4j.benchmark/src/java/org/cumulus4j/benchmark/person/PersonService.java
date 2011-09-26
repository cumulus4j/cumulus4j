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
	public static final String STORE_PERSON = "storePerson";
	public static final String READ_ALL_PERSONS = "readAllPersons";
	public static final String READ_PERSONS_STARTING_WITH = "readPersonsStartingWith";
	
	private static SecureRandom random = new SecureRandom();
	
	@GET
	@Path(WARMUP)
	@Produces(MediaType.TEXT_PLAIN)
	public String warmup(
			@QueryParam("cryptoManagerID") String cryptoManagerID,
			@QueryParam("cryptoSessionID") String cryptoSessionID
	)
	{			
		Stopwatch stopwatch = new Stopwatch();
		Person person = new Person(System.currentTimeMillis() + "-"+ Long.toString(random.nextLong(), 36), 
				System.currentTimeMillis() + "-" + Long.toString(random.nextLong(), 36));

		stopwatch.start("warmup");
		PersonDAO.sharedInstance().storePerson(cryptoManagerID, cryptoSessionID, person);
		stopwatch.stop("warmup");
		
		return "Person " + person.getFirstName() + " " + person.getLastName() + "stored. (warmup)";
	}
	
	@GET
	@Path(STORE_PERSON)
	@Produces(MediaType.TEXT_PLAIN)
	public String storePerson(			
			@QueryParam("cryptoManagerID") String cryptoManagerID,
			@QueryParam("cryptoSessionID") String cryptoSessionID
	)
	{
		Stopwatch stopwatch = new Stopwatch();
		Person person = new Person(System.currentTimeMillis() + "-"+ Long.toString(random.nextLong(), 36), 
				System.currentTimeMillis() + "-" + Long.toString(random.nextLong(), 36));

		stopwatch.start("store");
		PersonDAO.sharedInstance().storePerson(cryptoManagerID, cryptoSessionID, person);
		stopwatch.stop("store");
		
		return "Person " + person.getFirstName() + " " + person.getLastName() + "stored.";
	}

	@GET
	@Path(READ_ALL_PERSONS)
	@Produces(MediaType.TEXT_PLAIN)
	public String readAllPersons(
			@QueryParam("cryptoManagerID") String cryptoManagerID,
			@QueryParam("cryptoSessionID") String cryptoSessionID){
		
		Stopwatch stopwatch = new Stopwatch();
		stopwatch.start("reading all objects");
		PersonDAO.sharedInstance().getAllPersons(cryptoManagerID, cryptoSessionID);
		stopwatch.stop("reading all objects");
		
		return "Reading of " + PersonDAO.sharedInstance().getAllPersons(cryptoManagerID, cryptoSessionID).size() 
		+ " objects:" + stopwatch.createHumanReport(true);
	}
	
	@GET
	@Path(READ_PERSONS_STARTING_WITH)
	@Produces(MediaType.TEXT_PLAIN)
	public String readPersonsStartingWith(
			@QueryParam("cryptoManagerID") String cryptoManagerID,
			@QueryParam("cryptoSessionID") String cryptoSessionID,
			@QueryParam("arg") String arg)
	{
		/*
		 * TODO Query erzeugen die alle personen mit anfangsbuchstaben arg abfragt
		 * 
		 * TODO wenn arg.length() > 1 Exception thrwoen
		 */
		
		return "Das Argument ist: " + arg;
	}
}
