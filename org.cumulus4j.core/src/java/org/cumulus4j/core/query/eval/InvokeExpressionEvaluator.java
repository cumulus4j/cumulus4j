package org.cumulus4j.core.query.eval;

import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import javax.jdo.PersistenceManager;

import org.cumulus4j.core.model.ClassMeta;
import org.cumulus4j.core.model.DataEntry;
import org.cumulus4j.core.model.FieldMeta;
import org.cumulus4j.core.model.FieldMetaRole;
import org.cumulus4j.core.model.IndexEntry;
import org.cumulus4j.core.model.IndexEntryFactory;
import org.cumulus4j.core.model.IndexEntryFactoryRegistry;
import org.cumulus4j.core.model.IndexEntryObjectRelationHelper;
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
	protected Set<Long> _queryResultDataEntryIDs(ResultDescriptor resultDescriptor)
	{
		if (this.getLeft() instanceof PrimaryExpressionEvaluator) {
			if (!getLeft().getResultSymbols().contains(resultDescriptor.getSymbol()))
				return null;

			if ("contains".equals(this.getExpression().getOperation())) {
				PrimaryExpressionEvaluator primaryEval = (PrimaryExpressionEvaluator) this.getLeft();
				PrimaryExpression primaryExpr = primaryEval.getExpression();
				Class<?> parameterType = getFieldType(primaryExpr);

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
						return new ContainsVariableResolver(
								getQueryEvaluator(), primaryExpr, FieldMetaRole.collectionElement, (VariableExpression) invokeArgExpr
						).query();
					else
						throw new UnsupportedOperationException("NYI");

					return new ContainsConstantResolver(getQueryEvaluator(), primaryExpr, FieldMetaRole.collectionElement, invokeArgument).query();
				}
				throw new UnsupportedOperationException("The method 'contains' is not supported for the data type " + parameterType.getName() + '!');
			}

			if ("containsKey".equals(this.getExpression().getOperation())) {
				PrimaryExpressionEvaluator primaryEval = (PrimaryExpressionEvaluator) this.getLeft();
				PrimaryExpression primaryExpr = primaryEval.getExpression();
				Class<?> parameterType = getFieldType(primaryExpr);

				if (this.getExpression().getArguments().size() != 1)
					throw new IllegalStateException("containsKey(...) expects exactly one argument, but there are " + this.getExpression().getArguments().size());

				Expression invokeArgExpr = this.getExpression().getArguments().get(0);

				if (Map.class.isAssignableFrom(parameterType)) {
					Object invokeArgument;
					if (invokeArgExpr instanceof Literal)
						invokeArgument = ((Literal)invokeArgExpr).getLiteral();
					else if (invokeArgExpr instanceof ParameterExpression)
						invokeArgument = QueryUtils.getValueForParameterExpression(getQueryEvaluator().getParameterValues(), (ParameterExpression)invokeArgExpr);
					else if (invokeArgExpr instanceof VariableExpression)
						return new ContainsVariableResolver(
								getQueryEvaluator(), primaryExpr, FieldMetaRole.mapKey, (VariableExpression) invokeArgExpr
						).query();
					else
						throw new UnsupportedOperationException("NYI");

					return new ContainsConstantResolver(getQueryEvaluator(), primaryExpr, FieldMetaRole.mapKey, invokeArgument).query();
				}
				throw new UnsupportedOperationException("The method 'containsKey' is not supported for the data type " + parameterType.getName() + '!');
			}

			if ("containsValue".equals(this.getExpression().getOperation())) {
				PrimaryExpressionEvaluator primaryEval = (PrimaryExpressionEvaluator) this.getLeft();
				PrimaryExpression primaryExpr = primaryEval.getExpression();
				Class<?> parameterType = getFieldType(primaryExpr);

				if (this.getExpression().getArguments().size() != 1)
					throw new IllegalStateException("containsValue(...) expects exactly one argument, but there are " + this.getExpression().getArguments().size());

				Expression invokeArgExpr = this.getExpression().getArguments().get(0);

				if (Map.class.isAssignableFrom(parameterType)) {
					Object invokeArgument;
					if (invokeArgExpr instanceof Literal)
						invokeArgument = ((Literal)invokeArgExpr).getLiteral();
					else if (invokeArgExpr instanceof ParameterExpression)
						invokeArgument = QueryUtils.getValueForParameterExpression(getQueryEvaluator().getParameterValues(), (ParameterExpression)invokeArgExpr);
					else if (invokeArgExpr instanceof VariableExpression)
						return new ContainsVariableResolver(
								getQueryEvaluator(), primaryExpr, FieldMetaRole.mapValue, (VariableExpression) invokeArgExpr
						).query();
					else
						throw new UnsupportedOperationException("NYI");

					return new ContainsConstantResolver(getQueryEvaluator(), primaryExpr, FieldMetaRole.mapValue, invokeArgument).query();
				}
				throw new UnsupportedOperationException("The method 'containsValue' is not supported for the data type " + parameterType.getName() + '!');
			}

			throw new UnsupportedOperationException("NYI");
		}

		throw new UnsupportedOperationException("NYI");
	}

	private abstract class AbstractContainsResolver extends PrimaryExpressionResolver
	{
		protected FieldMetaRole role;

		public AbstractContainsResolver(
				QueryEvaluator queryEvaluator, PrimaryExpression primaryExpression,
				FieldMetaRole role
		)
		{
			super(queryEvaluator, primaryExpression);
			this.role = role;

			if (role != FieldMetaRole.collectionElement && role != FieldMetaRole.mapKey && role != FieldMetaRole.mapValue)
				throw new IllegalArgumentException("role == " + role);
		}

		@Override
		protected final Set<Long> queryEnd(FieldMeta fieldMeta) {
			PersistenceManager pm = getPersistenceManager();
			AbstractMemberMetaData mmd = fieldMeta.getDataNucleusMemberMetaData(getQueryEvaluator().getExecutionContext());
			FieldMeta subFieldMeta = fieldMeta.getSubFieldMeta(role);

			boolean argumentIsPersistent;
			switch (role) {
				case collectionElement:
					argumentIsPersistent = mmd.getCollection().elementIsPersistent();
					break;
				case mapKey:
					argumentIsPersistent = mmd.getMap().keyIsPersistent();
					break;
				case mapValue:
					argumentIsPersistent = mmd.getMap().valueIsPersistent();
					break;
				default:
					throw new IllegalStateException("Unknown role: " + role);
			}

			return _queryEnd(pm, fieldMeta, mmd, subFieldMeta, argumentIsPersistent);
		}

		protected abstract Set<Long> _queryEnd(
				PersistenceManager pm, FieldMeta fieldMeta, AbstractMemberMetaData mmd, FieldMeta subFieldMeta, boolean argumentIsPersistent
		);

	}


	/**
	 * Resolve {@link Collection#contains(Object)} with the argument being a query variable.
	 *
	 * @author Marco หงุ่ยตระกูล-Schulze - marco at nightlabs dot de
	 */
	private class ContainsVariableResolver extends AbstractContainsResolver
	{
		private VariableExpression variableExpr;

		public ContainsVariableResolver(
				QueryEvaluator queryEvaluator, PrimaryExpression primaryExpression,
				FieldMetaRole role, VariableExpression variableExpr
		)
		{
			super(queryEvaluator, primaryExpression, role);
			this.variableExpr = variableExpr;

			if (variableExpr == null)
				throw new IllegalArgumentException("variableExpr == null");

			if (variableExpr.getSymbol() == null)
				throw new IllegalArgumentException("variableExpr.getSymbol() == null");
		}

		@Override
		public Set<Long> _queryEnd(
				PersistenceManager pm, FieldMeta fieldMeta,
				AbstractMemberMetaData mmd, FieldMeta subFieldMeta,
				boolean argumentIsPersistent
		)
		{
			if (argumentIsPersistent) {
				Set<Long> result = new HashSet<Long>();
				AbstractExpressionEvaluator<?> eval = getQueryEvaluator().getExpressionEvaluator();
				Collection<Long> valueDataEntryIDs = eval.queryResultDataEntryIDs(new ResultDescriptor(variableExpr.getSymbol()));
				for (Long valueDataEntryID : valueDataEntryIDs) {
					IndexEntry indexEntry = IndexEntryObjectRelationHelper.getIndexEntry(pm, subFieldMeta, valueDataEntryID);
					if (indexEntry != null) {
						IndexValue indexValue = getQueryEvaluator().getEncryptionHandler().decryptIndexEntry(indexEntry);
						result.addAll(indexValue.getDataEntryIDs());
					}
				}
				return result;
			}
			else
				throw new UnsupportedOperationException("Variable of a simple type is not yet implemented! Variable must be of a persistence-capable class!");
		}
	}

	/**
	 * Resolve {@link Collection#contains(Object)} with the argument being a concrete value (a 'constant').
	 *
	 * @author Marco หงุ่ยตระกูล-Schulze - marco at nightlabs dot de
	 */
	private class ContainsConstantResolver extends AbstractContainsResolver
	{
		private Object constant;

		public ContainsConstantResolver(
				QueryEvaluator queryEvaluator, PrimaryExpression primaryExpression,
				FieldMetaRole role, Object constant
		)
		{
			super(queryEvaluator, primaryExpression, role);
			this.constant = constant;
		}

		@Override
		public Set<Long> _queryEnd(
				PersistenceManager pm, FieldMeta fieldMeta,
				AbstractMemberMetaData mmd, FieldMeta subFieldMeta,
				boolean argumentIsPersistent
		)
		{
			if (argumentIsPersistent) {
				Long constantDataEntryID = null;
				if (constant != null) {
					ClassMeta constantClassMeta = getQueryEvaluator().getStoreManager().getClassMeta(getQueryEvaluator().getExecutionContext(), constant.getClass());
					Object constantID = getQueryEvaluator().getExecutionContext().getApiAdapter().getIdForObject(constant);
					if (constantID == null)
						throw new IllegalStateException("The ApiAdapter returned null as object-ID for: " + constant);

					constantDataEntryID = DataEntry.getDataEntryID(pm, constantClassMeta, constantID.toString());
				}
				IndexEntry indexEntry = IndexEntryObjectRelationHelper.getIndexEntry(pm, subFieldMeta, constantDataEntryID);
				if (indexEntry == null)
					return Collections.emptySet();

				IndexValue indexValue = getQueryEvaluator().getEncryptionHandler().decryptIndexEntry(indexEntry);
				return indexValue.getDataEntryIDs();
			}
			else {
				IndexEntryFactory indexEntryFactory = IndexEntryFactoryRegistry.sharedInstance().getIndexEntryFactory(getQueryEvaluator().getExecutionContext(), subFieldMeta, true);
				IndexEntry indexEntry = indexEntryFactory == null ? null : indexEntryFactory.getIndexEntry(pm, subFieldMeta, constant);
				if (indexEntry == null)
					return Collections.emptySet();

				IndexValue indexValue = getQueryEvaluator().getEncryptionHandler().decryptIndexEntry(indexEntry);
				return indexValue.getDataEntryIDs();
			}
		}
	}
}
