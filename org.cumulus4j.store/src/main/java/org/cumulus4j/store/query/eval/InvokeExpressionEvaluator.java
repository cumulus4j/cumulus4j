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

import java.util.Collection;
import java.util.Date;
import java.util.Map;
import java.util.Set;

import org.cumulus4j.store.query.QueryEvaluator;
import org.cumulus4j.store.query.method.MethodEvaluator;
import org.datanucleus.query.expression.InvokeExpression;
import org.datanucleus.query.expression.PrimaryExpression;
import org.datanucleus.query.expression.VariableExpression;
import org.datanucleus.query.symbol.Symbol;

/**
 * Evaluator handling method invocations like <code>Collection.contains(...)</code>.
 *
 * @author Marco หงุ่ยตระกูล-Schulze - marco at nightlabs dot de
 */
public class InvokeExpressionEvaluator
extends AbstractExpressionEvaluator<InvokeExpression>
{
	public InvokeExpressionEvaluator(QueryEvaluator queryEvaluator, AbstractExpressionEvaluator<?> parent, InvokeExpression expression)
	{
		super(queryEvaluator, parent, expression);
	}

	@Override
	protected Set<Long> _queryResultDataEntryIDs(ResultDescriptor resultDescriptor)
	{
		// The invocationTarget is always the left side of the InvokeExpression. It can be one of the following three:
		// 1) PrimaryExpression
		// 2) VariableExpression
		// 3) ParameterExpression
		// 4) InvokeExpression
		// 5) null (for static methods or aggregates)

		if (this.getLeft() instanceof PrimaryExpressionEvaluator) {
			if (!getLeft().getResultSymbols().contains(resultDescriptor.getSymbol()))
				return null;

			// Evaluate the left-hand expression on which we perform the method invocation
			PrimaryExpressionEvaluator primaryEval = (PrimaryExpressionEvaluator) this.getLeft();
			PrimaryExpression primaryExpr = primaryEval.getExpression();
			Class<?> invocationTargetType = getFieldType(primaryExpr);

			// Find the evaluator to use and invoke it
			MethodEvaluator eval = getMethodEvaluatorForMethodOfClass(invocationTargetType, getExpression().getOperation());
			synchronized (eval) {
				if (eval.requiresComparisonArgument()) {
					eval.setCompareToArgument(getCompareToArgument());
				}
				return eval.evaluate(getQueryEvaluator(), this, primaryExpr, resultDescriptor);
			}
		}
		else if (this.getLeft() instanceof VariableExpressionEvaluator) {
			VariableExpressionEvaluator variableExprEval = (VariableExpressionEvaluator) this.getLeft();
			VariableExpression variableExpr = variableExprEval.getExpression();
			Symbol classSymbol = variableExprEval.getExpression().getSymbol();
			if (classSymbol == null)
				throw new IllegalStateException("((VariableExpressionEvaluator)this.getLeft()).getExpression().getSymbol() returned null!");

			// Evaluate the left-hand expression on which we perform the method invocation
			Class<?> invocationTargetType = getQueryEvaluator().getValueType(classSymbol);

			// Find the evaluator to use and invoke it
			MethodEvaluator eval = getMethodEvaluatorForMethodOfClass(invocationTargetType, getExpression().getOperation());
			synchronized (eval) {
				if (eval.requiresComparisonArgument()) {
					eval.setCompareToArgument(getCompareToArgument());
				}
				return eval.evaluate(getQueryEvaluator(), this, variableExpr, resultDescriptor);
			}
		}
		else if (this.getLeft() instanceof ParameterExpressionEvaluator) {
			ParameterExpressionEvaluator paramExprEval = (ParameterExpressionEvaluator) this.getLeft();
			Symbol classSymbol = paramExprEval.getExpression().getSymbol();
			if (classSymbol == null)
				throw new IllegalStateException("((ParameterExpressionEvaluator)this.getLeft()).getExpression().getSymbol() returned null!");

			// Evaluate the left-hand expression on which we perform the method invocation
			Class<?> invocationTargetType = getQueryEvaluator().getValueType(classSymbol);

			// Find the evaluator to use and invoke it
			MethodEvaluator eval = getMethodEvaluatorForMethodOfClass(invocationTargetType, getExpression().getOperation());
			synchronized (eval) {
				if (eval.requiresComparisonArgument()) {
					eval.setCompareToArgument(getCompareToArgument());
				}
				return eval.evaluate(getQueryEvaluator(), this, paramExprEval.getExpression(), resultDescriptor);
			}
		}
		else if (this.getLeft() instanceof InvokeExpressionEvaluator) {
			// TODO support this.getLeft() instanceof InvokeExpressionEvaluator
			throw new UnsupportedOperationException("NYI: this.getLeft() instanceof InvokeExpressionEvaluator");
		}
		else if (this.getLeft() instanceof SubqueryExpressionEvaluator) {
			// TODO support this.getLeft() instanceof SubqueryExpressionEvaluator
			throw new UnsupportedOperationException("NYI: this.getLeft() instanceof SubqueryExpressionEvaluator");
		}
		else if (this.getLeft() == null) {
			// TODO support this.getLeft() == null (static method call)
			throw new UnsupportedOperationException("NYI: this.getLeft() == null");
		}

		throw new UnsupportedOperationException("NYI");
	}

	private MethodEvaluator getMethodEvaluatorForMethodOfClass(Class cls, String method) {
		String className = cls.getName();
		if (Collection.class.isAssignableFrom(cls)) {
			className = Collection.class.getName(); // Sub-types go through Collection
		}
		else if (Map.class.isAssignableFrom(cls)) {
			className = Map.class.getName(); // Sub-types go through Map
		}
		else if (Date.class.isAssignableFrom(cls)) {
			className = Date.class.getName(); // Sub-types go through Date
		}
		return ExpressionHelper.getMethodEvaluatorForMethodOfClass(getQueryEvaluator().getStoreManager(),
				getQueryEvaluator().getClassLoaderResolver(), className, method);
	}

	private Object getCompareToArgument() {
		if (!(getParent() instanceof ComparisonExpressionEvaluator))
			throw new UnsupportedOperationException(this.getExpression().toString() + " needs to be compared to something as it does not have a boolean result! this.getParent() is thus expected to be a ComparisonExpressionEvaluator, but is: " + getParent());

		ComparisonExpressionEvaluator comparisonExpressionEvaluator = (ComparisonExpressionEvaluator) getParent();

		Object compareToArgument;
		if (this == comparisonExpressionEvaluator.getLeft())
			compareToArgument = comparisonExpressionEvaluator.getRightCompareToArgument();
		else if (this == comparisonExpressionEvaluator.getRight())
			compareToArgument = comparisonExpressionEvaluator.getLeftCompareToArgument();
		else
			throw new UnsupportedOperationException("this is neither parent.left nor parent.right!");

		return compareToArgument;
	}
}