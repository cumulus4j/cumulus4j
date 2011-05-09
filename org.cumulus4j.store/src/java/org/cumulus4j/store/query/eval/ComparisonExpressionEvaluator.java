package org.cumulus4j.store.query.eval;

import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import javax.jdo.PersistenceManager;
import javax.jdo.Query;

import org.cumulus4j.store.model.ClassMeta;
import org.cumulus4j.store.model.DataEntry;
import org.cumulus4j.store.model.FieldMeta;
import org.cumulus4j.store.model.IndexEntry;
import org.cumulus4j.store.model.IndexEntryFactory;
import org.cumulus4j.store.model.IndexEntryFactoryRegistry;
import org.cumulus4j.store.model.IndexEntryObjectRelationHelper;
import org.cumulus4j.store.model.IndexValue;
import org.cumulus4j.store.query.QueryEvaluator;
import org.datanucleus.metadata.AbstractMemberMetaData;
import org.datanucleus.metadata.Relation;
import org.datanucleus.query.expression.DyadicExpression;
import org.datanucleus.query.expression.Expression;
import org.datanucleus.query.expression.Expression.Operator;
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

			return getLeft().queryResultDataEntryIDs(resultDescriptor);
		}

		if (getLeft() instanceof PrimaryExpressionEvaluator) {
			if (!getLeft().getResultSymbols().contains(resultDescriptor.getSymbol()))
				return null;

			Object compareToArgument = getRightCompareToArgument();
			return new CompareWithConcreteValueResolver(getQueryEvaluator(), ((PrimaryExpressionEvaluator)getLeft()).getExpression(), compareToArgument, resultDescriptor.isNegated()).query();
		}

		if (getRight() instanceof PrimaryExpressionEvaluator) {
			if (!getRight().getResultSymbols().contains(resultDescriptor.getSymbol()))
				return null;

			Object compareToArgument = getLeftCompareToArgument();
			return new CompareWithConcreteValueResolver(getQueryEvaluator(), ((PrimaryExpressionEvaluator)getRight()).getExpression(), compareToArgument, resultDescriptor.isNegated()).query();
		}

		if (getLeft() instanceof VariableExpressionEvaluator) {
			if (!getLeft().getResultSymbols().contains(resultDescriptor.getSymbol()))
				return null;

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

	protected Object getLeftCompareToArgument() {
		Object compareToArgument;
		if (getLeft() instanceof LiteralEvaluator)
			compareToArgument = ((LiteralEvaluator)getLeft()).getLiteralValue();
		else if (getLeft() instanceof ParameterExpressionEvaluator)
			compareToArgument = ((ParameterExpressionEvaluator)getLeft()).getParameterValue();
		else
			throw new UnsupportedOperationException("NYI");
		return compareToArgument;
	}

	protected Object getRightCompareToArgument() {
		Object compareToArgument;
		if (getRight() instanceof LiteralEvaluator)
			compareToArgument = ((LiteralEvaluator)getRight()).getLiteralValue();
		else if (getRight() instanceof ParameterExpressionEvaluator)
			compareToArgument = ((ParameterExpressionEvaluator)getRight()).getParameterValue();
		else
			throw new UnsupportedOperationException("NYI");
		return compareToArgument;
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
			IndexValue indexValue = getQueryEvaluator().getEncryptionHandler().decryptIndexEntry(executionContext, indexEntry);
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
		return ExpressionHelper.getOperatorAsJDOQLSymbol(op, negate);
	}
}