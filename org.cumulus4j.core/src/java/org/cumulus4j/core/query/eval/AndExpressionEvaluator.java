package org.cumulus4j.core.query.eval;

import java.util.HashSet;
import java.util.Set;

import org.cumulus4j.core.query.QueryEvaluator;
import org.datanucleus.query.expression.DyadicExpression;

public class AndExpressionEvaluator
extends AbstractExpressionEvaluator<DyadicExpression>
{
	public AndExpressionEvaluator(QueryEvaluator queryEvaluator, AbstractExpressionEvaluator<?> parent, DyadicExpression expression) {
		super(queryEvaluator, parent, expression);
	}

	@Override
	protected Set<Long> _queryResultDataEntryIDs() {
		if (getLeft() == null)
			throw new IllegalStateException("getLeft() == null");

		if (getRight() == null)
			throw new IllegalStateException("getRight() == null");

		if (getLeft().getQueryEvaluator().getCandidateAlias().equals(getRight().getQueryEvaluator().getCandidateAlias())) {
			Set<Long> dataEntryIDs1 = getLeft().queryResultDataEntryIDs();
			Set<Long> dataEntryIDs2 = getRight().queryResultDataEntryIDs();

			// Swap them, if the first set is bigger than the 2nd (to always iterate the smaller set => faster).
			if (dataEntryIDs1.size() > dataEntryIDs2.size()) {
				Set<Long> tmp = dataEntryIDs1;
				dataEntryIDs1 = dataEntryIDs2;
				dataEntryIDs2 = tmp;
			}

			Set<Long> result = new HashSet<Long>(dataEntryIDs1.size());
			for (Long dataEntryID : dataEntryIDs1) {
				if (dataEntryIDs2.contains(dataEntryID))
					result.add(dataEntryID);
			}
			return result;
		}
		else
			throw new UnsupportedOperationException("NYI");
	}
}
