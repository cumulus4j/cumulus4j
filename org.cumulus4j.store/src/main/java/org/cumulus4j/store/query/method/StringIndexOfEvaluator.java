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

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import javax.jdo.Query;

import org.cumulus4j.store.crypto.CryptoContext;
import org.cumulus4j.store.model.FieldMeta;
import org.cumulus4j.store.model.IndexEntry;
import org.cumulus4j.store.model.IndexEntryFactory;
import org.cumulus4j.store.model.IndexValue;
import org.cumulus4j.store.query.QueryEvaluator;
import org.cumulus4j.store.query.eval.ExpressionHelper;
import org.cumulus4j.store.query.eval.InvokeExpressionEvaluator;
import org.cumulus4j.store.query.eval.PrimaryExpressionResolver;
import org.cumulus4j.store.query.eval.ResultDescriptor;
import org.datanucleus.query.expression.Expression;
import org.datanucleus.query.expression.PrimaryExpression;
import org.datanucleus.store.ExecutionContext;

/**
 * Evaluator for "String.indexOf(str [,from]) {oper} {compareTo}".
 */
public class StringIndexOfEvaluator extends AbstractMethodEvaluator {

	/* (non-Javadoc)
	 * @see org.cumulus4j.store.query.method.AbstractMethodEvaluator#requiresComparisonArgument()
	 */
	@Override
	public boolean requiresComparisonArgument() {
		return true;
	}

	/* (non-Javadoc)
	 * @see org.cumulus4j.store.query.method.MethodEvaluator#evaluate(org.cumulus4j.store.query.QueryEvaluator, org.cumulus4j.store.query.eval.InvokeExpressionEvaluator, org.datanucleus.query.expression.Expression, org.cumulus4j.store.query.eval.ResultDescriptor)
	 */
	@Override
	public Set<Long> evaluate(QueryEvaluator queryEval,
			InvokeExpressionEvaluator invokeExprEval, Expression invokedExpr,
			ResultDescriptor resultDesc) {
		if (invokeExprEval.getExpression().getArguments().size() < 1 || invokeExprEval.getExpression().getArguments().size() > 2)
			throw new IllegalStateException("String.indexOf(...) expects 1 or 2 arguments, but there are " +
					invokeExprEval.getExpression().getArguments().size());

		// Evaluate the invoke argument
		Object[] invokeArgs = ExpressionHelper.getEvaluatedInvokeArguments(queryEval, invokeExprEval.getExpression());

		if (invokedExpr instanceof PrimaryExpression) {
			return new MethodResolver(invokeExprEval, queryEval, (PrimaryExpression) invokedExpr, invokeArgs[0],
					(invokeArgs.length > 1 ? invokeArgs[1] : null), compareToArgument, resultDesc.isNegated()).query();
		}
		else {
			if (!invokeExprEval.getLeft().getResultSymbols().contains(resultDesc.getSymbol()))
				return null;

			return queryEvaluate(invokeExprEval, queryEval, resultDesc.getFieldMeta(), invokeArgs[0],
					(invokeArgs.length > 1 ? invokeArgs[1] : null), compareToArgument, resultDesc.isNegated());
		}
	}

	private Set<Long> queryEvaluate(
			InvokeExpressionEvaluator invokeExprEval,
			QueryEvaluator queryEval,
			FieldMeta fieldMeta,
			Object invokeArg1, // the xxx in 'indexOf(xxx)'
			Object invokeArg2, // the xxx2 in 'indexOf(xxx1, xxx2)'
			Object compareToArgument, // the yyy in 'indexOf(xxx) >= yyy'
			boolean negate
	) {
		CryptoContext cryptoContext = queryEval.getCryptoContext();
		ExecutionContext executionContext = queryEval.getExecutionContext();
		IndexEntryFactory indexEntryFactory = queryEval.getStoreManager().getIndexFactoryRegistry().getIndexEntryFactory(
				executionContext, fieldMeta, true
		);

		Query q = queryEval.getPersistenceManagerForIndex().newQuery(indexEntryFactory.getIndexEntryClass());
		q.setFilter(
				"this.keyStoreRefID == :keyStoreRefID && this.fieldMeta == :fieldMeta && " +
				(invokeArg2 != null ?
						"this.indexKey.indexOf(:invokeArg,:invokeFrom) " : "this.indexKey.indexOf(:invokeArg) ") +
				ExpressionHelper.getOperatorAsJDOQLSymbol(invokeExprEval.getParent().getExpression().getOperator(), negate) +
				" :compareToArgument"
		);
		Map<String, Object> params = new HashMap<String, Object>(3);
		params.put("keyStoreRefID", cryptoContext.getKeyStoreRefID());
		params.put("fieldMeta", fieldMeta);
		params.put("invokeArg", invokeArg1);
		if (invokeArg2 != null)
		{
			params.put("invokeFrom", invokeArg2);
		}
		params.put("compareToArgument", compareToArgument);

		@SuppressWarnings("unchecked")
		Collection<? extends IndexEntry> indexEntries = (Collection<? extends IndexEntry>) q.executeWithMap(params);

		Set<Long> result = new HashSet<Long>();
		for (IndexEntry indexEntry : indexEntries) {
			IndexValue indexValue = queryEval.getEncryptionHandler().decryptIndexEntry(cryptoContext, indexEntry);
			result.addAll(indexValue.getDataEntryIDs());
		}
		q.closeAll();
		return result;
	}

	private class MethodResolver extends PrimaryExpressionResolver
	{
		private InvokeExpressionEvaluator invokeExprEval;
		private Object invokeArg;
		private Object invokeFrom;
		private Object compareToArgument;
		private boolean negate;

		public MethodResolver(
				InvokeExpressionEvaluator invokeExprEval,
				QueryEvaluator queryEvaluator, PrimaryExpression primaryExpression,
				Object invokeArg1, // the xxx1 in 'indexOf(xxx1) >= yyy'
				Object invokeArg2, // the xxx2 in 'indexOf(xxx1, xxx2) >= yyy'
				Object compareToArgument, // the yyy in 'indexOf(xxx) >= yyy'
				boolean negate
		)
		{
			super(queryEvaluator, primaryExpression);
			this.invokeExprEval = invokeExprEval;
			this.invokeArg = invokeArg1;
			this.invokeFrom = invokeArg2;
			this.compareToArgument = compareToArgument;
			this.negate = negate;
		}

		@Override
		protected Set<Long> queryEnd(FieldMeta fieldMeta) {
			return queryEvaluate(invokeExprEval, queryEvaluator, fieldMeta, invokeArg, invokeFrom, compareToArgument, negate);
		}
	}
}