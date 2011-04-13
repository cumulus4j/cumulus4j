package org.cumulus4j.core.query;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.jdo.PersistenceManager;

import org.cumulus4j.core.Cumulus4jStoreManager;
import org.cumulus4j.core.model.ClassMeta;
import org.datanucleus.query.evaluator.JDOQLEvaluator;
import org.datanucleus.query.evaluator.JavaQueryEvaluator;
import org.datanucleus.store.ExecutionContext;
import org.datanucleus.store.connection.ManagedConnection;
import org.datanucleus.store.query.AbstractJDOQLQuery;

/**
 * JDO query implementation. Delegates to the JDO-agnostic {@link QueryEvaluator} via
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
			PersistenceManager pm = (PersistenceManager) mconn.getConnection();

      boolean inMemory = evaluateInMemory();

			boolean inMemory_applyFilter = true;
      List<Object> candidates = null;
			if (this.candidateCollection != null) {
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
			JavaQueryEvaluator evaluator = new JDOQLEvaluator(
					this, candidates, compilation, parameters, ec.getClassLoaderResolver()
			);
			Collection<?> results = evaluator.execute(inMemory_applyFilter, true, true, true, true);
			return results;
		} finally {
			mconn.release();
		}
	}
}
