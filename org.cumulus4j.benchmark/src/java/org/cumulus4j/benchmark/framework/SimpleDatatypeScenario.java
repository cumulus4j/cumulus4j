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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
*
* @author Jan Mortensen - jmortensen at nightlabs dot de
*
*/
public abstract class SimpleDatatypeScenario<T extends Entity> extends AbstractScenario<T> {

	private static Logger logger = LoggerFactory.getLogger(SimpleDatatypeScenario.class);

	public static final String STORE_SINGLE_OBJECT = "storeSingleObject";

	public static final String LOAD_SINGLE_RANDOM_OBJECT = "loadSingleObject";

	public static final String LOAD_ALL_OBJECTS = "loadAllObjects";

	public static final String BULK_LOAD_OBJECTS = "bulkLoadObjects";

	public static final String BULK_STORE_OBJECTS = "bulkStoreObjetcts";

	int bulkCount = 10;

	@GET
	@Path(STORE_SINGLE_OBJECT)
	@Produces(MediaType.TEXT_PLAIN)
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
			pm.makePersistent(object);
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
	@Path(LOAD_SINGLE_RANDOM_OBJECT)
	@Produces(MediaType.TEXT_PLAIN)
	public String loadSingleObject(
			@QueryParam("cryptoManagerID") String cryptoManagerID,
			@QueryParam("cryptoSessionID") String cryptoSessionID
	)
	{
		if(cryptoManagerID == null || cryptoManagerID.isEmpty())
			cryptoManagerID = "keyManager";

		PersistenceManager pm = PersistenceManagerProvider.sharedInstance().getPersistenceManager(cryptoManagerID, cryptoSessionID);
//		pm.getFetchPlan().addGroup(Person.FETCH_GROUP_FRIENDS);
//		String[] fetchGroups = new String[]{FetchPlan.DEFAULT, Person.FETCH_GROUP_FRIENDS};
//		pm.getFetchPlan().setGroups(fetchGroups);

		long objectId = getRandomObjectId(cryptoManagerID, cryptoSessionID);

		try{
			pm.currentTransaction().begin();
			T object = pm.getObjectById(getObjectClass(), objectId);
			object = pm.detachCopy(object);
			logger.info(object.toString());
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
	public String loadAllObjects(
			@QueryParam("cryptoManagerID") String cryptoManagerID,
			@QueryParam("cryptoSessionID") String cryptoSessionID
	){
		logger.info("All objects the database contains: " + getAllObjects(cryptoManagerID, cryptoSessionID).toString());

		return "";
	}

	@GET
	@Path(BULK_LOAD_OBJECTS)
	@Produces(MediaType.TEXT_PLAIN)
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
//			for(int i = 0; i < bulkCount; i++){
//				logger.info("Trying to load objets with id: " + ids[i]);
//			}
//			pm.getObjectsById(ids, true);

			Query q = pm.newQuery("select from " + getObjectClass().getName() + " where :ids2.contains(id)");
			Collection<Entity> entities = (Collection<Entity>)q.execute(ids2);

//			for(Entity e : entities)
//				logger.info("Loaded Entity has id: " + e.getId());

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
			pm.makePersistentAll(objects);
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
//		logger.info("All objects: " + objects.toString());
		Entity[] allObjects = new Entity[objects.size()];
		allObjects = objects.toArray(allObjects);

		long[] result = new long[count];

		for(int i = 0; i < count; i++){
			result[i] = (allObjects[(int)(Math.random() * allObjects.length)].getId());
		}

//		for(int i = 0; i < result.length; i++)
//			logger.info("The random object ids: " + result[i]);

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