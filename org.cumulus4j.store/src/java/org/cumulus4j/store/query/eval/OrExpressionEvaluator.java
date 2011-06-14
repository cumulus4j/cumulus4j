/*
 * Cumulus4j - Securing your data in the cloud - http://cumulus4j.org
 * Copyright (C) 2011 NightLabs Consulting GmbH
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.cumulus4j.store.query.eval;

import java.util.HashSet;
import java.util.Set;

import org.cumulus4j.store.query.QueryEvaluator;
import org.cumulus4j.store.query.QueryHelper;
import org.datanucleus.query.expression.DyadicExpression;
import org.datanucleus.query.expression.Expression;
import org.datanucleus.util.NucleusLogger;

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
 * See {@link NotExpressionEvaluator} as well as
 * <a href="http://en.wikipedia.org/wiki/De_Morgan%27s_laws">De Morgan's laws</a> in wikipedia for details.
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

		if (leftEvaluated && !rightEvaluated) {
			rightResult = QueryHelper.getAllDataEntryIdsForCandidate(getQueryEvaluator().getPersistenceManagerForData(), 
					getQueryEvaluator().getExecutionContext(), getQueryEvaluator().getQuery().getCandidateClass(), getQueryEvaluator().getQuery().isSubclasses());
		}
		else if (!leftEvaluated && rightEvaluated) {
			leftResult = QueryHelper.getAllDataEntryIdsForCandidate(getQueryEvaluator().getPersistenceManagerForData(), 
					getQueryEvaluator().getExecutionContext(), getQueryEvaluator().getQuery().getCandidateClass(), getQueryEvaluator().getQuery().isSubclasses());
		}
		else if (!leftEvaluated && !rightEvaluated) {
			leftResult = QueryHelper.getAllDataEntryIdsForCandidate(getQueryEvaluator().getPersistenceManagerForData(), 
					getQueryEvaluator().getExecutionContext(), getQueryEvaluator().getQuery().getCandidateClass(), getQueryEvaluator().getQuery().isSubclasses());
		}

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
