package org.cumulus4j.core.query.eval;

import java.util.HashSet;
import java.util.Set;

import org.cumulus4j.core.query.QueryEvaluator;
import org.datanucleus.query.expression.DyadicExpression;
import org.datanucleus.query.expression.Expression;

/**
 * Evaluator handling the boolean operation &amp;&amp;.
 *
 * @author Marco หงุ่ยตระกูล-Schulze - marco at nightlabs dot de
 */
public class AndExpressionEvaluator
extends AbstractExpressionEvaluator<DyadicExpression>
{
	private OrExpressionEvaluator negatedExpressionEvaluator;

	public AndExpressionEvaluator(QueryEvaluator queryEvaluator, AbstractExpressionEvaluator<?> parent, DyadicExpression expression) {
		super(queryEvaluator, parent, expression);
	}

	public AndExpressionEvaluator(OrExpressionEvaluator negatedExpressionEvaluator)
	{
		this(negatedExpressionEvaluator.getQueryEvaluator(), negatedExpressionEvaluator.getParent(), negatedExpressionEvaluator.getExpression());
		this.negatedExpressionEvaluator = negatedExpressionEvaluator;
	}

	@Override
	public AbstractExpressionEvaluator<? extends Expression> getLeft() {
		if (negatedExpressionEvaluator != null)
			return negatedExpressionEvaluator.getLeft();

		return super.getLeft();
	}

	@Override
	public AbstractExpressionEvaluator<? extends Expression> getRight() {
		if (negatedExpressionEvaluator != null)
			return negatedExpressionEvaluator.getRight();

		return super.getRight();
	}

	@Override
	protected Set<Long> _queryResultDataEntryIDs(ResultDescriptor resultDescriptor)
	{
		if (resultDescriptor.isNegated())
			return new OrExpressionEvaluator(this)._queryResultDataEntryIDsIgnoringNegation(resultDescriptor);
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
