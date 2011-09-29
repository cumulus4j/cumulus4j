package org.cumulus4j.benchmark.scenario.inheritance;

import java.util.Random;

import javax.ws.rs.Consumes;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

import org.cumulus4j.benchmark.framework.BaseScenario;

/**
 * @author Jan Mortensen - jmortensen at nightlabs dot de
 */
@Path(InheritanceScenarioService.PATH)
@Consumes({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
@Produces({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
public class InheritanceScenarioService extends
		BaseScenario<InheritanceObject4> {

	public static final String PATH = "InheritanceScenario";

	private static Random random = new Random();

	@Override
	protected InheritanceObject4 createNewObject() {

		return new InheritanceObject4(
				random.nextInt(),
				random.nextInt(),
				random.nextInt(),
				random.nextInt(),
				random.nextInt());
	}

	@Override
	protected Class<InheritanceObject4> getObjectClass() {

		return InheritanceObject4.class;
	}

}
