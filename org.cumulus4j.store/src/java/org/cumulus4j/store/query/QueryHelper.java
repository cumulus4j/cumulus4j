package org.cumulus4j.store.query;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.jdo.PersistenceManager;

import org.cumulus4j.store.Cumulus4jStoreManager;
import org.cumulus4j.store.model.ClassMeta;
import org.cumulus4j.store.model.DataEntry;
import org.datanucleus.ClassLoaderResolver;
import org.datanucleus.identity.IdentityUtils;
import org.datanucleus.store.ExecutionContext;

/**
 * Helper methods for querying.
 */
public class QueryHelper {

	/**
	 * Access the data entry ids for a candidate.
	 * @param pmData PersistenceManager for the backend data
	 * @param ec ExecutionContext
	 * @param candidateCls Candidate class
	 * @param subclasses Whether to include subclasses
	 * @return The data entry ids
	 */
	public static Set<Long> getAllDataEntryIdsForCandidate(PersistenceManager pmData, ExecutionContext ec, Class candidateCls, boolean subclasses) {
		javax.jdo.Query q = pmData.newQuery(DataEntry.class);
		q.setResult("this.dataEntryID");

		Object queryParam;
		Set<ClassMeta> classMetas = QueryHelper.getCandidateClassMetas((Cumulus4jStoreManager) ec.getStoreManager(), 
				ec, candidateCls, subclasses);
		if (classMetas.size() == 1) {
			q.setFilter("this.classMeta == :classMeta");
			queryParam = classMetas.iterator().next();
		}
		else {
			q.setFilter(":classMetas.contains(this.classMeta)");
			queryParam = classMetas;
		}

		@SuppressWarnings("unchecked")
		Collection<Object[]> c = (Collection<Object[]>) q.execute(queryParam);
		Set<Long> resultList = new HashSet<Long>(c.size());
		for (Object[] oa : c) {
			resultList.add((Long)oa[0]);
		}
		q.closeAll();
		return resultList;
	}

	/**
	 * Convenience method to return the persistent objects for the classes with the provided ClassMetas.
	 * @param pmData PersistenceManager for the backend data
	 * @param ec ExecutionContext
	 * @param candidateClassMetas The class metas defining the required classes
	 * @return The persistent objects
	 */
	public static List<Object> getAllPersistentObjectsForCandidateClasses(PersistenceManager pmData, ExecutionContext ec, 
			Set<ClassMeta> candidateClassMetas)
	{
		javax.jdo.Query q = pmData.newQuery(DataEntry.class);
		q.setResult("this.classMeta, this.objectID");

		Object queryParam;
		if (candidateClassMetas.size() == 1) {
			q.setFilter("this.classMeta == :classMeta");
			queryParam = candidateClassMetas.iterator().next();
		}
		else {
			q.setFilter(":classMetas.contains(this.classMeta)");
			queryParam = candidateClassMetas;
		}

		@SuppressWarnings("unchecked")
		Collection<Object[]> c = (Collection<Object[]>) q.execute(queryParam);
		List<Object> resultList = new ArrayList<Object>(c.size());
		for (Object[] oa : c) {
			ClassMeta classMeta = (ClassMeta) oa[0];
			String objectIDString = (String) oa[1];
			Object obj = IdentityUtils.getObjectFromIdString(objectIDString, classMeta.getDataNucleusClassMetaData(ec), ec, true);
			resultList.add(obj);
		}
		q.closeAll();
		return resultList;
	}

	/**
	 * Convenience method to return the ClassMeta objects for the specified class (and subclasses if required).
	 * @param storeMgr Cumulus4J StoreManager
	 * @param ec ExecutionContext
	 * @param candidateClass The class required
	 * @param withSubclasses Whether to return subclasses too
	 * @return The ClassMeta objects
	 */
	public static Set<ClassMeta> getCandidateClassMetas(Cumulus4jStoreManager storeMgr, ExecutionContext ec, 
			Class<?> candidateClass, boolean withSubclasses)
	{
		Set<? extends Class<?>> candidateClasses;
		if (withSubclasses) {
			ClassLoaderResolver clr = ec.getClassLoaderResolver();
			HashSet<String> classNames = storeMgr.getSubClassesForClass(candidateClass.getName(), true, clr);
			Set<Class<?>> classes = new HashSet<Class<?>>(classNames.size() + 1);
			classes.add(candidateClass);
			for (String className : classNames) {
				Class<?> clazz = clr.classForName(className);
				classes.add(clazz);
			}
			candidateClasses = classes;
		}
		else
			candidateClasses = Collections.singleton(candidateClass);

		Set<ClassMeta> candidateClassMetas = new HashSet<ClassMeta>(candidateClasses.size());
		for (Class<?> c : candidateClasses) {
			ClassMeta cm = storeMgr.getClassMeta(ec, c);
			candidateClassMetas.add(cm);
		}

		return candidateClassMetas;
	}
}