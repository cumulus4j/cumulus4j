package org.cumulus4j.benchmark.framework;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import javax.jdo.PersistenceManager;
import javax.jdo.Query;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;

import org.nightlabs.util.Stopwatch;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
*
* @author Jan Mortensen - jmortensen at nightlabs dot de
*
*/
public abstract class AbstractSimpleDatatypeScenario<T extends Entity> implements IScenario {

	private static Logger logger = LoggerFactory.getLogger(AbstractSimpleDatatypeScenario.class);

	private static List<String> results = new ArrayList<String>();

	/**
	 * Creates a new (random) instance of type T.
	 *
	 * @return An instance of type T.
	 */
	protected abstract T createNewObject();

	protected abstract Class<T> getObjectClass();

	@GET
	@Path(WARMUP)
	@Produces(MediaType.TEXT_PLAIN)
	@Override
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

			Stopwatch stopwatch = new Stopwatch();
			stopwatch.start(WARMUP);
			pm.makePersistentAll(objects);
			stopwatch.stop(WARMUP);
			results.add(stopwatch.createHumanReport(true));

			pm.currentTransaction().commit();
		}
		finally{
			if (pm.currentTransaction().isActive())
				pm.currentTransaction().rollback();

			pm.close();
		}

		return "";
	}

	int bulkCount = 10;

	@GET
	@Path(STORE_SINGLE_OBJECT)
	@Produces(MediaType.TEXT_PLAIN)
	@Override
	public String storeSingleObject(
			@QueryParam("cryptoManagerID") String cryptoManagerID,
			@QueryParam("cryptoSessionID") String cryptoSessionID
	)
	{
		if (cryptoManagerID == null || cryptoManagerID.isEmpty())
			cryptoManagerID = "keyManager";

		PersistenceManager pm = PersistenceManagerProvider.sharedInstance().getPersistenceManager(cryptoManagerID, cryptoSessionID);

		T object = createNewObject();

		try{
			pm.currentTransaction().begin();

			Stopwatch stopwatch = new Stopwatch();
			stopwatch.start(STORE_SINGLE_OBJECT);
			pm.makePersistent(object);
			stopwatch.stop(STORE_SINGLE_OBJECT);
			results.add(stopwatch.createHumanReport(true));

			pm.currentTransaction().commit();
		}
		finally{
			if (pm.currentTransaction().isActive())
				pm.currentTransaction().rollback();

			pm.close();
		}

		return "";
	}

	@GET
	@Path(LOAD_SINGLE_OBJECT)
	@Produces(MediaType.TEXT_PLAIN)
	@Override
	public String loadSingleObject(
			@QueryParam("cryptoManagerID") String cryptoManagerID,
			@QueryParam("cryptoSessionID") String cryptoSessionID
	)
	{
		if(cryptoManagerID == null || cryptoManagerID.isEmpty())
			cryptoManagerID = "keyManager";

		PersistenceManager pm = PersistenceManagerProvider.sharedInstance().getPersistenceManager(cryptoManagerID, cryptoSessionID);

		long objectId = getRandomObjectId(cryptoManagerID, cryptoSessionID);

		try{
			pm.currentTransaction().begin();

			Stopwatch stopwatch = new Stopwatch();
			stopwatch.start(LOAD_SINGLE_OBJECT);
			pm.getObjectById(getObjectClass(), objectId);
			stopwatch.stop(LOAD_SINGLE_OBJECT);
			results.add(stopwatch.createHumanReport(true));

			pm.currentTransaction().commit();
		}
		finally{
			if (pm.currentTransaction().isActive())
				pm.currentTransaction().rollback();

			pm.close();
		}

		return "";
	}

	@GET
	@Path(LOAD_ALL_OBJECTS)
	@Produces(MediaType.TEXT_PLAIN)
	@Override
	public String loadAllObjects(
			@QueryParam("cryptoManagerID") String cryptoManagerID,
			@QueryParam("cryptoSessionID") String cryptoSessionID
	){
		if(cryptoManagerID == null || cryptoManagerID.isEmpty())
			cryptoManagerID = "keyManager";

		PersistenceManager pm = PersistenceManagerProvider.sharedInstance().getPersistenceManager(cryptoManagerID, cryptoSessionID);

		try{
			pm.currentTransaction().begin();

			Query q = pm.newQuery(getObjectClass());
			Stopwatch stopwatch = new Stopwatch();
			stopwatch.start(LOAD_ALL_OBJECTS);
			logger.info(q.execute().toString());
			stopwatch.stop(LOAD_ALL_OBJECTS);
			results.add(stopwatch.createHumanReport(true));

			pm.currentTransaction().commit();
		}
		finally{
			if (pm.currentTransaction().isActive())
				pm.currentTransaction().rollback();

			pm.close();
		}

		logger.info(getResults().toString());

		return "";
	}

	@GET
	@Path(BULK_LOAD_OBJECTS)
	@Produces(MediaType.TEXT_PLAIN)
	@Override
	public String bulkLoadObjects(
			@QueryParam("cryptoManagerID") String cryptoManagerID,
			@QueryParam("cryptoSessionID") String cryptoSessionID
	){
		if (cryptoManagerID == null || cryptoManagerID.isEmpty())
			cryptoManagerID = "keyManager";

		long[] ids = getRandomObjectIds(cryptoManagerID, cryptoSessionID, bulkCount);
		List<Long> ids2 = new ArrayList<Long>();
		for(long l : ids)
			ids2.add(l);

		PersistenceManager pm = PersistenceManagerProvider.sharedInstance().getPersistenceManager(cryptoManagerID, cryptoSessionID);

		try{
			pm.currentTransaction().begin();

			Query q = pm.newQuery("select from " + getObjectClass().getName() + " where :ids2.contains(id)");
			Stopwatch stopwatch = new Stopwatch();
			stopwatch.start(BULK_LOAD_OBJECTS);
			q.execute(ids2);
			stopwatch.stop(BULK_LOAD_OBJECTS);
			results.add(stopwatch.createHumanReport(true));

			pm.currentTransaction().commit();
		}
		finally{
			if (pm.currentTransaction().isActive())
				pm.currentTransaction().rollback();

			pm.close();
		}

		return "";
	}

	@GET
	@Path(BULK_STORE_OBJECTS)
	@Produces(MediaType.TEXT_PLAIN)
	@Override
	public String bulkStoreObjects(
			@QueryParam("cryptoManagerID") String cryptoManagerID,
			@QueryParam("cryptoSessionID") String cryptoSessionID
	){
		if (cryptoManagerID == null || cryptoManagerID.isEmpty())
			cryptoManagerID = "keyManager";

		PersistenceManager pm = PersistenceManagerProvider.sharedInstance().getPersistenceManager(cryptoManagerID, cryptoSessionID);

		Entity[] objects = new Entity[bulkCount];

		for(int i = 0; i < bulkCount; i++){
			objects[i] = createNewObject();
		}

		try{
			pm.currentTransaction().begin();

			Stopwatch stopwatch = new Stopwatch();
			stopwatch.start(BULK_STORE_OBJECTS);
			pm.makePersistentAll(objects);
			stopwatch.stop(BULK_STORE_OBJECTS);
			results.add(stopwatch.createHumanReport(true));

			pm.currentTransaction().commit();
		}
		finally{
			if (pm.currentTransaction().isActive())
				pm.currentTransaction().rollback();

			pm.close();
		}

//		logger.info("After call of bulkStoreObjects the database contains the following objects: " + getAllObjects(cryptoManagerID, cryptoSessionID));

		return "";
	}

	@Override
	public List<String> getResults(){

		return results;
	}

	private long getRandomObjectId(
			String cryptoManagerID,
			String cryptoSessionID
	)
	{
		Collection<T> objects = getAllObjects(cryptoManagerID, cryptoSessionID);
		Entity[] allObjects = new Entity[1];
		allObjects = objects.toArray(allObjects);

		return allObjects[(int)(Math.random() * allObjects.length)].getId();
	}

	private long[] getRandomObjectIds(
			String cryptoManagerID,
			String cryptoSessionID,
			int count
	)
	{
		Collection<T> objects = getAllObjects(cryptoManagerID, cryptoSessionID);
		Entity[] allObjects = new Entity[objects.size()];
		allObjects = objects.toArray(allObjects);

		long[] result = new long[count];

		for(int i = 0; i < count; i++){
			result[i] = (allObjects[(int)(Math.random() * allObjects.length)].getId());
		}

		return result;
	}

	private Collection<T> getAllObjects(
			String cryptoManagerID,
			String cryptoSessionID
			){
		if(cryptoManagerID == null || cryptoManagerID.isEmpty())
			cryptoManagerID = "keyManager";

		PersistenceManager pm = PersistenceManagerProvider.sharedInstance().getPersistenceManager(cryptoManagerID, cryptoSessionID);

		Collection<T> result;

		try{
			pm.currentTransaction().begin();
			Query q = pm.newQuery(getObjectClass());
			@SuppressWarnings("unchecked")
			Collection<T> resultTemp = (Collection<T>)q.execute();
			result = resultTemp;
			result = pm.detachCopyAll(result);
			pm.currentTransaction().commit();
		}
		finally{
			if (pm.currentTransaction().isActive())
				pm.currentTransaction().rollback();

			pm.close();
		}

		return result;
	}
}