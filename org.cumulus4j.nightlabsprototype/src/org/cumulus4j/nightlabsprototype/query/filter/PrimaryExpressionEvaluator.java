package org.cumulus4j.nightlabsprototype.query.filter;

import java.util.Set;

import org.cumulus4j.nightlabsprototype.query.QueryEvaluator;
import org.datanucleus.query.expression.PrimaryExpression;

public class PrimaryExpressionEvaluator extends AbstractExpressionEvaluator<PrimaryExpression>
{
	public PrimaryExpressionEvaluator(QueryEvaluator queryEvaluator, AbstractExpressionEvaluator<?> parent, PrimaryExpression expression) {
		super(queryEvaluator, parent, expression);
	}

	@Override
	protected Set<Long> _queryResultDataEntryIDs() {
		throw new UnsupportedOperationException("It is impossible to get a result set for a PrimaryExpression alone!");
	}

}
