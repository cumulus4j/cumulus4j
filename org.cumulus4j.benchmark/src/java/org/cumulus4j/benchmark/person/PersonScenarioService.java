package org.cumulus4j.benchmark.person;

import java.util.Random;

import javax.ws.rs.Consumes;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

import org.cumulus4j.benchmark.framework.SimpleDatatypeScenario;

@Path("Person")
@Consumes({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
@Produces({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
public class PersonScenarioService extends SimpleDatatypeScenario<Person>{

	private Random random = new Random();

	@Override
	protected Person createNewObject() {

		return new Person(System.currentTimeMillis() + "-"+ Long.toString(random.nextLong(), 36),
				System.currentTimeMillis() + "-" + Long.toString(random.nextLong(), 36));
	}

	@Override
	protected Class<Person> getObjectClass() {

		return Person.class;
	}
}
