package org.cumulus4j.nightlabsprototype.query.filter;

import java.util.Set;

import org.cumulus4j.nightlabsprototype.query.QueryEvaluator;
import org.datanucleus.query.expression.InvokeExpression;

public class InvokeExpressionEvaluator
extends AbstractExpressionEvaluator<InvokeExpression>
{
	public InvokeExpressionEvaluator(QueryEvaluator queryEvaluator, AbstractExpressionEvaluator<?> parent, InvokeExpression expression)
	{
		super(queryEvaluator, parent, expression);
	}

	@Override
	protected Set<Long> _queryResultDataEntryIDs() {
		throw new UnsupportedOperationException("Cannot be evaluated alone without loading *ALL* records of a certain type!");
	}
}
