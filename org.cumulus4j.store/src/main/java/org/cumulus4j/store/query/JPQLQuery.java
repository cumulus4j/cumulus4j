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
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.jdo.PersistenceManager;

import org.cumulus4j.store.Cumulus4jStoreManager;
import org.cumulus4j.store.PersistenceManagerConnection;
import org.cumulus4j.store.crypto.CryptoContext;
import org.cumulus4j.store.model.ClassMeta;
import org.datanucleus.query.evaluator.JPQLEvaluator;
import org.datanucleus.query.evaluator.JavaQueryEvaluator;
import org.datanucleus.store.ExecutionContext;
import org.datanucleus.store.StoreManager;
import org.datanucleus.store.connection.ManagedConnection;
import org.datanucleus.store.query.AbstractJPQLQuery;
import org.datanucleus.util.NucleusLogger;

/**
 * JPQL query implementation. Delegates to the query-language-agnostic {@link QueryEvaluator} via
 * its thin wrapper sub-class {@link JPAQueryEvaluator}.
 */
public class JPQLQuery extends AbstractJPQLQuery {

	private static final long serialVersionUID = 1L;

// BEGIN DataNucleus 3.0.0-m6 and 3.0.0-release
//	public JPQLQuery(ExecutionContext ec, AbstractJPQLQuery q) {
//		super(ec, q);
//	}
//
//	public JPQLQuery(ExecutionContext ec, String query) {
//		super(ec, query);
//	}
//
//	public JPQLQuery(ExecutionContext ec) {
//		super(ec);
//	}
// END DataNucleus 3.0.0-m6 and 3.0.0-release

// BEGIN DataNucleus 3.0.1 and newer
	public JPQLQuery(StoreManager storeMgr, ExecutionContext ec, AbstractJPQLQuery q) {
		super(storeMgr, ec, q);
	}

	public JPQLQuery(StoreManager storeMgr, ExecutionContext ec, String query) {
		super(storeMgr, ec, query);
	}

	public JPQLQuery(StoreManager storeMgr, ExecutionContext ec) {
		super(storeMgr, ec);
	}
// END DataNucleus 3.0.1 and newer

	@Override
	protected Object performExecute(@SuppressWarnings("rawtypes") Map parameters) {
		ManagedConnection mconn = ec.getStoreManager().getConnection(ec);
		try {
			PersistenceManagerConnection pmConn = (PersistenceManagerConnection)mconn.getConnection();
			PersistenceManager pmData = pmConn.getDataPM();

			Cumulus4jStoreManager storeManager = (Cumulus4jStoreManager) ec.getStoreManager();
			CryptoContext cryptoContext = new CryptoContext(storeManager.getEncryptionCoordinateSetManager(), storeManager.getKeyStoreRefManager(), ec, pmConn);

			boolean inMemory = evaluateInMemory();
			boolean inMemory_applyFilter = true;
			List<Object> candidates = null;
			if (this.candidateCollection != null) {
				if (candidateCollection.isEmpty()) {
					return Collections.EMPTY_LIST;
				}

				@SuppressWarnings("unchecked")
				Collection<? extends Object> c = this.candidateCollection;
				candidates = new ArrayList<Object>(c);
			}
			else {
				// http://sourceforge.net/tracker/?func=detail&aid=3514690&group_id=517465&atid=2102911
				// Must NOT call this.setCandidateClass(...), because 1st it's already assigned and 2nd it clears the compilation.
				// Marco :-)
//				if (candidateExtent != null) {
//					this.setCandidateClass(candidateExtent.getCandidateClass());
//					this.setSubclasses(candidateExtent.hasSubclasses());
//				}

				if (inMemory) {
					// Retrieve all candidates and perform all evaluation in-memory
					Set<ClassMeta> classMetas = QueryHelper.getCandidateClassMetas((Cumulus4jStoreManager) ec.getStoreManager(),
							ec, candidateClass, subclasses);
					candidates = QueryHelper.getAllPersistentObjectsForCandidateClasses(cryptoContext, pmData, classMetas);
				}
				else {
					try
					{
						// Apply filter in datastore
						@SuppressWarnings("unchecked")
						Map<String, Object> parameterValues = parameters;
						JPAQueryEvaluator queryEvaluator = new JPAQueryEvaluator(this, compilation, parameterValues, clr, pmConn, cryptoContext);
						candidates = queryEvaluator.execute();
						if (queryEvaluator.isComplete()) {
							inMemory_applyFilter = false;
						}
						else {
							NucleusLogger.QUERY.debug("Query evaluation of filter in datastore was incomplete so doing further work in-memory");
						}
					}
					catch (UnsupportedOperationException uoe) {
						// Some part of the filter is not yet supported, so fallback to in-memory evaluation
						// Retrieve all candidates and perform all evaluation in-memory
						NucleusLogger.QUERY.info("Query filter is not totally evaluatable in-datastore using Cumulus4j currently, so evaluating in-memory : "+uoe.getMessage());
						Set<ClassMeta> classMetas = QueryHelper.getCandidateClassMetas((Cumulus4jStoreManager) ec.getStoreManager(),
								ec, candidateClass, subclasses);
						candidates = QueryHelper.getAllPersistentObjectsForCandidateClasses(cryptoContext, pmData, classMetas);
					}
				}
			}

			// Evaluate any remaining query components in-memory
			JavaQueryEvaluator evaluator = new JPQLEvaluator(this, candidates, compilation, parameters, ec.getClassLoaderResolver());
			return evaluator.execute(inMemory_applyFilter, true, true, true, true);
		} finally {
			mconn.release();
		}
	}
}