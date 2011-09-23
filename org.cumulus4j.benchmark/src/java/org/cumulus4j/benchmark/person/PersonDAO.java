package org.cumulus4j.benchmark.person;

import java.util.Collection;

import javax.jdo.PersistenceManager;
import javax.jdo.Query;

import org.cumulus4j.benchmark.framework.BenchmarkBaseDAO;

/**
 * 
 * @author Jan Mortensen - jmortensen at nightlabs dot de
 *
 */
public class PersonDAO extends BenchmarkBaseDAO{

	private static PersonDAO sharedInstance = null;

	public static PersonDAO sharedInstance(){
		if(sharedInstance == null){
			synchronized (PersonDAO.class) {
				if(sharedInstance == null)
					sharedInstance = new PersonDAO();	
			}
		}

		return sharedInstance;
	}

	public Collection<Person> getAllPersons(String cryptoManagerID, String cryptoSessionID){

		if (cryptoManagerID == null || cryptoManagerID.isEmpty())
			cryptoManagerID = "keyManager";

		PersistenceManager pm = getPersistenceManager(cryptoManagerID, cryptoSessionID);

		try{
			pm.currentTransaction().begin();
			Query q = pm.newQuery(Person.class);
			@SuppressWarnings("unchecked")
			Collection<Person> result = (Collection<Person>)q.execute();
			pm.currentTransaction().commit();
			result = pm.detachCopyAll(result);
			return result;
		}
		finally{
			if (pm.currentTransaction().isActive())
				pm.currentTransaction().rollback();

			pm.close();
		}
	}

	public void storePerson(String cryptoManagerID, String cryptoSessionID, Person person){

		if (cryptoManagerID == null || cryptoManagerID.isEmpty())
			cryptoManagerID = "keyManager";

		PersistenceManager pm = getPersistenceManager(cryptoManagerID, cryptoSessionID);

		try{
			pm.currentTransaction().begin();
			pm.makePersistent(person);
			pm.currentTransaction().commit();
		}
		finally{
			if (pm.currentTransaction().isActive())
				pm.currentTransaction().rollback();

			pm.close();
		}
	}

	//	public void storePersons(String cryptoManagerID, String cryptoSessionID, Collection<Person> persons){
	//		
	//		if (cryptoManagerID == null || cryptoManagerID.isEmpty())
	//			cryptoManagerID = "keyManager";
	//
	//		PersistenceManager pm = getPersistenceManager(cryptoManagerID, cryptoSessionID);
	//
	//		try{
	//			pm.currentTransaction().begin();
	//			pm.makePersistentAll(persons);
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