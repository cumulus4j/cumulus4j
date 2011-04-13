package org.cumulus4j.core.query.method;

import java.util.Set;

import org.cumulus4j.core.model.FieldMetaRole;
import org.cumulus4j.core.query.QueryEvaluator;
import org.cumulus4j.core.query.eval.ExpressionHelper;
import org.cumulus4j.core.query.eval.InvokeExpressionEvaluator;
import org.cumulus4j.core.query.eval.ResultDescriptor;
import org.datanucleus.query.QueryUtils;
import org.datanucleus.query.expression.Expression;
import org.datanucleus.query.expression.Literal;
import org.datanucleus.query.expression.ParameterExpression;
import org.datanucleus.query.expression.PrimaryExpression;
import org.datanucleus.query.expression.VariableExpression;

/**
 * Evaluator for <pre>Map.containsValue(val)</pre>
 */
public class MapContainsValueEvaluator implements MethodEvaluator
{
	/* (non-Javadoc)
	 * @see org.cumulus4j.core.query.method.MethodEvaluator#evaluate(org.cumulus4j.core.query.QueryEvaluator, org.datanucleus.query.expression.InvokeExpression, org.datanucleus.query.expression.PrimaryExpression, org.cumulus4j.core.query.eval.ResultDescriptor)
	 */
	@Override
	public Set<Long> evaluate(QueryEvaluator queryEval, InvokeExpressionEvaluator invokeExprEval, 
			PrimaryExpression invokedExpr, ResultDescriptor resultDesc){
		if (invokeExprEval.getExpression().getArguments().size() != 1)
			throw new IllegalStateException("containsValue(...) expects exactly one argument, but there are " + 
					invokeExprEval.getExpression().getArguments().size());

		// Evaluate the invoke argument
		Expression invokeArgExpr = invokeExprEval.getExpression().getArguments().get(0);
		Object invokeArgument;
		if (invokeArgExpr instanceof Literal)
			invokeArgument = ((Literal)invokeArgExpr).getLiteral();
		else if (invokeArgExpr instanceof ParameterExpression)
			invokeArgument = QueryUtils.getValueForParameterExpression(queryEval.getParameterValues(), (ParameterExpression)invokeArgExpr);
		else if (invokeArgExpr instanceof VariableExpression)
			return new ExpressionHelper.ContainsVariableResolver(
					queryEval, invokedExpr, FieldMetaRole.mapValue, (VariableExpression) invokeArgExpr,
					resultDesc.isNegated()
			).query();
		else
			throw new UnsupportedOperationException("NYI");

		return new ExpressionHelper.ContainsConstantResolver(
				queryEval, invokedExpr, FieldMetaRole.mapValue, invokeArgument,
				resultDesc.isNegated()
		).query();
	}
}