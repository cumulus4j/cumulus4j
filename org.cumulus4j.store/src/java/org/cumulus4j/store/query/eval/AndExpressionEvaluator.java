package org.cumulus4j.store.query.eval;

import java.util.HashSet;
import java.util.Set;

import org.cumulus4j.store.model.DataEntry;
import org.cumulus4j.store.query.QueryEvaluator;
import org.cumulus4j.store.query.QueryHelper;
import org.datanucleus.query.expression.DyadicExpression;
import org.datanucleus.query.expression.Expression;
import org.datanucleus.util.NucleusLogger;

/**
 * <p>
 * Evaluator handling the boolean operation "&amp;&amp;" (AND).
 * </p>
 * <p>
 * Cumulus4j encrypts as much as possible and keeps a minimum of plain-text indexes. The plain-text-indexes
 * index each field separately. This is a compromise between security and searchability. As the index
 * contains only plain-text field-values without any plain-text context (the context is encrypted),
 * it provides the advantage of high security, but at the same time it is not possible to query an
 * AND operation directly in the underlying database.
 * </p>
 * <p>
 * Instead, the AND operation is performed by first querying all {@link DataEntry#getDataEntryID() dataEntryID}s
 * of the {@link #getLeft() left} and the {@link #getRight() right} side and then intersecting these two
 * <code>Set&lt;Long&gt;</code> in memory.
 * </p>
 * <p>
 * If the {@link ResultDescriptor} indicates a {@link ResultDescriptor#isNegated() negation}, this evaluator
 * delegates to the {@link OrExpressionEvaluator}, because a query like
 * "!( a > 5 &amp;&amp; b <= 12 )" is internally converted to "a <= 5 || b > 12" for performance reasons.
 * See {@link NotExpressionEvaluator} as well as
 * <a href="http://en.wikipedia.org/wiki/De_Morgan%27s_laws">De Morgan's laws</a> in wikipedia for details.
 * </p>
 *
 * @author Marco หงุ่ยตระกูล-Schulze - marco at nightlabs dot de
 * @see OrExpressionEvaluator
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

		Set<Long> leftResult = null;
		boolean leftEvaluated = true;
		try {
			leftResult = getLeft().queryResultDataEntryIDs(resultDescriptor);
		}
		catch (UnsupportedOperationException uoe) {
			leftEvaluated = false;
			getQueryEvaluator().setIncomplete();
			NucleusLogger.QUERY.debug("Unsupported operation in LEFT : "+getLeft().getExpression() + " so deferring evaluation to in-memory");
		}

		Set<Long> rightResult = null;
		boolean rightEvaluated = true;
		try {
			rightResult = getRight().queryResultDataEntryIDs(resultDescriptor);
		}
		catch (UnsupportedOperationException uoe) {
			rightEvaluated = false;
			getQueryEvaluator().setIncomplete();
			NucleusLogger.QUERY.debug("Unsupported operation in RIGHT : "+getRight().getExpression() + " so deferring evaluation to in-memory");
		}

		if (!leftEvaluated && !rightEvaluated) {
			// Neither side evaluated so return all data entry ids
			leftResult = QueryHelper.getAllDataEntryIdsForCandidate(getQueryEvaluator().getPersistenceManagerForData(), 
					getQueryEvaluator().getExecutionContext(), getQueryEvaluator().getQuery().getCandidateClass(), 
					getQueryEvaluator().getQuery().isSubclasses());
		}

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
