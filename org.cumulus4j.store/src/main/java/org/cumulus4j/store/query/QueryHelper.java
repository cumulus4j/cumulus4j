/*
 * Cumulus4j - Securing your data in the cloud - http://cumulus4j.org
 * Copyright (C) 2011 NightLabs Consulting GmbH
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.cumulus4j.store.query;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.jdo.PersistenceManager;

import org.cumulus4j.store.Cumulus4jStoreManager;
import org.cumulus4j.store.crypto.CryptoContext;
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
	 * @param cryptoContext the crypto-context (containing the {@link ExecutionContext} and other context data + API).
	 * @param pmData PersistenceManager for the backend data
	 * @param candidateCls Candidate class
	 * @param subclasses Whether to include subclasses
	 * @return The data entry ids
	 */
	public static Set<Long> getAllDataEntryIdsForCandidate(CryptoContext cryptoContext, PersistenceManager pmData, Class<?> candidateCls, boolean subclasses) {
		ExecutionContext ec = cryptoContext.getExecutionContext();
		javax.jdo.Query q = pmData.newQuery(DataEntry.class);
		q.setResult("this.dataEntryID");

		Object queryParam_classMeta;
		Set<ClassMeta> classMetas = QueryHelper.getCandidateClassMetas(
				(Cumulus4jStoreManager) ec.getStoreManager(), ec, candidateCls, subclasses
		);
		StringBuilder filter = new StringBuilder();
		filter.append("this.keyStoreRefID == :keyStoreRefID && ");
		if (classMetas.size() == 1) {
			filter.append("this.classMeta == :classMeta");
			queryParam_classMeta = classMetas.iterator().next();
		}
		else {
			filter.append(":classMetas.contains(this.classMeta)");
			queryParam_classMeta = classMetas;
		}
		q.setFilter(filter.toString());

		@SuppressWarnings("unchecked")
		Collection<Object[]> c = (Collection<Object[]>) q.execute(cryptoContext.getKeyStoreRefID(), queryParam_classMeta);
		Set<Long> resultList = new HashSet<Long>(c.size());
		for (Object[] oa : c) {
			resultList.add((Long)oa[0]);
		}
		q.closeAll();
		return resultList;
	}

	/**
	 * Convenience method to return the persistent objects for the classes with the provided ClassMetas.
	 * @param cryptoContext the crypto-context (containing the {@link ExecutionContext} and other context data + API).
	 * @param pmData PersistenceManager for the backend data
	 * @param candidateClassMetas The class metas defining the required classes
	 * @return The persistent objects
	 */
	public static List<Object> getAllPersistentObjectsForCandidateClasses(CryptoContext cryptoContext, PersistenceManager pmData,
			Set<ClassMeta> candidateClassMetas)
	{
		ExecutionContext ec = cryptoContext.getExecutionContext();
		javax.jdo.Query q = pmData.newQuery(DataEntry.class);
		q.setResult("this.classMeta, this.objectID");

		Object queryParam_classMeta;
		StringBuilder filter = new StringBuilder();
		filter.append("this.keyStoreRefID == :keyStoreRefID && ");
		if (candidateClassMetas.size() == 1) {
			filter.append("this.classMeta == :classMeta");
			queryParam_classMeta = candidateClassMetas.iterator().next();
		}
		else {
			filter.append(":classMetas.contains(this.classMeta)");
			queryParam_classMeta = candidateClassMetas;
		}
		q.setFilter(filter.toString());

		@SuppressWarnings("unchecked")
		Collection<Object[]> c = (Collection<Object[]>) q.execute(cryptoContext.getKeyStoreRefID(), queryParam_classMeta);
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