package org.cumulus4j.core.query.eval;

import java.util.HashSet;
import java.util.Set;

import org.cumulus4j.core.query.QueryEvaluator;
import org.datanucleus.query.expression.DyadicExpression;
import org.datanucleus.query.symbol.Symbol;

public class AndExpressionEvaluator
extends AbstractExpressionEvaluator<DyadicExpression>
{
	public AndExpressionEvaluator(QueryEvaluator queryEvaluator, AbstractExpressionEvaluator<?> parent, DyadicExpression expression) {
		super(queryEvaluator, parent, expression);
	}

	@Override
	protected Set<Long> _queryResultDataEntryIDs(Symbol resultSymbol) {
		if (getLeft() == null)
			throw new IllegalStateException("getLeft() == null");

		if (getRight() == null)
			throw new IllegalStateException("getRight() == null");

		Set<Long> leftResult = getLeft().queryResultDataEntryIDs(resultSymbol);
		Set<Long> rightResult = getRight().queryResultDataEntryIDs(resultSymbol);

		if (leftResult != null && rightResult != null) {
			Set<Long> dataEntryIDs1;
			Set<Long> dataEntryIDs2;

			// Swap them, if the first set is bigger than the 2nd (we want to always iterate the smaller set => faster).
			if (leftResult.size() > rightResult.size()) {
				dataEntryIDs1 = rightResult;
				dataEntryIDs2 = leftResult;
			}
			else {
				dataEntryIDs1 = leftResult;
				dataEntryIDs2 = rightResult;
			}

			Set<Long> result = new HashSet<Long>(dataEntryIDs1.size());
			for (Long dataEntryID : dataEntryIDs1) {
				if (dataEntryIDs2.contains(dataEntryID))
					result.add(dataEntryID);
			}
			return result;
		}
		else if (leftResult != null)
			return leftResult;
		else
			return rightResult;
	}
}
