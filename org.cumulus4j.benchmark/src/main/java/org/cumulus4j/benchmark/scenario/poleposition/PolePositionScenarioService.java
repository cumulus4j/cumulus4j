package org.cumulus4j.benchmark.scenario.poleposition;

import javax.jdo.PersistenceManager;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;

import org.cumulus4j.benchmark.framework.BaseScenario;
import org.cumulus4j.benchmark.framework.PersistenceManagerProvider;

@Path(PolePositionScenarioService.PATH)
@Consumes({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
@Produces({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
public class PolePositionScenarioService extends
		BaseScenario<Dummy> {

	public static final String PATH = "PolePosition";

	public static final String START_POLE_POSITION = "startPolePosition";

	@Override
	protected Dummy createNewObject() {

		return new Dummy();
	}

	@Override
	protected Class<Dummy> getObjectClass() {

		return Dummy.class;
	}

	@GET
	@Path(START_POLE_POSITION)
	@Produces(MediaType.TEXT_PLAIN)
	public String startPolePosition(
			@QueryParam("cryptoManagerID") String cryptoManagerID,
			@QueryParam("cryptoSessionID") String cryptoSessionID
	){
		if(cryptoManagerID == null || cryptoManagerID.isEmpty())
			cryptoManagerID = "keyManager";

		PersistenceManager pm = PersistenceManagerProvider.sharedInstance().getPersistenceManager(cryptoManagerID, cryptoSessionID);

		try{
			pm.currentTransaction().begin();

			pm.newQuery(getObjectClass());

			pm.currentTransaction().commit();
		}
		finally{
			if (pm.currentTransaction().isActive())
				pm.currentTransaction().rollback();

			pm.close();
		}

		return "";
	}
}
