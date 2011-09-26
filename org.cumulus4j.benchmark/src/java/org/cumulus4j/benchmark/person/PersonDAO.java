package org.cumulus4j.benchmark.person;


/**
 *
 * @author Jan Mortensen - jmortensen at nightlabs dot de
 *
 */
public class PersonDAO{


//	private static final Logger logger = LoggerFactory.getLogger(PersistenceManagerProvider.class);
//
//	private static PersonDAO sharedInstance = null;
//
//	public static PersonDAO sharedInstance(){
//		if(sharedInstance == null){
//			synchronized (PersonDAO.class) {
//				if(sharedInstance == null)
//					sharedInstance = new PersonDAO();
//			}
//		}
//
//		return sharedInstance;
//	}
//
//	public Collection<Person> getAllPersons(String cryptoManagerID, String cryptoSessionID){
//
//		if (cryptoManagerID == null || cryptoManagerID.isEmpty())
//			cryptoManagerID = "keyManager";
//
//		PersistenceManager pm = getPersistenceManager(cryptoManagerID, cryptoSessionID);
//
//		try{
//			pm.currentTransaction().begin();
//			Query q = pm.newQuery(Person.class);
//			@SuppressWarnings("unchecked")
//			Collection<Person> result = (Collection<Person>)q.execute();
//			pm.currentTransaction().commit();
//			result = pm.detachCopyAll(result);
//			return result;
//		}
//		finally{
//			if (pm.currentTransaction().isActive())
//				pm.currentTransaction().rollback();
//
//			pm.close();
//		}
//	}
//
//	public void storePerson(String cryptoManagerID, String cryptoSessionID, Person person){
//
//		if (cryptoManagerID == null || cryptoManagerID.isEmpty())
//			cryptoManagerID = "keyManager";
//
//		PersistenceManager pm = getPersistenceManager(cryptoManagerID, cryptoSessionID);
//
//		try{
//			pm.currentTransaction().begin();
//			logger.info(person.toString());
//			pm.makePersistent(person);
//			pm.currentTransaction().commit();
//		}
//		finally{
//			if (pm.currentTransaction().isActive())
//				pm.currentTransaction().rollback();
//
//			pm.close();
//		}
//	}
}
