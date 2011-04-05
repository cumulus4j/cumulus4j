package org.cumulus4j.core.query.eval;

import java.util.HashSet;
import java.util.Set;

import org.cumulus4j.core.query.QueryEvaluator;
import org.datanucleus.query.expression.DyadicExpression;

/**
 * Evaluator handling the boolean operation ||.
 *
 * @author Marco หงุ่ยตระกูล-Schulze - marco at nightlabs dot de
 */
public class OrExpressionEvaluator
extends AbstractExpressionEvaluator<DyadicExpression>
{
	public OrExpressionEvaluator(QueryEvaluator queryEvaluator, AbstractExpressionEvaluator<?> parent, DyadicExpression expression) {
		super(queryEvaluator, parent, expression);
	}

	@Override
	protected Set<Long> _queryResultDataEntryIDs(ResultDescriptor resultDescriptor)
	{
		if (resultDescriptor.isNegated())
			return new AndExpressionEvaluator(getQueryEvaluator(), getParent(), getExpression())._queryResultDataEntryIDsIgnoringNegation(resultDescriptor);
		else
			return _queryResultDataEntryIDsIgnoringNegation(resultDescriptor);
	}

	protected Set<Long> _queryResultDataEntryIDsIgnoringNegation(ResultDescriptor resultDescriptor)
	{
		if (getLeft() == null)
			throw new IllegalStateException("getLeft() == null");

		if (getRight() == null)
			throw new IllegalStateException("getRight() == null");

		Set<Long> leftResult = getLeft().queryResultDataEntryIDs(resultDescriptor);
		Set<Long> rightResult = getRight().queryResultDataEntryIDs(resultDescriptor);

		if (leftResult != null && rightResult != null) {
			Set<Long> result = new HashSet<Long>(leftResult.size() + rightResult.size());
			result.addAll(leftResult);
			result.addAll(rightResult);
			return result;
		}
		else if (leftResult != null)
			return leftResult;
		else
			return rightResult;
	}
}
