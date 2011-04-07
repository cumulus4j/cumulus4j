package org.cumulus4j.core.query.eval;

import java.util.Set;

import org.cumulus4j.core.query.QueryEvaluator;
import org.datanucleus.query.expression.DyadicExpression;

/**
 * <p>
 * Evaluator handling NOT (JDOQL "!").
 * </p>
 * <p>
 * It is quite expensive to evaluate a NOT (JDOQL "!") by first querying the normal (non-negated)
 * result and then negating it by querying ALL candidates and finally filtering the normal result
 * out. Therefore, we instead push the negation down the expression-evaluator-tree into the leafs.
 * Thus <code>NotExpressionEvaluator</code> simply calls {@link ResultDescriptor#negate()} and passes the negated
 * <code>ResultDescriptor</code> down the evaluator-tree. All nodes in the tree therefore have to take this
 * negation-flag into account.
 * </p>
 * <p>
 * Example 1: Instead of querying the expensive expression "NOT( AND( a > 5, b <= 12 ) )", the cheaper
 * equivalent "OR( a <= 5, b > 12 )" is used.
 * </p>
 * <p>
 * Example 2: Instead of "NOT( OR( NOT( AND( a > 5, b <= 12 ) ), c > 3) )" the equivalent "AND( OR( a <= 5, b > 12 ), c <= 3 )"
 * is executed.
 * </p>
 *
 * @author Marco หงุ่ยตระกูล-Schulze - marco at nightlabs dot de
 */
public class NotExpressionEvaluator extends AbstractExpressionEvaluator<DyadicExpression>
{
	public NotExpressionEvaluator(QueryEvaluator queryEvaluator, AbstractExpressionEvaluator<?> parent, DyadicExpression expression)
	{
		super(queryEvaluator, parent, expression);
	}

	@Override
	protected Set<Long> _queryResultDataEntryIDs(ResultDescriptor resultDescriptor)
	{
		if (getLeft() != null && getRight() != null)
			throw new UnsupportedOperationException("Both left and right are assigned - one of them must be null!");

		if (getLeft() != null)
			return getLeft().queryResultDataEntryIDs(resultDescriptor.negate());

		if (getRight() != null)
			return getRight().queryResultDataEntryIDs(resultDescriptor.negate());

		throw new UnsupportedOperationException("Both left and right are null - one of them must be assigned!");
	}
}
