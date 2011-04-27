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
 * Evaluator for <pre>{String}.equals(arg)</pre>.
 */
public class StringEqualsEvaluator extends AbstractMethodEvaluator
{
	/* (non-Javadoc)
	 * @see org.cumulus4j.core.query.method.MethodEvaluator#evaluate(org.cumulus4j.core.query.QueryEvaluator, org.datanucleus.query.expression.InvokeExpression, org.datanucleus.query.expression.Expression, org.cumulus4j.core.query.eval.ResultDescriptor)
	 */
	@Override
	public Set<Long> evaluate(QueryEvaluator queryEval, InvokeExpressionEvaluator invokeExprEval,
			Expression invokedExpr, ResultDescriptor resultDesc) {
		if (invokeExprEval.getExpression().getArguments().size() != 1)
			throw new IllegalStateException("{String}.equals(...) expects exactly one argument, but there are " +
					invokeExprEval.getExpression().getArguments().size());

		// Evaluate the invoke argument
		Object invokeArgument = ExpressionHelper.getEvaluatedInvokeArgument(queryEval, invokeExprEval.getExpression());

		if (invokedExpr instanceof PrimaryExpression) {
			return new StringEqualsResolver(queryEval, (PrimaryExpression) invokedExpr, invokeArgument, resultDesc.isNegated()).query();
		}
		else {
			if (!invokeExprEval.getLeft().getResultSymbols().contains(resultDesc.getSymbol()))
				return null;
			return queryStringEquals(queryEval, resultDesc.getFieldMeta(), invokeArgument, resultDesc.isNegated());
		}
	}

	private Set<Long> queryStringEquals(
			QueryEvaluator queryEval,
			FieldMeta fieldMeta,
			Object invokeArgument, // the xxx in 'equals(xxx)'
			boolean negate
	) {
		ExecutionContext executionContext = queryEval.getExecutionContext();
		IndexEntryFactory indexEntryFactory = IndexEntryFactoryRegistry.sharedInstance().getIndexEntryFactory(
				executionContext, fieldMeta, true
		);

		Query q = queryEval.getPersistenceManager().newQuery(indexEntryFactory.getIndexEntryClass());
		q.setFilter(
				"this.fieldMeta == :fieldMeta && " +
				(negate ? "!this.indexKey == :invokeArg" : "this.indexKey == :invokeArg")
		);
		Map<String, Object> params = new HashMap<String, Object>(3);
		params.put("fieldMeta", fieldMeta);
		params.put("invokeArg", invokeArgument);

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

	private class StringEqualsResolver extends PrimaryExpressionResolver
	{
		private Object invokeArgument;
		private boolean negate;

		public StringEqualsResolver(
				QueryEvaluator queryEvaluator, PrimaryExpression primaryExpression,
				Object invokeArgument, // the xxx in 'equals(xxx)'
				boolean negate
		)
		{
			super(queryEvaluator, primaryExpression);
			this.invokeArgument = invokeArgument;
			this.negate = negate;
		}

		@Override
		protected Set<Long> queryEnd(FieldMeta fieldMeta) {
			return queryStringEquals(queryEvaluator, fieldMeta, invokeArgument, negate);
		}
	}
}