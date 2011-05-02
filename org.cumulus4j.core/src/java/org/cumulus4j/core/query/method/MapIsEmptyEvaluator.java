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
import org.cumulus4j.core.query.eval.InvokeExpressionEvaluator;
import org.cumulus4j.core.query.eval.PrimaryExpressionResolver;
import org.cumulus4j.core.query.eval.ResultDescriptor;
import org.datanucleus.query.expression.Expression;
import org.datanucleus.query.expression.PrimaryExpression;
import org.datanucleus.store.ExecutionContext;

/**
 * Evaluator for <pre>Map.isEmpty()</pre>
 */
public class MapIsEmptyEvaluator extends AbstractMethodEvaluator {

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
			return new MapIsEmptyResolver(queryEval, (PrimaryExpression) invokedExpr, resultDesc.isNegated()).query();
		}
		else {
			if (!invokeExprEval.getLeft().getResultSymbols().contains(resultDesc.getSymbol()))
				return null;
			return queryMapIsEmpty(queryEval, resultDesc.getFieldMeta(), resultDesc.isNegated());
		}
	}

	private Set<Long> queryMapIsEmpty(
			QueryEvaluator queryEval,
			FieldMeta fieldMeta,
			boolean negate
	) {
		ExecutionContext executionContext = queryEval.getExecutionContext();
		IndexEntryFactory indexEntryFactory = IndexEntryFactoryRegistry.sharedInstance().getIndexEntryFactoryForContainerSize();

		Query q = queryEval.getPersistenceManager().newQuery(indexEntryFactory.getIndexEntryClass());
		q.setFilter(
				"this.fieldMeta == :fieldMeta && " +
				(negate ? "this.indexKey != 0" : "this.indexKey == 0")
		);
		Map<String, Object> params = new HashMap<String, Object>(3);
		params.put("fieldMeta", fieldMeta);

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

	private class MapIsEmptyResolver extends PrimaryExpressionResolver
	{
		private boolean negate;

		public MapIsEmptyResolver(
				QueryEvaluator queryEvaluator, PrimaryExpression primaryExpression,
				boolean negate
		)
		{
			super(queryEvaluator, primaryExpression);
			this.negate = negate;
		}

		@Override
		protected Set<Long> queryEnd(FieldMeta fieldMeta) {
			return queryMapIsEmpty(queryEvaluator, fieldMeta, negate);
		}
	}
}