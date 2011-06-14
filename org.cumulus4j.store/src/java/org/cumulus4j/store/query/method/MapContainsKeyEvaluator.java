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
package org.cumulus4j.store.query.method;

import java.util.Set;

import org.cumulus4j.store.model.FieldMetaRole;
import org.cumulus4j.store.query.QueryEvaluator;
import org.cumulus4j.store.query.eval.ExpressionHelper;
import org.cumulus4j.store.query.eval.InvokeExpressionEvaluator;
import org.cumulus4j.store.query.eval.ResultDescriptor;
import org.datanucleus.query.QueryUtils;
import org.datanucleus.query.expression.Expression;
import org.datanucleus.query.expression.Literal;
import org.datanucleus.query.expression.ParameterExpression;
import org.datanucleus.query.expression.PrimaryExpression;
import org.datanucleus.query.expression.VariableExpression;

/**
 * Evaluator for <pre>Map.containsKey(key)</pre>
 */
public class MapContainsKeyEvaluator extends AbstractMethodEvaluator
{
	/* (non-Javadoc)
	 * @see org.cumulus4j.store.query.method.MethodEvaluator#evaluate(org.cumulus4j.store.query.QueryEvaluator, org.datanucleus.query.expression.InvokeExpression, org.datanucleus.query.expression.Expression, org.cumulus4j.store.query.eval.ResultDescriptor)
	 */
	@Override
	public Set<Long> evaluate(QueryEvaluator queryEval, InvokeExpressionEvaluator invokeExprEval, 
			Expression invokedExpr, ResultDescriptor resultDesc) {
		if (invokeExprEval.getExpression().getArguments().size() != 1)
			throw new IllegalStateException("containsKey(...) expects exactly one argument, but there are " + 
					invokeExprEval.getExpression().getArguments().size());

		if (invokedExpr instanceof PrimaryExpression) {
			// Evaluate the invoke argument
			Expression invokeArgExpr = invokeExprEval.getExpression().getArguments().get(0);
			Object invokeArgument;
			if (invokeArgExpr instanceof Literal)
				invokeArgument = ((Literal)invokeArgExpr).getLiteral();
			else if (invokeArgExpr instanceof ParameterExpression)
				invokeArgument = QueryUtils.getValueForParameterExpression(queryEval.getParameterValues(), (ParameterExpression)invokeArgExpr);
			else if (invokeArgExpr instanceof VariableExpression)
				return new ExpressionHelper.ContainsVariableResolver(
						queryEval, (PrimaryExpression) invokedExpr, FieldMetaRole.mapKey, (VariableExpression) invokeArgExpr,
						resultDesc.isNegated()
				).query();
			else
				throw new UnsupportedOperationException("NYI");

			return new ExpressionHelper.ContainsConstantResolver(
					queryEval, (PrimaryExpression) invokedExpr, FieldMetaRole.mapKey, invokeArgument,
					resultDesc.isNegated()
			).query();
		}
		else {
			throw new UnsupportedOperationException("NYI invocation of Map.containsKey on a variable");
		}
	}
}