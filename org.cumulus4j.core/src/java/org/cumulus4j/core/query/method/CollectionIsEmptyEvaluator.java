package org.cumulus4j.core.query.method;

import java.util.Set;

import org.cumulus4j.core.model.FieldMetaRole;
import org.cumulus4j.core.query.QueryEvaluator;
import org.cumulus4j.core.query.eval.ExpressionHelper;
import org.cumulus4j.core.query.eval.InvokeExpressionEvaluator;
import org.cumulus4j.core.query.eval.ResultDescriptor;
import org.datanucleus.query.expression.Expression;
import org.datanucleus.query.expression.PrimaryExpression;

/**
 * Evaluator for <pre>Collection.isEmpty()</pre>
 */
public class CollectionIsEmptyEvaluator extends AbstractMethodEvaluator {

	/* (non-Javadoc)
	 * @see org.cumulus4j.core.query.method.MethodEvaluator#evaluate(org.cumulus4j.core.query.QueryEvaluator, org.cumulus4j.core.query.eval.InvokeExpressionEvaluator, org.datanucleus.query.expression.Expression, org.cumulus4j.core.query.eval.ResultDescriptor)
	 */
	@Override
	public Set<Long> evaluate(QueryEvaluator queryEval,
			InvokeExpressionEvaluator invokeExprEval, Expression invokedExpr,
			ResultDescriptor resultDesc) {
		if (invokeExprEval.getExpression().getArguments().size() != 0)
			throw new IllegalStateException("isEmpty(...) expects no argument, but there are " + 
					invokeExprEval.getExpression().getArguments().size());

		if (invokedExpr instanceof PrimaryExpression) {
			return new ExpressionHelper.ContainerIsEmptyResolver(
					queryEval, (PrimaryExpression) invokedExpr, FieldMetaRole.collectionElement, resultDesc.isNegated()).query();
		}
		else {
			throw new UnsupportedOperationException("Not Yet Implemented : Collection.isEmpty on a variableExpression collection");
		}
	}
}