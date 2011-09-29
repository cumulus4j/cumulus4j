package org.cumulus4j.benchmark.scenario.relation;

import javax.ws.rs.Consumes;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

import org.cumulus4j.benchmark.framework.BaseScenario;
import org.cumulus4j.benchmark.scenario.inheritance.InheritanceScenarioService;

/**
 * @author Jan Mortensen - jmortensen at nightlabs dot de
 */
@Path(InheritanceScenarioService.PATH)
@Consumes({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
@Produces({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
public class RelationScenarioService extends BaseScenario<SimplePerson> {

	@Override
	protected SimplePerson createNewObject() {

		return new SimplePerson();
	}

	@Override
	protected Class<SimplePerson> getObjectClass() {

		return SimplePerson.class;
	}

}
