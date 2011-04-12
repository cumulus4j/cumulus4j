package org.cumulus4j.core.query.method;

import java.util.Set;

import org.cumulus4j.core.model.DataEntry;
import org.cumulus4j.core.query.QueryEvaluator;
import org.cumulus4j.core.query.eval.InvokeExpressionEvaluator;
import org.cumulus4j.core.query.eval.ResultDescriptor;
import org.datanucleus.query.expression.PrimaryExpression;

/**
 * Evaluator for a method.
 */
public interface MethodEvaluator
{
	/**
	 * Method to evaluate the provided method invocation.
	 * @param queryEval Query evaluator
	 * @param invokeExprEval Evaluator for the InvokeExpression that this is processing
	 * @param invokedExpr PrimaryExpression on which we are invoking the method
	 * @param resultDesc Result descriptor
	 * @return those {@link DataEntry#getDataEntryID() dataEntryID}s that match the query
	 * criteria for the specified <code>resultSymbol</code> or <code>null</code>, if the symbol is not supported
	 */
	Set<Long> evaluate(QueryEvaluator queryEval, InvokeExpressionEvaluator invokeExprEval, PrimaryExpression invokedExpr, 
			ResultDescriptor resultDesc);
}