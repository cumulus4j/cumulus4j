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

import org.cumulus4j.store.model.DataEntry;
import org.cumulus4j.store.query.QueryEvaluator;
import org.cumulus4j.store.query.eval.InvokeExpressionEvaluator;
import org.cumulus4j.store.query.eval.ResultDescriptor;
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