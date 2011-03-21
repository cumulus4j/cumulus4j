package org.cumulus4j.core.query.eval;

import java.util.Set;

import org.cumulus4j.core.query.QueryEvaluator;
import org.datanucleus.query.QueryUtils;
import org.datanucleus.query.expression.ParameterExpression;
import org.datanucleus.query.symbol.Symbol;

public class ParameterExpressionEvaluator
extends AbstractExpressionEvaluator<ParameterExpression>
{
	public ParameterExpressionEvaluator(QueryEvaluator queryEvaluator, AbstractExpressionEvaluator<?> parent, ParameterExpression expression) {
		super(queryEvaluator, parent, expression);
	}

	@Override
	protected Set<Long> _queryResultDataEntryIDs(Symbol resultSymbol)
	{
		throw new UnsupportedOperationException("It is impossible to get a result set for a ParameterExpression alone!");
	}

	public Object getParameterValue()
	{
		return QueryUtils.getValueForParameterExpression(getQueryEvaluator().getParameterValues(), getExpression());
	}
}
