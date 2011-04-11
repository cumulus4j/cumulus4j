package org.cumulus4j.core.query.eval;

import java.util.Set;

import org.cumulus4j.core.query.QueryEvaluator;
import org.datanucleus.query.expression.SubqueryExpression;

public class SubqueryExpressionEvaluator
extends AbstractExpressionEvaluator<SubqueryExpression>
{
	public SubqueryExpressionEvaluator(QueryEvaluator queryEvaluator, AbstractExpressionEvaluator<?> parent, SubqueryExpression expression)
	{
		super(queryEvaluator, parent, expression);
	}

	@Override
	protected Set<Long> _queryResultDataEntryIDs(ResultDescriptor resultDescriptor)
	throws UnsupportedOperationException
	{
		throw new UnsupportedOperationException("NYI");
	}

}
