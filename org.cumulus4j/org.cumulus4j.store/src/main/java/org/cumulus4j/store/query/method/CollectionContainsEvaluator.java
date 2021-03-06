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
import org.cumulus4j.store.model.ClassMeta;
import org.cumulus4j.store.model.FieldMeta;
import org.cumulus4j.store.model.FieldMetaRole;
import org.cumulus4j.store.model.IndexEntry;
import org.cumulus4j.store.model.IndexEntryFactory;
import org.cumulus4j.store.model.IndexValue;
import org.cumulus4j.store.query.QueryEvaluator;
import org.cumulus4j.store.query.eval.ExpressionHelper;
import org.cumulus4j.store.query.eval.InvokeExpressionEvaluator;
import org.cumulus4j.store.query.eval.PrimaryExpressionResolver;
import org.cumulus4j.store.query.eval.ResultDescriptor;
import org.datanucleus.ExecutionContext;
import org.datanucleus.query.QueryUtils;
import org.datanucleus.query.expression.Expression;
import org.datanucleus.query.expression.Literal;
import org.datanucleus.query.expression.ParameterExpression;
import org.datanucleus.query.expression.PrimaryExpression;
import org.datanucleus.query.expression.VariableExpression;

/**
 * Evaluator for "Collection.contains(element)".
 */
public class CollectionContainsEvaluator extends AbstractMethodEvaluator
{
	/* (non-Javadoc)
	 * @see org.cumulus4j.store.query.method.MethodEvaluator#evaluate(org.cumulus4j.store.query.QueryEvaluator, org.datanucleus.query.expression.InvokeExpression, org.datanucleus.query.expression.Expression, org.cumulus4j.store.query.eval.ResultDescriptor)
	 */
	@Override
	public Set<Long> evaluate(QueryEvaluator queryEval, InvokeExpressionEvaluator invokeExprEval,
			Expression invokedExpr, ResultDescriptor resultDesc) {

		if (invokeExprEval.getExpression().getArguments().size() != 1)
			throw new IllegalStateException("contains(...) expects exactly one argument, but there are " +
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
						queryEval, (PrimaryExpression) invokedExpr, FieldMetaRole.collectionElement, (VariableExpression) invokeArgExpr,
						resultDesc.isNegated()
				).query();
			else
				throw new UnsupportedOperationException("NYI");

			return new ExpressionHelper.ContainsConstantResolver(
					queryEval, (PrimaryExpression) invokedExpr, FieldMetaRole.collectionElement, invokeArgument,
					resultDesc.isNegated()
			).query();
		}
		else if (invokedExpr instanceof ParameterExpression) {
			Expression invokeArgExpr = invokeExprEval.getExpression().getArguments().get(0);
			Object paramValue = QueryUtils.getValueForParameterExpression(queryEval.getParameterValues(), (ParameterExpression)invokedExpr);

			if (invokeArgExpr instanceof PrimaryExpression) {
				return new ParameterContainsPrimaryEvaluator(queryEval, (PrimaryExpression) invokeArgExpr, (Collection)paramValue, resultDesc.isNegated()).query();
			}
			else {
				throw new UnsupportedOperationException("NYI invocation of Collection.contains on a " + invokedExpr.getClass().getName());
			}
		}
		else {
			throw new UnsupportedOperationException("NYI invocation of Collection.contains on a " + invokedExpr.getClass().getName());
		}
	}

	private class ParameterContainsPrimaryEvaluator extends PrimaryExpressionResolver
	{
		private Collection invokeCollection;
		private boolean negate;

		public ParameterContainsPrimaryEvaluator(
				QueryEvaluator queryEvaluator, PrimaryExpression primaryExpression,
				Collection invokeCollection,
				boolean negate
		)
		{
			super(queryEvaluator, primaryExpression);
			this.invokeCollection = invokeCollection;
			this.negate = negate;
		}

		@Override
		protected Set<Long> queryEnd(FieldMeta fieldMeta, ClassMeta classMeta) {
			CryptoContext cryptoContext = queryEvaluator.getCryptoContext();
			ExecutionContext executionContext = queryEvaluator.getExecutionContext();
			IndexEntryFactory indexEntryFactory = queryEvaluator.getStoreManager().getIndexFactoryRegistry().getIndexEntryFactory(
					executionContext, fieldMeta, true
			);

			Query q = queryEvaluator.getPersistenceManagerForIndex().newQuery(indexEntryFactory.getIndexEntryClass());
			StringBuilder str = new StringBuilder();
			str.append("this.keyStoreRefID == :keyStoreRefID && this.fieldMeta_fieldID == :fieldMeta_fieldID");
			if (!invokeCollection.isEmpty()) {
				if (negate) {
					str.append(" && !:paramColl.contains(this.indexKey)");
				}
				else {
					str.append(" && :paramColl.contains(this.indexKey)");
				}
			}

			q.setFilter(str.toString());
			Map<String, Object> params = new HashMap<String, Object>(2);
			params.put("keyStoreRefID", cryptoContext.getKeyStoreRefID());
			params.put("fieldMeta_fieldID", fieldMeta.getFieldID());
			params.put("paramColl", invokeCollection);

			@SuppressWarnings("unchecked")
			Collection<? extends IndexEntry> indexEntries = (Collection<? extends IndexEntry>) q.executeWithMap(params);

			Set<Long> result = new HashSet<Long>();
			for (IndexEntry indexEntry : indexEntries) {
				IndexValue indexValue = queryEvaluator.getEncryptionHandler().decryptIndexEntry(cryptoContext, indexEntry);
				result.addAll(indexValue.getDataEntryIDs());
			}
			q.closeAll();
			return result;
		}
	}
}
