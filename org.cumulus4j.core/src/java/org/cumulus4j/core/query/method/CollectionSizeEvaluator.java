package org.cumulus4j.core.query.method;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import javax.jdo.Query;

import org.cumulus4j.core.model.FieldMeta;
import org.cumulus4j.core.model.IndexEntry;
import org.cumulus4j.core.model.IndexEntryFactory;
import org.cumulus4j.core.model.IndexEntryFactoryRegistry;
import org.cumulus4j.core.model.IndexValue;
import org.cumulus4j.core.query.QueryEvaluator;
import org.cumulus4j.core.query.eval.ExpressionHelper;
import org.cumulus4j.core.query.eval.InvokeExpressionEvaluator;
import org.cumulus4j.core.query.eval.PrimaryExpressionResolver;
import org.cumulus4j.core.query.eval.ResultDescriptor;
import org.datanucleus.query.expression.Expression;
import org.datanucleus.query.expression.PrimaryExpression;
import org.datanucleus.store.ExecutionContext;

/**
 * Evaluator for <pre>Collection.size() {oper} {comparisonArg}</pre>
 */
public class CollectionSizeEvaluator extends AbstractMethodEvaluator {

	/* (non-Javadoc)
	 * @see org.cumulus4j.core.query.method.MethodEvaluator#evaluate(org.cumulus4j.core.query.QueryEvaluator, org.cumulus4j.core.query.eval.InvokeExpressionEvaluator, org.datanucleus.query.expression.Expression, org.cumulus4j.core.query.eval.ResultDescriptor)
	 */
	@Override
	public Set<Long> evaluate(QueryEvaluator queryEval,
			InvokeExpressionEvaluator invokeExprEval, Expression invokedExpr,
			ResultDescriptor resultDesc) {
		if (invokeExprEval.getExpression().getArguments().size() != 0)
			throw new IllegalStateException("size(...) expects no argument, but there are " + 
					invokeExprEval.getExpression().getArguments().size());

		if (invokedExpr instanceof PrimaryExpression) {
			return new CollectionSizeResolver(invokeExprEval, queryEval, (PrimaryExpression) invokedExpr, compareToArgument, resultDesc.isNegated()).query();
		}
		else {
			if (!invokeExprEval.getLeft().getResultSymbols().contains(resultDesc.getSymbol()))
				return null;
			return queryCollectionSize(invokeExprEval, queryEval, resultDesc.getFieldMeta(), compareToArgument, resultDesc.isNegated());
		}
	}

	private Set<Long> queryCollectionSize(
			InvokeExpressionEvaluator invokeExprEval,
			QueryEvaluator queryEval,
			FieldMeta fieldMeta,
			Object compareToArgument, // the yyy in 'indexOf(xxx) >= yyy'
			boolean negate
	) {
		ExecutionContext executionContext = queryEval.getExecutionContext();
		IndexEntryFactory indexEntryFactory = IndexEntryFactoryRegistry.sharedInstance().getIndexEntryFactoryForContainerSize();

		Query q = queryEval.getPersistenceManager().newQuery(indexEntryFactory.getIndexEntryClass());
		q.setFilter(
				"this.fieldMeta == :fieldMeta && this.indexKey " +
				ExpressionHelper.getOperatorAsJDOQLSymbol(invokeExprEval.getParent().getExpression().getOperator(), negate) + 
				" :compareToArgument"
		);
		Map<String, Object> params = new HashMap<String, Object>(3);
		params.put("fieldMeta", fieldMeta);
		params.put("compareToArgument", compareToArgument);

		@SuppressWarnings("unchecked")
		Collection<? extends IndexEntry> indexEntries = (Collection<? extends IndexEntry>) q.executeWithMap(params);

		Set<Long> result = new HashSet<Long>();
		for (IndexEntry indexEntry : indexEntries) {
			IndexValue indexValue = queryEval.getEncryptionHandler().decryptIndexEntry(executionContext, indexEntry);
			result.addAll(indexValue.getDataEntryIDs());
		}
		q.closeAll();
		return result;
	}

	private class CollectionSizeResolver extends PrimaryExpressionResolver
	{
		private InvokeExpressionEvaluator invokeExprEval;
		private Object compareToArgument;
		private boolean negate;

		public CollectionSizeResolver(
				InvokeExpressionEvaluator invokeExprEval,
				QueryEvaluator queryEvaluator, PrimaryExpression primaryExpression,
				Object compareToArgument, // the yyy in 'size() >= yyy'
				boolean negate
		)
		{
			super(queryEvaluator, primaryExpression);
			this.invokeExprEval = invokeExprEval;
			this.compareToArgument = compareToArgument;
			this.negate = negate;
		}

		@Override
		protected Set<Long> queryEnd(FieldMeta fieldMeta) {
			return queryCollectionSize(invokeExprEval, queryEvaluator, fieldMeta, compareToArgument, negate);
		}
	}
}