package org.cumulus4j.store.query.eval;

import java.util.Set;

import org.cumulus4j.store.query.QueryEvaluator;
import org.datanucleus.query.expression.Literal;

/**
 * Evaluator representing literals.
 *
 * @author Marco หงุ่ยตระกูล-Schulze - marco at nightlabs dot de
 */
public class LiteralEvaluator extends AbstractExpressionEvaluator<Literal>
{
	public LiteralEvaluator(QueryEvaluator queryEvaluator, AbstractExpressionEvaluator<?> parent, Literal expression) {
		super(queryEvaluator, parent, expression);
	}

	@Override
	protected Set<Long> _queryResultDataEntryIDs(ResultDescriptor resultDescriptor)
	{
		throw new UnsupportedOperationException("Cannot evaluate a literal alone!");
	}

	public Object getLiteralValue()
	{
		return getExpression().getLiteral();
	}
}
