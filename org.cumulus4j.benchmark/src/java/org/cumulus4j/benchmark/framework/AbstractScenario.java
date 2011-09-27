package org.cumulus4j.benchmark.framework;

import javax.jdo.PersistenceManager;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;

/**
*
* @author Jan Mortensen - jmortensen at nightlabs dot de
*
*/
public abstract class AbstractScenario<T extends Entity> implements IScenario {

	public static final String WARMUP = "warmup";

	/**
	 * Creates a new (random) instance of type T.
	 *
	 * @return An instance of type T.
	 */
	protected abstract T createNewObject();

	protected abstract Class<T> getObjectClass();


	@Override
	@GET
	@Path(WARMUP)
	@Produces(MediaType.TEXT_PLAIN)
	public String warmup(
			@QueryParam("cryptoManagerID") String cryptoManagerID,
			@QueryParam("cryptoSessionID") String cryptoSessionID
	){
		if (cryptoManagerID == null || cryptoManagerID.isEmpty())
			cryptoManagerID = "keyManager";

		PersistenceManager pm = PersistenceManagerProvider.sharedInstance().getPersistenceManager(cryptoManagerID, cryptoSessionID);

		Entity[] objects = new Entity[20];

		for(int i = 0; i < 20; i++){
			objects[i] = createNewObject();
		}

		try{
			pm.currentTransaction().begin();
			pm.makePersistentAll(objects);
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