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
import org.cumulus4j.store.Cumulus4jConnectionFactory.PersistenceManagerConnection;
import org.cumulus4j.store.model.ClassMeta;
import org.datanucleus.query.evaluator.JDOQLEvaluator;
import org.datanucleus.query.evaluator.JavaQueryEvaluator;
import org.datanucleus.store.ExecutionContext;
import org.datanucleus.store.connection.ManagedConnection;
import org.datanucleus.store.query.AbstractJDOQLQuery;
import org.datanucleus.util.NucleusLogger;

/**
 * JDOQL query implementation. Delegates to the query-language-agnostic {@link QueryEvaluator} via
 * its thin wrapper sub-class {@link JDOQueryEvaluator}.
 *
 * @author Marco หงุ่ยตระกูล-Schulze - marco at nightlabs dot de
 */
public class JDOQLQuery extends AbstractJDOQLQuery
{
	private static final long serialVersionUID = 1L;

	public JDOQLQuery(ExecutionContext ec) {
		super(ec);
	}

	public JDOQLQuery(ExecutionContext ec, AbstractJDOQLQuery q) {
		super(ec, q);
	}

	public JDOQLQuery(ExecutionContext ec, String query) {
		super(ec, query);
	}

	@Override
	protected Object performExecute(@SuppressWarnings("unchecked")Map parameters)
	{
		ManagedConnection mconn = ec.getStoreManager().getConnection(ec);
		try {
			PersistenceManagerConnection pmConn = (PersistenceManagerConnection)mconn.getConnection();
			PersistenceManager pmData = pmConn.getDataPM();
			PersistenceManager pmIndex = pmConn.getIndexPM();

      boolean inMemory = evaluateInMemory();
			boolean inMemory_applyFilter = true;
      List<Object> candidates = null;
			if (this.candidateCollection != null) {
        if (candidateCollection.isEmpty())
        {
            return Collections.EMPTY_LIST;
        }

        @SuppressWarnings("unchecked")
				Collection<? extends Object> c = this.candidateCollection;
				candidates = new ArrayList<Object>(c); // TODO is it really necessary to copy? Other query implementations do... Marco.
			}
			else {
				if (candidateExtent != null) {
					this.setCandidateClass(candidateExtent.getCandidateClass());
					this.setSubclasses(candidateExtent.hasSubclasses());
				}

				if (inMemory) {
					// Retrieve all candidates and perform all evaluation in-memory
					Set<ClassMeta> classMetas = QueryHelper.getCandidateClassMetas((Cumulus4jStoreManager) ec.getStoreManager(), 
							ec, candidateClass, subclasses);
					candidates = QueryHelper.getAllPersistentObjectsForCandidateClasses(pmData, ec, classMetas);
				}
				else {
					try
					{
						// Apply filter in datastore
						@SuppressWarnings("unchecked")
						Map<String, Object> parameterValues = parameters;
						JDOQueryEvaluator queryEvaluator = new JDOQueryEvaluator(this, compilation, parameterValues, clr, pmData, pmIndex);
						candidates = queryEvaluator.execute();
						if (queryEvaluator.isComplete()) {
							inMemory_applyFilter = false;
						}
						else {
							NucleusLogger.QUERY.debug("Query evaluation of filter in datastore was incomplete so doing further work in-memory");
						}
					}
					catch (UnsupportedOperationException uoe)
					{
						// Some part of the filter is not yet supported, so fallback to in-memory evaluation
						// Retrieve all candidates and perform all evaluation in-memory
						NucleusLogger.QUERY.info("Query filter is not totally evaluatable in-datastore using Cumulus4j currently, so evaluating in-memory : "+uoe.getMessage());
						Set<ClassMeta> classMetas = QueryHelper.getCandidateClassMetas((Cumulus4jStoreManager) ec.getStoreManager(), 
								ec, candidateClass, subclasses);
						candidates = QueryHelper.getAllPersistentObjectsForCandidateClasses(pmData, ec, classMetas);
					}
				}
			}

			// Evaluate any remaining query components in-memory
			JavaQueryEvaluator evaluator = new JDOQLEvaluator(this, candidates, compilation, parameters, ec.getClassLoaderResolver());
			return evaluator.execute(inMemory_applyFilter, true, true, true, true);
		} finally {
			mconn.release();
		}
	}
}
