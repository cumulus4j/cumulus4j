package org.cumulus4j.store.query.method;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import javax.jdo.Query;

import org.cumulus4j.store.model.FieldMeta;
import org.cumulus4j.store.model.FieldMetaRole;
import org.cumulus4j.store.model.IndexEntry;
import org.cumulus4j.store.model.IndexEntryFactory;
import org.cumulus4j.store.model.IndexEntryFactoryRegistry;
import org.cumulus4j.store.model.IndexValue;
import org.cumulus4j.store.query.QueryEvaluator;
import org.cumulus4j.store.query.eval.ExpressionHelper;
import org.cumulus4j.store.query.eval.InvokeExpressionEvaluator;
import org.cumulus4j.store.query.eval.PrimaryExpressionResolver;
import org.cumulus4j.store.query.eval.ResultDescriptor;
import org.datanucleus.query.QueryUtils;
import org.datanucleus.query.expression.Expression;
import org.datanucleus.query.expression.Literal;
import org.datanucleus.query.expression.ParameterExpression;
import org.datanucleus.query.expression.PrimaryExpression;
import org.datanucleus.query.expression.VariableExpression;
import org.datanucleus.store.ExecutionContext;

/**
 * Evaluator for <pre>Collection.contains(element)</pre>
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
		protected Set<Long> queryEnd(FieldMeta fieldMeta) {
			ExecutionContext executionContext = queryEvaluator.getExecutionContext();
			IndexEntryFactory indexEntryFactory = IndexEntryFactoryRegistry.sharedInstance().getIndexEntryFactory(
					executionContext, fieldMeta, true
			);

			Query q = queryEvaluator.getPersistenceManager().newQuery(indexEntryFactory.getIndexEntryClass());
			StringBuilder str = new StringBuilder();
			str.append("this.fieldMeta == :fieldMeta");
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
			params.put("fieldMeta", fieldMeta);
			params.put("paramColl", invokeCollection);

			@SuppressWarnings("unchecked")
			Collection<? extends IndexEntry> indexEntries = (Collection<? extends IndexEntry>) q.executeWithMap(params);

			Set<Long> result = new HashSet<Long>();
			for (IndexEntry indexEntry : indexEntries) {
				IndexValue indexValue = queryEvaluator.getEncryptionHandler().decryptIndexEntry(executionContext, indexEntry);
				result.addAll(indexValue.getDataEntryIDs());
			}
			q.closeAll();
			return result;
		}
	}
}
