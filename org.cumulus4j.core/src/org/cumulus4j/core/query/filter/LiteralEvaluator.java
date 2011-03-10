package org.cumulus4j.core.query.filter;

import java.util.Set;

import org.cumulus4j.core.query.QueryEvaluator;
import org.datanucleus.query.expression.Literal;

public class LiteralEvaluator extends AbstractExpressionEvaluator<Literal>
{
	public LiteralEvaluator(QueryEvaluator queryEvaluator, AbstractExpressionEvaluator<?> parent, Literal expression) {
		super(queryEvaluator, parent, expression);
	}

	@Override
	protected Set<Long> _queryResultDataEntryIDs() {
		throw new UnsupportedOperationException("Cannot evaluate a literal alone!");
	}

	public Object getLiteralValue()
	{
		return getExpression().getLiteral();
	}
}
