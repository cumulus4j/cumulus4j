package org.cumulus4j.core.query.eval;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import javax.jdo.PersistenceManager;
import javax.jdo.Query;

import org.cumulus4j.core.model.ClassMeta;
import org.cumulus4j.core.model.DataEntry;
import org.cumulus4j.core.model.FieldMeta;
import org.cumulus4j.core.model.IndexEntry;
import org.cumulus4j.core.model.IndexEntryFactory;
import org.cumulus4j.core.model.IndexEntryFactoryRegistry;
import org.cumulus4j.core.model.IndexEntryObjectRelationHelper;
import org.cumulus4j.core.model.IndexValue;
import org.cumulus4j.core.query.QueryEvaluator;
import org.datanucleus.metadata.AbstractMemberMetaData;
import org.datanucleus.metadata.Relation;
import org.datanucleus.query.QueryUtils;
import org.datanucleus.query.expression.DyadicExpression;
import org.datanucleus.query.expression.Expression;
import org.datanucleus.query.expression.Expression.Operator;
import org.datanucleus.query.expression.Literal;
import org.datanucleus.query.expression.ParameterExpression;
import org.datanucleus.query.expression.PrimaryExpression;
import org.datanucleus.store.ExecutionContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Evaluator handling the comparisons ==, &lt;, &lt;=, &gt;, &gt;=.
 *
 * @author Marco หงุ่ยตระกูล-Schulze - marco at nightlabs dot de
 */
public class ComparisonExpressionEvaluator
extends AbstractExpressionEvaluator<DyadicExpression>
{
	private static final Logger logger = LoggerFactory.getLogger(ComparisonExpressionEvaluator.class);

	public ComparisonExpressionEvaluator(QueryEvaluator queryEvaluator, AbstractExpressionEvaluator<?> parent, DyadicExpression expression) {
		super(queryEvaluator, parent, expression);
	}

	@Override
	protected Set<Long> _queryResultDataEntryIDs(ResultDescriptor resultDescriptor)
	{
		ExecutionContext executionContext = getQueryEvaluator().getExecutionContext();

		if (getLeft() instanceof InvokeExpressionEvaluator) {
			if (!getLeft().getResultSymbols().contains(resultDescriptor.getSymbol()))
				return null;

			InvokeExpressionEvaluator invokeEval = (InvokeExpressionEvaluator) getLeft();
			if ("indexOf".equals(invokeEval.getExpression().getOperation())) {
				if (invokeEval.getExpression().getArguments().size() != 1)
					throw new IllegalStateException("indexOf(...) expects exactly one argument, but there are " + invokeEval.getExpression().getArguments().size());

				Expression invokeArgExpr = invokeEval.getExpression().getArguments().get(0);

				Object invokeArgument;
				if (invokeArgExpr instanceof Literal)
					invokeArgument = ((Literal)invokeArgExpr).getLiteral();
				else if (invokeArgExpr instanceof ParameterExpression)
					invokeArgument = QueryUtils.getValueForParameterExpression(getQueryEvaluator().getParameterValues(), (ParameterExpression)invokeArgExpr);
				else
					throw new UnsupportedOperationException("NYI");

				Object compareToArgument = getRightCompareToArgument();

				if (invokeEval.getLeft() instanceof PrimaryExpressionEvaluator) {
					PrimaryExpressionEvaluator primaryEval = (PrimaryExpressionEvaluator) invokeEval.getLeft();
					PrimaryExpression primaryExpr = primaryEval.getExpression();
					Class<?> parameterType = getFieldType(primaryExpr);

					if (String.class.isAssignableFrom(parameterType))
						return new StringIndexOfResolver(getQueryEvaluator(), primaryExpr, invokeArgument, compareToArgument, resultDescriptor.isNegated()).query();

					throw new UnsupportedOperationException("NYI");
				}

				if (invokeEval.getLeft() instanceof VariableExpressionEvaluator) {
					if (!invokeEval.getLeft().getResultSymbols().contains(resultDescriptor.getSymbol()))
						return null;
//					VariableExpressionEvaluator varExprEval = (VariableExpressionEvaluator) invokeEval.getLeft();
//					VariableExpression varExpr = varExprEval.getExpression();
//					if (varExpr.getSymbol() == null)
//						throw new IllegalStateException("varExpr.getSymbol() == null");
//
//					if (!varExpr.getSymbol().equals(resultDescriptor.getSymbol())) // searching something else ;-)
//						return null;

					// We query a simple data type (otherwise we would be above in the PrimaryExpressionEvaluator block), hence
					// we do not need to recursively resolve some tuples.
					IndexEntryFactory indexEntryFactory = IndexEntryFactoryRegistry.sharedInstance().getIndexEntryFactory(
							executionContext, resultDescriptor.getFieldMeta(), true
					);
					return queryStringIndexOf(resultDescriptor.getFieldMeta(), indexEntryFactory, invokeArgument, compareToArgument, resultDescriptor.isNegated());
				}
				throw new UnsupportedOperationException("NYI");
			}
			throw new UnsupportedOperationException("NYI");
		}

		if (getLeft() instanceof PrimaryExpressionEvaluator) {
			if (!getLeft().getResultSymbols().contains(resultDescriptor.getSymbol()))
				return null;

			Object compareToArgument = getRightCompareToArgument();

//			if (Expression.OP_EQ == getExpression().getOperator())
//				return new EqualsWithConcreteValueResolver(getQueryEvaluator(), ((PrimaryExpressionEvaluator)getLeft()).getExpression(), compareToArgument).query();
//			else
				return new CompareWithConcreteValueResolver(getQueryEvaluator(), ((PrimaryExpressionEvaluator)getLeft()).getExpression(), compareToArgument, resultDescriptor.isNegated()).query();
		}

		if (getRight() instanceof PrimaryExpressionEvaluator) {
			if (!getRight().getResultSymbols().contains(resultDescriptor.getSymbol()))
				return null;

			Object compareToArgument = getLeftCompareToArgument();

//			if (Expression.OP_EQ == getExpression().getOperator())
//				return new EqualsWithConcreteValueResolver(getQueryEvaluator(), ((PrimaryExpressionEvaluator)getRight()).getExpression(), compareToArgument).query();
//			else
				return new CompareWithConcreteValueResolver(getQueryEvaluator(), ((PrimaryExpressionEvaluator)getRight()).getExpression(), compareToArgument, resultDescriptor.isNegated()).query();
		}

		if (getLeft() instanceof VariableExpressionEvaluator) {
			if (!getLeft().getResultSymbols().contains(resultDescriptor.getSymbol()))
				return null;
//			VariableExpressionEvaluator varExprEval = (VariableExpressionEvaluator) getLeft();
//			VariableExpression varExpr = varExprEval.getExpression();
//			if (varExpr.getSymbol() == null)
//				throw new IllegalStateException("varExpr.getSymbol() == null");
//
//			if (!varExpr.getSymbol().equals(resultDescriptor.getSymbol())) // searching something else ;-)
//				return null;

			if (resultDescriptor.getFieldMeta() != null)
				return queryCompareConcreteValue(resultDescriptor.getFieldMeta(), getRightCompareToArgument(), resultDescriptor.isNegated());
			else {
				// The variable is an FCO and directly compared (otherwise it would be a PrimaryExpression - see above) or the FieldMeta would be specified.
				ClassMeta classMeta = getQueryEvaluator().getStoreManager().getClassMeta(executionContext, resultDescriptor.getResultType());
				return queryEqualsConcreteValue(classMeta, getRightCompareToArgument(), resultDescriptor.isNegated());
			}
		}

		throw new UnsupportedOperationException("NYI");
	}

	private Object getLeftCompareToArgument() {
		Object compareToArgument;
		if (getLeft() instanceof LiteralEvaluator)
			compareToArgument = ((LiteralEvaluator)getRight()).getLiteralValue();
		else if (getLeft() instanceof ParameterExpressionEvaluator)
			compareToArgument = ((ParameterExpressionEvaluator)getRight()).getParameterValue();
		else
			throw new UnsupportedOperationException("NYI");
		return compareToArgument;
	}

	private Object getRightCompareToArgument() {
		Object compareToArgument;
		if (getRight() instanceof LiteralEvaluator)
			compareToArgument = ((LiteralEvaluator)getRight()).getLiteralValue();
		else if (getRight() instanceof ParameterExpressionEvaluator)
			compareToArgument = ((ParameterExpressionEvaluator)getRight()).getParameterValue();
		else
			throw new UnsupportedOperationException("NYI");
		return compareToArgument;
	}

	private Set<Long> queryStringIndexOf(
			FieldMeta fieldMeta,
			IndexEntryFactory indexEntryFactory,
			Object invokeArgument, // the xxx in 'indexOf(xxx)'
			Object compareToArgument, // the yyy in 'indexOf(xxx) >= yyy'
			boolean negate
	) {
		Query q = getPersistenceManager().newQuery(indexEntryFactory.getIndexEntryClass());
		q.setFilter(
				"this.fieldMeta == :fieldMeta && " +
				"this.indexKey.indexOf(:invokeArgument) " + getOperatorAsJDOQLSymbol(negate) + " :compareToArgument"
		);
		Map<String, Object> params = new HashMap<String, Object>(3);
		params.put("fieldMeta", fieldMeta);
		params.put("invokeArgument", invokeArgument);
		params.put("compareToArgument", compareToArgument);

		@SuppressWarnings("unchecked")
		Collection<? extends IndexEntry> indexEntries = (Collection<? extends IndexEntry>) q.executeWithMap(params);

		Set<Long> result = new HashSet<Long>();
		for (IndexEntry indexEntry : indexEntries) {
			IndexValue indexValue = getQueryEvaluator().getEncryptionHandler().decryptIndexEntry(indexEntry);
			result.addAll(indexValue.getDataEntryIDs());
		}
		q.closeAll();
		return result;
	}

	private class StringIndexOfResolver extends PrimaryExpressionResolver
	{
		private Object invokeArgument;
		private Object compareToArgument;
		private boolean negate;

		public StringIndexOfResolver(
				QueryEvaluator queryEvaluator, PrimaryExpression primaryExpression,
				Object invokeArgument, // the xxx in 'indexOf(xxx)'
				Object compareToArgument, // the yyy in 'indexOf(xxx) >= yyy'
				boolean negate
		)
		{
			super(queryEvaluator, primaryExpression);
			this.invokeArgument = invokeArgument;
			this.compareToArgument = compareToArgument;
			this.negate = negate;
		}

		@Override
		protected Set<Long> queryEnd(FieldMeta fieldMeta) {
			IndexEntryFactory indexEntryFactory = IndexEntryFactoryRegistry.sharedInstance().getIndexEntryFactory(
					executionContext, fieldMeta, true
			);
			return queryStringIndexOf(fieldMeta, indexEntryFactory, invokeArgument, compareToArgument, negate);
		}
	}

	private class CompareWithConcreteValueResolver extends PrimaryExpressionResolver
	{
		private Object value;
		private boolean negate;

		public CompareWithConcreteValueResolver(
				QueryEvaluator queryEvaluator, PrimaryExpression primaryExpression,
				Object value, boolean negate
		)
		{
			super(queryEvaluator, primaryExpression);
			this.value = value;
			this.negate = negate;
		}

		@Override
		protected Set<Long> queryEnd(FieldMeta fieldMeta) {
			return queryCompareConcreteValue(fieldMeta, value, negate);
		}
	}

	private Set<Long> queryCompareConcreteValue(FieldMeta fieldMeta, Object value, boolean negate)
	{
		PersistenceManager pm = getPersistenceManager();
		ExecutionContext executionContext = getQueryEvaluator().getExecutionContext();
		AbstractMemberMetaData mmd = fieldMeta.getDataNucleusMemberMetaData(executionContext);
		int relationType = mmd.getRelationType(executionContext.getClassLoaderResolver());

		Object queryParam;
		IndexEntryFactory indexEntryFactory;
		if (Relation.NONE == relationType)
		{
			indexEntryFactory = IndexEntryFactoryRegistry.sharedInstance().getIndexEntryFactory(
					getQueryEvaluator().getExecutionContext(), fieldMeta, true
			);
			queryParam = value;
		}
		else if (Relation.isRelationSingleValued(relationType))
		{
			// Only "==" and "!=" are supported for object relations => check.
			Operator op = getExpression().getOperator();
			if (Expression.OP_EQ != op && Expression.OP_NOTEQ != op)
				throw new UnsupportedOperationException("The operation \"" + getOperatorAsJDOQLSymbol(false) + "\" is not supported for object relations!");

			indexEntryFactory = IndexEntryObjectRelationHelper.getIndexEntryFactory();
			Long valueDataEntryID = null;
			if (value != null) {
				ClassMeta valueClassMeta = getQueryEvaluator().getStoreManager().getClassMeta(executionContext, value.getClass());
				Object valueID = executionContext.getApiAdapter().getIdForObject(value);
				if (valueID == null)
					throw new IllegalStateException("The ApiAdapter returned null as object-ID for: " + value);

				valueDataEntryID = DataEntry.getDataEntryID(pm, valueClassMeta, valueID.toString());
			}
			queryParam = valueDataEntryID;
		}
		else
			throw new UnsupportedOperationException("NYI");

		if (indexEntryFactory == null) {
			logger.warn("queryCompareConcreteValue: Returning empty result, because there is no index for this field: " + fieldMeta);
			return Collections.emptySet();
		}

		Query q = pm.newQuery(indexEntryFactory.getIndexEntryClass());
		q.setFilter(
				"this.fieldMeta == :fieldMeta && " +
				"this.indexKey " + getOperatorAsJDOQLSymbol(negate) + " :value"
		);

		@SuppressWarnings("unchecked")
		Collection<? extends IndexEntry> indexEntries = (Collection<? extends IndexEntry>) q.execute(fieldMeta, queryParam);

		Set<Long> result = new HashSet<Long>();
		for (IndexEntry indexEntry : indexEntries) {
			IndexValue indexValue = getQueryEvaluator().getEncryptionHandler().decryptIndexEntry(indexEntry);
			result.addAll(indexValue.getDataEntryIDs());
		}
		q.closeAll();
		return result;
	}

	private Set<Long> queryEqualsConcreteValue(ClassMeta classMeta, Object value, boolean negate)
	{
		Operator op = getExpression().getOperator();
		if (Expression.OP_EQ != op && Expression.OP_NOTEQ != op)
			throw new UnsupportedOperationException("The operation \"" + getOperatorAsJDOQLSymbol(false) + "\" is not supported for object relations!");

		PersistenceManager pm = getPersistenceManager();
		ExecutionContext executionContext = getQueryEvaluator().getExecutionContext();
		Object valueID = executionContext.getApiAdapter().getIdForObject(value);
		if (valueID == null)
			throw new IllegalStateException("The ApiAdapter returned null as object-ID for: " + value);

		if (Expression.OP_NOTEQ == op || negate) {
			return DataEntry.getDataEntryIDsNegated(pm, classMeta, valueID.toString());
		}
		else {
			Long dataEntryID = DataEntry.getDataEntryID(pm, classMeta, valueID.toString());
			return Collections.singleton(dataEntryID);
		}
	}

	private String getOperatorAsJDOQLSymbol(boolean negate)
	{
		Operator op = getExpression().getOperator();
		if (Expression.OP_EQ == op)
			return negate ? "!=" : "==";
		if (Expression.OP_NOTEQ == op)
			return negate ? "==" : "!=";
		if (Expression.OP_LT == op)
			return negate ? ">=" : "<";
		if (Expression.OP_LTEQ == op)
			return negate ? ">"  : "<=";
		if (Expression.OP_GT == op)
			return negate ? "<=" : ">";
		if (Expression.OP_GTEQ == op)
			return negate ? "<"  : ">=";

		throw new UnsupportedOperationException("NYI");
	}
}
