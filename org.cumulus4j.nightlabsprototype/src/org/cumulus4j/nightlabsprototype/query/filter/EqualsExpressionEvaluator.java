package org.cumulus4j.nightlabsprototype.query.filter;

import java.util.Set;

import org.cumulus4j.nightlabsprototype.query.QueryEvaluator;
import org.datanucleus.query.expression.DyadicExpression;

public class EqualsExpressionEvaluator
extends AbstractExpressionEvaluator<DyadicExpression>
{
	public EqualsExpressionEvaluator(QueryEvaluator queryEvaluator, AbstractExpressionEvaluator<?> parent, DyadicExpression expression) {
		super(queryEvaluator, parent, expression);
	}

	@Override
	protected Set<Long> _queryResultDataEntryIDs() {
		// TODO Auto-generated method stub
		return null;
	}
}
