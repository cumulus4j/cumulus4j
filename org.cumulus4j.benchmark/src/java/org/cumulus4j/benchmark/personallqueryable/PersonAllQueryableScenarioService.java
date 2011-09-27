package org.cumulus4j.benchmark.personallqueryable;

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
@Path(PersonAllQueryableScenarioService.PATH)
@Consumes({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
@Produces({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
public class PersonAllQueryableScenarioService extends SimpleDatatypeScenario<Person>{

	public static final String PATH = "PersonAllQueryable";

	@Override
	protected Person createNewObject() {

		return new Person();
	}

	@Override
	protected Class<Person> getObjectClass() {

		return Person.class;
	}
}
