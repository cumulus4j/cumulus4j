package org.cumulus4j.core.query;

import java.util.Collection;
import java.util.Map;

import javax.jdo.PersistenceManager;

import org.datanucleus.ClassLoaderResolver;
import org.datanucleus.query.compiler.QueryCompilation;
import org.datanucleus.store.query.Query;

public class JDOQueryEvaluator extends QueryEvaluator {

	public JDOQueryEvaluator(
			Query query,
			QueryCompilation compilation, Map<String, Object> parameterValues,
			ClassLoaderResolver clr,
			PersistenceManager pm
	)
	{
		super("JDOQL", query, compilation, parameterValues, clr, pm);
	}

	@Override
	protected Collection<Object> evaluateSubquery(
			Query subquery, QueryCompilation compilation, Object outerCandidate
	)
	{
		JDOQueryEvaluator evaluator = new JDOQueryEvaluator(
				getQuery(), compilation, getParameterValues(), getClassLoaderResolver(), getPersistenceManager()
		);
        // TODO Make use of outer candidate - what the hell is the "outer candidate"??? It's never set according to references!
        return evaluator.execute();
	}
}
