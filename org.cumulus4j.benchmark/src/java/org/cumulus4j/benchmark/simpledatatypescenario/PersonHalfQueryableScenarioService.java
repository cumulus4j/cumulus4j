package org.cumulus4j.benchmark.simpledatatypescenario;

import javax.ws.rs.Consumes;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;


/**
*
* @author Jan Mortensen - jmortensen at nightlabs dot de
*
*/
@Path(PersonHalfQueryableScenarioService.PATH)
@Consumes({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
@Produces({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
public class PersonHalfQueryableScenarioService extends SimpleDatatypeScenario<PersonHalfQueryable>{

	public static final String PATH = "PersonHalfQueryable";

	@Override
	protected PersonHalfQueryable createNewObject() {

		return new PersonHalfQueryable();
	}

	@Override
	protected Class<PersonHalfQueryable> getObjectClass() {

		return PersonHalfQueryable.class;
	}
}
