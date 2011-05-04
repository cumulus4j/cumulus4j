package org.cumulus4j.core.query.method;

import java.util.Set;

import org.cumulus4j.core.model.DataEntry;
import org.cumulus4j.core.query.QueryEvaluator;
import org.cumulus4j.core.query.eval.InvokeExpressionEvaluator;
import org.cumulus4j.core.query.eval.ResultDescriptor;
import org.datanucleus.query.expression.Expression;

/**
 * Evaluator for a method.
 */
public interface MethodEvaluator
{
	/**
	 * Method to evaluate the provided method invocation invoking on a PrimaryExpression.
	 * @param queryEval Query evaluator
	 * @param invokeExprEval Evaluator for the InvokeExpression that this is processing
	 * @param invokedExpr Expression on which we are invoking the method
	 * @param resultDesc Result descriptor
	 * @return those {@link DataEntry#getDataEntryID() dataEntryID}s that match the query
	 * criteria for the specified <code>resultSymbol</code> or <code>null</code>, if the symbol is not supported
	 */
	Set<Long> evaluate(QueryEvaluator queryEval, InvokeExpressionEvaluator invokeExprEval, Expression invokedExpr, 
			ResultDescriptor resultDesc);

	/**
	 * Whether this evaluator requires a comparison argument to perform its evaluation.
	 * @return True if needing the comparison argument
	 */
	boolean requiresComparisonArgument();

	/**
	 * Method to set any argument to be compared with (when evaluating method invocation and comparison).
	 * Should be set prior to call of evaluate(...).
	 * @param obj The compared argument
	 */
	void setCompareToArgument(Object obj);
}