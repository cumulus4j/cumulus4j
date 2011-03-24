package org.cumulus4j.core.query.eval;

import java.util.Collections;
import java.util.Set;

import org.cumulus4j.core.query.QueryEvaluator;
import org.datanucleus.query.expression.VariableExpression;
import org.datanucleus.query.symbol.Symbol;

/**
 * Evaluator representing variables.
 *
 * @author Marco หงุ่ยตระกูล-Schulze - marco at nightlabs dot de
 */
public class VariableExpressionEvaluator extends AbstractExpressionEvaluator<VariableExpression>
{
	public VariableExpressionEvaluator(QueryEvaluator queryEvaluator, AbstractExpressionEvaluator<?> parent, VariableExpression expression)
	{
		super(queryEvaluator, parent, expression);
	}

	@Override
	protected Set<Long> _queryResultDataEntryIDs(ResultDescriptor resultDescriptor)
	{
		throw new UnsupportedOperationException("Cannot evaluate a variable without any context!");
	}

	@Override
	protected Set<Symbol> _getResultSymbols()
	{
		Symbol symbol = getExpression().getSymbol();
		if (symbol == null)
			throw new IllegalStateException("getExpression().getSymbol() returned null!");

		return Collections.singleton(symbol);
	}
}
