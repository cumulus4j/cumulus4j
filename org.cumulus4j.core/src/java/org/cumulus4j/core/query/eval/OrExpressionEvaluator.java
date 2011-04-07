package org.cumulus4j.core.query.eval;

import java.util.HashSet;
import java.util.Set;

import org.cumulus4j.core.query.QueryEvaluator;
import org.datanucleus.query.expression.DyadicExpression;
import org.datanucleus.query.expression.Expression;

/**
 * <p>
 * Evaluator handling the boolean operation "||" (OR).
 * </p>
 * <p>
 * This evaluator works just like the {@link AndExpressionEvaluator} with the only difference
 * that it unites the partial results instead of intersecting them.
 * </p>
 * <p>
 * If the {@link ResultDescriptor} indicates a {@link ResultDescriptor#isNegated() negation}, this evaluator
 * delegates to the {@link AndExpressionEvaluator}, because a query like
 * "!( a > 5 || b <= 12 )" is internally converted to "a <= 5 &amp;&amp; b > 12" for performance reasons.
 * See {@link NotExpressionEvaluator} for details.
 * </p>
 *
 * @author Marco หงุ่ยตระกูล-Schulze - marco at nightlabs dot de
 * @see AndExpressionEvaluator
 */
public class OrExpressionEvaluator
extends AbstractExpressionEvaluator<DyadicExpression>
{
	private AndExpressionEvaluator negatedExpressionEvaluator;

	public OrExpressionEvaluator(QueryEvaluator queryEvaluator, AbstractExpressionEvaluator<?> parent, DyadicExpression expression) {
		super(queryEvaluator, parent, expression);
	}

	public OrExpressionEvaluator(AndExpressionEvaluator negatedExpressionEvaluator)
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
			return new AndExpressionEvaluator(this)._queryResultDataEntryIDsIgnoringNegation(resultDescriptor);
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
