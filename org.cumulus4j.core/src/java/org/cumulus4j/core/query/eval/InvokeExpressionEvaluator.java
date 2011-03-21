package org.cumulus4j.core.query.eval;

import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import javax.jdo.PersistenceManager;

import org.cumulus4j.core.model.ClassMeta;
import org.cumulus4j.core.model.DataEntry;
import org.cumulus4j.core.model.FieldMeta;
import org.cumulus4j.core.model.FieldMetaRole;
import org.cumulus4j.core.model.IndexEntry;
import org.cumulus4j.core.model.IndexEntryFactory;
import org.cumulus4j.core.model.IndexEntryFactoryRegistry;
import org.cumulus4j.core.model.IndexEntryOneToOneRelationHelper;
import org.cumulus4j.core.model.IndexValue;
import org.cumulus4j.core.query.QueryEvaluator;
import org.datanucleus.metadata.AbstractMemberMetaData;
import org.datanucleus.query.QueryUtils;
import org.datanucleus.query.expression.Expression;
import org.datanucleus.query.expression.InvokeExpression;
import org.datanucleus.query.expression.Literal;
import org.datanucleus.query.expression.ParameterExpression;
import org.datanucleus.query.expression.PrimaryExpression;
import org.datanucleus.query.expression.VariableExpression;
import org.datanucleus.query.symbol.Symbol;

/**
 * Evaluator handling method invocations like <code>Collection.contains(...)</code>.
 *
 * @author Marco หงุ่ยตระกูล-Schulze - marco at nightlabs dot de
 */
public class InvokeExpressionEvaluator
extends AbstractExpressionEvaluator<InvokeExpression>
{
	public InvokeExpressionEvaluator(QueryEvaluator queryEvaluator, AbstractExpressionEvaluator<?> parent, InvokeExpression expression)
	{
		super(queryEvaluator, parent, expression);
	}

	@Override
	protected Set<Long> _queryResultDataEntryIDs(Symbol resultSymbol)
	{
		if (this.getLeft() instanceof PrimaryExpressionEvaluator) {
			if (!getLeft().getResultSymbols().contains(resultSymbol))
				return null;

			if ("contains".equals(this.getExpression().getOperation())) {
				PrimaryExpressionEvaluator primaryEval = (PrimaryExpressionEvaluator) this.getLeft();
				PrimaryExpression primaryExpr = primaryEval.getExpression();
				Class<?> parameterType = primaryExpr.getSymbol().getValueType();

				if (this.getExpression().getArguments().size() != 1)
					throw new IllegalStateException("contains(...) expects exactly one argument, but there are " + this.getExpression().getArguments().size());

				Expression invokeArgExpr = this.getExpression().getArguments().get(0);

				if (Collection.class.isAssignableFrom(parameterType)) {
					Object invokeArgument;
					if (invokeArgExpr instanceof Literal)
						invokeArgument = ((Literal)invokeArgExpr).getLiteral();
					else if (invokeArgExpr instanceof ParameterExpression)
						invokeArgument = QueryUtils.getValueForParameterExpression(getQueryEvaluator().getParameterValues(), (ParameterExpression)invokeArgExpr);
					else if (invokeArgExpr instanceof VariableExpression)
						return new CollectionContainsVariableResolver(
								getQueryEvaluator(), primaryExpr, (VariableExpression) invokeArgExpr
						).query();
					else
						throw new UnsupportedOperationException("NYI");

					return new CollectionContainsValueResolver(getQueryEvaluator(), primaryExpr, invokeArgument).query();
				}

				throw new UnsupportedOperationException("NYI");
			}
			throw new UnsupportedOperationException("NYI");
		}

		throw new UnsupportedOperationException("Cannot be evaluated alone without loading *ALL* records of a certain type!");
	}


	private class CollectionContainsVariableResolver extends PrimaryExpressionResolver
	{
		private VariableExpression variableExpr;

		public CollectionContainsVariableResolver(
				QueryEvaluator queryEvaluator, PrimaryExpression primaryExpression,
				VariableExpression variableExpr
		)
		{
			super(queryEvaluator, primaryExpression);
			this.variableExpr = variableExpr;

			if (variableExpr == null)
				throw new IllegalArgumentException("variableExpr == null");

			if (variableExpr.getSymbol() == null)
				throw new IllegalArgumentException("variableExpr.getSymbol() == null");
		}

		@Override
		protected Set<Long> queryEnd(FieldMeta fieldMeta) {
			PersistenceManager pm = getPersistenceManager();
//			AbstractMemberMetaData mmd = fieldMeta.getDataNucleusMemberMetaData(getQueryEvaluator().getExecutionContext());
			FieldMeta subFieldMeta = fieldMeta.getSubFieldMeta(FieldMetaRole.collectionElement);

//			if (mmd.getCollection().elementIsPersistent()) {
				Set<Long> result = new HashSet<Long>();
				AbstractExpressionEvaluator<?> eval = getQueryEvaluator().getExpressionEvaluator();
				Collection<Long> valueDataEntryIDs = eval.queryResultDataEntryIDs(variableExpr.getSymbol());
				for (Long valueDataEntryID : valueDataEntryIDs) {
					IndexEntry indexEntry = IndexEntryOneToOneRelationHelper.getIndexEntry(pm, subFieldMeta, valueDataEntryID);
					if (indexEntry != null) {
						IndexValue indexValue = getQueryEvaluator().getEncryptionHandler().decryptIndexEntry(indexEntry);
						result.addAll(indexValue.getDataEntryIDs());
					}
				}
				return result;
//			}
//			else
//				throw new UnsupportedOperationException("Variable of a simple type is not yet implemented! Variable must be of a persistence-capable class!");
		}
	}


	private class CollectionContainsValueResolver extends PrimaryExpressionResolver
	{
		private Object value;

		public CollectionContainsValueResolver(
				QueryEvaluator queryEvaluator, PrimaryExpression primaryExpression,
				Object value
		)
		{
			super(queryEvaluator, primaryExpression);
			this.value = value;
		}

		@Override
		protected Set<Long> queryEnd(FieldMeta fieldMeta) {
			PersistenceManager pm = getPersistenceManager();
			AbstractMemberMetaData mmd = fieldMeta.getDataNucleusMemberMetaData(getQueryEvaluator().getExecutionContext());
			FieldMeta subFieldMeta = fieldMeta.getSubFieldMeta(FieldMetaRole.collectionElement);

			if (mmd.getCollection().elementIsPersistent()) {
				Long valueDataEntryID = null;
				if (value != null) {
					ClassMeta valueClassMeta = getQueryEvaluator().getStoreManager().getClassMeta(getQueryEvaluator().getExecutionContext(), value.getClass());
					Object valueID = getQueryEvaluator().getExecutionContext().getApiAdapter().getIdForObject(value);
					if (valueID == null)
						throw new IllegalStateException("The ApiAdapter returned null as object-ID for: " + value);

					valueDataEntryID = DataEntry.getDataEntryID(pm, valueClassMeta, valueID.toString());
				}
				IndexEntry indexEntry = IndexEntryOneToOneRelationHelper.getIndexEntry(pm, subFieldMeta, valueDataEntryID);
				if (indexEntry == null)
					return Collections.emptySet();

				IndexValue indexValue = getQueryEvaluator().getEncryptionHandler().decryptIndexEntry(indexEntry);
				return indexValue.getDataEntryIDs();
			}
			else {
				IndexEntryFactory indexEntryFactory = IndexEntryFactoryRegistry.sharedInstance().getIndexEntryFactory(mmd, true);
				IndexEntry indexEntry = indexEntryFactory == null ? null : indexEntryFactory.getIndexEntry(pm, subFieldMeta, value);
				if (indexEntry == null)
					return Collections.emptySet();

				IndexValue indexValue = getQueryEvaluator().getEncryptionHandler().decryptIndexEntry(indexEntry);
				return indexValue.getDataEntryIDs();
			}
		}
	}
}
