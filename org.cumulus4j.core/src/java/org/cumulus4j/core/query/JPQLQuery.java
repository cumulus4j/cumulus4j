package org.cumulus4j.core.query;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.jdo.PersistenceManager;

import org.cumulus4j.core.Cumulus4jStoreManager;
import org.cumulus4j.core.model.ClassMeta;
import org.datanucleus.query.evaluator.JPQLEvaluator;
import org.datanucleus.query.evaluator.JavaQueryEvaluator;
import org.datanucleus.store.ExecutionContext;
import org.datanucleus.store.connection.ManagedConnection;
import org.datanucleus.store.query.AbstractJPQLQuery;

/**
 * JPQL query implementation. Delegates to the query-language-agnostic {@link QueryEvaluator} via
 * its thin wrapper sub-class {@link JDOQueryEvaluator}.
 */
public class JPQLQuery extends AbstractJPQLQuery {

	public JPQLQuery(ExecutionContext ec) {
		super(ec);
	}

	public JPQLQuery(ExecutionContext ec, AbstractJPQLQuery query) {
		super(ec, query);
	}

	public JPQLQuery(ExecutionContext ec, String query) {
		super(ec, query);
	}

	/* (non-Javadoc)
	 * @see org.datanucleus.store.query.Query#performExecute(java.util.Map)
	 */
	@Override
	protected Object performExecute(Map parameters) {
		ManagedConnection mconn = ec.getStoreManager().getConnection(ec);
		try {
			PersistenceManager pm = (PersistenceManager) mconn.getConnection();
			boolean inMemory = evaluateInMemory();

			boolean inMemory_applyFilter = true;
      List<Object> candidates = null;
			if (this.candidateCollection != null) {
				if (candidateCollection.isEmpty()) {
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
					candidates = QueryHelper.getAllPersistentObjectsForCandidateClasses(pm, ec, classMetas);
				}
				else {
					// We filter already in our JDOQueryEvaluator => don't filter again in-memory!
					inMemory_applyFilter = false;

					@SuppressWarnings("unchecked")
					Map<String, Object> parameterValues = parameters;
					JDOQueryEvaluator queryEvaluator = new JDOQueryEvaluator(this, compilation, parameterValues, clr, pm);
					candidates = queryEvaluator.execute();
				}
			}

			// Evaluate any remaining query components in-memory
			JavaQueryEvaluator evaluator = new JPQLEvaluator(
					this, candidates, compilation, parameters, ec.getClassLoaderResolver()
			);
			Collection<?> results = evaluator.execute(inMemory_applyFilter, true, true, true, true);
			return results;
		} finally {
			mconn.release();
		}
	}
}