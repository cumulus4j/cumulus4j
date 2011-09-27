package org.cumulus4j.benchmark.personhalfqueryable;

import javax.ws.rs.Consumes;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

import org.cumulus4j.benchmark.framework.SimpleDatatypeScenario;

/**
*
* @author Jan Mortensen - jmortensen at nightlabs dot de
*
*/
@Path(PersonHalfQueryableScenarioService.PATH)
@Consumes({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
@Produces({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
public class PersonHalfQueryableScenarioService extends SimpleDatatypeScenario<Person>{

	public static final String PATH = "PersonHalfQueryable";

	@Override
	protected Person createNewObject() {

		return new Person();
	}

	@Override
	protected Class<Person> getObjectClass() {

		return Person.class;
	}
}
