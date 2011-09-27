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
@Path(PersonAllQueryableScenarioService.PATH)
@Consumes({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
@Produces({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
public class PersonAllQueryableScenarioService extends SimpleDatatypeScenario<PersonAllQueryable>{

	public static final String PATH = "PersonAllQueryable";

	@Override
	protected PersonAllQueryable createNewObject() {

		return new PersonAllQueryable();
	}

	@Override
	protected Class<PersonAllQueryable> getObjectClass() {

		return PersonAllQueryable.class;
	}
}
