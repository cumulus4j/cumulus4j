package org.cumulus4j.core.query.eval;

import java.util.Collections;
import java.util.Set;

import org.cumulus4j.core.query.QueryEvaluator;
import org.datanucleus.query.expression.PrimaryExpression;
import org.datanucleus.query.symbol.Symbol;

/**
 * Evaluator representing {@link PrimaryExpression}s.
 *
 * @author Marco หงุ่ยตระกูล-Schulze - marco at nightlabs dot de
 */
public class PrimaryExpressionEvaluator extends AbstractExpressionEvaluator<PrimaryExpression>
{
	public PrimaryExpressionEvaluator(QueryEvaluator queryEvaluator, AbstractExpressionEvaluator<?> parent, PrimaryExpression expression) {
		super(queryEvaluator, parent, expression);
	}

	@Override
	protected Set<Long> _queryResultDataEntryIDs(Symbol resultSymbol)
	{
		throw new UnsupportedOperationException("It is impossible to get a result set for a PrimaryExpression alone!");
	}

	@Override
	protected Set<Symbol> _getResultSymbols() {
		if (getLeft() instanceof VariableExpressionEvaluator)
			return getLeft().getResultSymbols();

		Symbol symbol = getQueryEvaluator().getCompilation().getSymbolTable().getSymbol(getQueryEvaluator().getCandidateAlias());
		if (symbol == null)
			throw new IllegalStateException("getQueryEvaluator().getCompilation().getSymbolTable().getSymbol(getQueryEvaluator().getCandidateAlias()) returned null! getQueryEvaluator().getCandidateAlias()==\"" + getQueryEvaluator().getCandidateAlias() + "\"");

		return Collections.singleton(symbol);
	}
}
