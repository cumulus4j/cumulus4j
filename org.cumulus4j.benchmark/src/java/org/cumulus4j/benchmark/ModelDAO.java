package org.cumulus4j.benchmark;

import java.util.ArrayList;
import java.util.Collection;

import javax.jdo.PersistenceManager;
import javax.jdo.Query;

import org.cumulus4j.benchmark.entities.Person;
import org.cumulus4j.benchmark.framework.Service;

public class ModelDAO extends Service{
	
	
	private static ModelDAO sharedInstance = null;
	
	public static ModelDAO sharedInstance(){
		if(sharedInstance == null){
			synchronized (ModelDAO.class) {
				if(sharedInstance == null)
					sharedInstance = new ModelDAO();	
			}
		}
		
		return sharedInstance;
	}
	
	private ModelDAO(){
		super();
	}

	public Collection<Person> getAllPersons(String cryptoManagerID, String cryptoSessionID){
		
		if (cryptoManagerID == null || cryptoManagerID.isEmpty())
			cryptoManagerID = "keyManager";

		PersistenceManager pm = getPersistenceManager(cryptoManagerID, cryptoSessionID);
		
		try{
			pm.currentTransaction().begin();
			Query q = pm.newQuery(Person.class);
			@SuppressWarnings("unchecked")
			Collection<Person> result = (ArrayList<Person>)q.execute();
			pm.currentTransaction().commit();
			result = pm.detachCopyAll(result);
			return result;//.toString();
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
	
	public void storePersons(String cryptoManagerID, String cryptoSessionID, Collection<Person> persons){
		
		if (cryptoManagerID == null || cryptoManagerID.isEmpty())
			cryptoManagerID = "keyManager";

		PersistenceManager pm = getPersistenceManager(cryptoManagerID, cryptoSessionID);

		try{
			pm.currentTransaction().begin();
			pm.makePersistentAll(persons);
			pm.currentTransaction().commit();
				
		}
		finally{
			if (pm.currentTransaction().isActive())
				pm.currentTransaction().rollback();

			pm.close();
		}
	}
}
