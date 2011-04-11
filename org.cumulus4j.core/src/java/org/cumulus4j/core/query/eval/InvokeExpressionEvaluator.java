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
import org.cumulus4j.core.model.ObjectContainer;
import org.cumulus4j.core.query.QueryEvaluator;
import org.datanucleus.metadata.AbstractMemberMetaData;
import org.datanucleus.query.QueryUtils;
import org.datanucleus.query.expression.Expression;
import org.datanucleus.query.expression.InvokeExpression;
import org.datanucleus.query.expression.Literal;
import org.datanucleus.query.expression.ParameterExpression;
import org.datanucleus.query.expression.PrimaryExpression;
import org.datanucleus.query.expression.VariableExpression;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Evaluator handling method invocations like <code>Collection.contains(...)</code>.
 *
 * @author Marco หงุ่ยตระกูล-Schulze - marco at nightlabs dot de
 */
public class InvokeExpressionEvaluator
extends AbstractExpressionEvaluator<InvokeExpression>
{
	private static Logger logger = LoggerFactory.getLogger(InvokeExpressionEvaluator.class);

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

			// Evaluate the left-hand expression on which we perform the method invocation
            PrimaryExpressionEvaluator primaryEval = (PrimaryExpressionEvaluator) this.getLeft();
            PrimaryExpression primaryExpr = primaryEval.getExpression();
            Class<?> parameterType = getFieldType(primaryExpr);

			if (Collection.class.isAssignableFrom(parameterType) && 
			    "contains".equals(this.getExpression().getOperation())) {
				if (this.getExpression().getArguments().size() != 1)
					throw new IllegalStateException("contains(...) expects exactly one argument, but there are " + this.getExpression().getArguments().size());

				// Evaluate the invoke argument
				Expression invokeArgExpr = this.getExpression().getArguments().get(0);
				Object invokeArgument;
				if (invokeArgExpr instanceof Literal)
				    invokeArgument = ((Literal)invokeArgExpr).getLiteral();
				else if (invokeArgExpr instanceof ParameterExpression)
				    invokeArgument = QueryUtils.getValueForParameterExpression(getQueryEvaluator().getParameterValues(), (ParameterExpression)invokeArgExpr);
				else if (invokeArgExpr instanceof VariableExpression)
				    return new ContainsVariableResolver(
				        getQueryEvaluator(), primaryExpr, FieldMetaRole.collectionElement, (VariableExpression) invokeArgExpr,
				        resultDescriptor.isNegated()
				    ).query();
				else
				    throw new UnsupportedOperationException("NYI");

				return new ContainsConstantResolver(
				    getQueryEvaluator(), primaryExpr, FieldMetaRole.collectionElement, invokeArgument,
				    resultDescriptor.isNegated()
				).query();
			}
			else if (Map.class.isAssignableFrom(parameterType) && 
			    "containsKey".equals(this.getExpression().getOperation())) {
				if (this.getExpression().getArguments().size() != 1)
					throw new IllegalStateException("containsKey(...) expects exactly one argument, but there are " + this.getExpression().getArguments().size());

                // Evaluate the invoke argument
				Expression invokeArgExpr = this.getExpression().getArguments().get(0);
				Object invokeArgument;
				if (invokeArgExpr instanceof Literal)
				    invokeArgument = ((Literal)invokeArgExpr).getLiteral();
				else if (invokeArgExpr instanceof ParameterExpression)
				    invokeArgument = QueryUtils.getValueForParameterExpression(getQueryEvaluator().getParameterValues(), (ParameterExpression)invokeArgExpr);
				else if (invokeArgExpr instanceof VariableExpression)
				    return new ContainsVariableResolver(
				        getQueryEvaluator(), primaryExpr, FieldMetaRole.mapKey, (VariableExpression) invokeArgExpr,
				        resultDescriptor.isNegated()
				    ).query();
				else
				    throw new UnsupportedOperationException("NYI");

				return new ContainsConstantResolver(
				    getQueryEvaluator(), primaryExpr, FieldMetaRole.mapKey, invokeArgument,
				    resultDescriptor.isNegated()
				).query();
			}
			else if (Map.class.isAssignableFrom(parameterType) && 
			    "containsValue".equals(this.getExpression().getOperation())) {
				if (this.getExpression().getArguments().size() != 1)
					throw new IllegalStateException("containsValue(...) expects exactly one argument, but there are " + this.getExpression().getArguments().size());

                // Evaluate the invoke argument
				Expression invokeArgExpr = this.getExpression().getArguments().get(0);
				Object invokeArgument;
				if (invokeArgExpr instanceof Literal)
				    invokeArgument = ((Literal)invokeArgExpr).getLiteral();
				else if (invokeArgExpr instanceof ParameterExpression)
				    invokeArgument = QueryUtils.getValueForParameterExpression(getQueryEvaluator().getParameterValues(), (ParameterExpression)invokeArgExpr);
				else if (invokeArgExpr instanceof VariableExpression)
				    return new ContainsVariableResolver(
				        getQueryEvaluator(), primaryExpr, FieldMetaRole.mapValue, (VariableExpression) invokeArgExpr,
				        resultDescriptor.isNegated()
				    ).query();
				else
				    throw new UnsupportedOperationException("NYI");

				return new ContainsConstantResolver(
				    getQueryEvaluator(), primaryExpr, FieldMetaRole.mapValue, invokeArgument,
				    resultDescriptor.isNegated()
				).query();
			}

			throw new UnsupportedOperationException("Not Yet Implemented : "+this.getExpression().getOperation() + 
			    " on " + parameterType);
		}

		throw new UnsupportedOperationException("NYI");
	}

	private static abstract class AbstractContainsResolver extends PrimaryExpressionResolver
	{
		private static Logger logger = LoggerFactory.getLogger(AbstractContainsResolver.class);

		protected FieldMetaRole role;
		protected PersistenceManager pm;
		protected boolean negate;

		protected Set<Long> negateIfNecessary(FieldMeta fieldMeta, Set<Long> positiveResult)
		{
			if (!negate)
				return positiveResult;

			Class<?> candidateClass = executionContext.getClassLoaderResolver().classForName(fieldMeta.getClassMeta().getClassName());
			Set<ClassMeta> candidateClassMetas = queryEvaluator.getCandidateClassMetas(candidateClass, true);
			Set<Long> allDataEntryIDs = queryEvaluator.getAllDataEntryIDsForCandidateClasses(candidateClassMetas);

			Set<Long> negativeResult = new HashSet<Long>(allDataEntryIDs.size() - positiveResult.size());
			for (Long dataEntryID : allDataEntryIDs) {
				if (!positiveResult.contains(dataEntryID))
					negativeResult.add(dataEntryID);
			}
			return negativeResult;
		}

		public AbstractContainsResolver(
				QueryEvaluator queryEvaluator, PrimaryExpression primaryExpression,
				FieldMetaRole role, boolean negate
		)
		{
			super(queryEvaluator, primaryExpression);
			this.role = role;
			this.pm = queryEvaluator.getPersistenceManager();
			this.negate = negate;

			if (role != FieldMetaRole.collectionElement && role != FieldMetaRole.mapKey && role != FieldMetaRole.mapValue)
				throw new IllegalArgumentException("role == " + role);
		}

		@Override
		protected final Set<Long> queryEnd(FieldMeta fieldMeta) {
			AbstractMemberMetaData mmd = fieldMeta.getDataNucleusMemberMetaData(executionContext);
			FieldMeta subFieldMeta = fieldMeta.getSubFieldMeta(role);

			boolean argumentIsPersistent;
			Class<?> argumentType;
			switch (role) {
				case collectionElement:
					argumentIsPersistent = mmd.getCollection().elementIsPersistent();
					argumentType = executionContext.getClassLoaderResolver().classForName(mmd.getCollection().getElementType());
					break;
				case mapKey:
					argumentIsPersistent = mmd.getMap().keyIsPersistent();
					argumentType = executionContext.getClassLoaderResolver().classForName(mmd.getMap().getKeyType());
					break;
				case mapValue:
					argumentIsPersistent = mmd.getMap().valueIsPersistent();
					argumentType = executionContext.getClassLoaderResolver().classForName(mmd.getMap().getValueType());
					break;
				default:
					throw new IllegalStateException("Unknown role: " + role);
			}

			return _queryEnd(pm, fieldMeta, mmd, subFieldMeta, argumentIsPersistent, argumentType);
		}

		protected abstract Set<Long> _queryEnd(
				PersistenceManager pm, FieldMeta fieldMeta, AbstractMemberMetaData mmd, FieldMeta subFieldMeta, boolean argumentIsPersistent, Class<?> argumentType
		);
	}


	/**
	 * Resolve {@link Collection#contains(Object)} with the argument being a query variable.
	 *
	 * @author Marco หงุ่ยตระกูล-Schulze - marco at nightlabs dot de
	 */
	private static class ContainsVariableResolver extends AbstractContainsResolver
	{
		private static Logger logger = LoggerFactory.getLogger(ContainsVariableResolver.class);

		private VariableExpression variableExpr;

		public ContainsVariableResolver(
				QueryEvaluator queryEvaluator, PrimaryExpression primaryExpression,
				FieldMetaRole role, VariableExpression variableExpr, boolean negate
		)
		{
			super(queryEvaluator, primaryExpression, role, negate);
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
				boolean argumentIsPersistent, Class<?> argumentType
		)
		{
			if (argumentIsPersistent || subFieldMeta.getMappedByFieldMeta(executionContext) != null) {
				AbstractExpressionEvaluator<?> eval = queryEvaluator.getExpressionEvaluator();

				Collection<Long> valueDataEntryIDs = eval.queryResultDataEntryIDs(
						new ResultDescriptor(variableExpr.getSymbol(), argumentType, subFieldMeta.getMappedByFieldMeta(executionContext))
				);
				if (valueDataEntryIDs == null)
					return null;

				Set<Long> result = new HashSet<Long>();
				if (mmd.getMappedBy() != null) {
					for (Long valueDataEntryID : valueDataEntryIDs) {
						DataEntry valueDataEntry = DataEntry.getDataEntry(pm, valueDataEntryID);
						ObjectContainer constantObjectContainer = queryEvaluator.getEncryptionHandler().decryptDataEntry(valueDataEntry, executionContext.getClassLoaderResolver());
						Object value = constantObjectContainer.getValue(
								fieldMeta.getMappedByFieldMeta(executionContext).getFieldID()
						);
						Long mappedByDataEntryID = (Long) value;
						if (mappedByDataEntryID != null)
							result.add(mappedByDataEntryID);
					}
				}
				else {
					for (Long valueDataEntryID : valueDataEntryIDs) {
						IndexEntry indexEntry = IndexEntryObjectRelationHelper.getIndexEntry(pm, subFieldMeta, valueDataEntryID);
						if (indexEntry != null) {
							IndexValue indexValue = queryEvaluator.getEncryptionHandler().decryptIndexEntry(indexEntry);
							result.addAll(indexValue.getDataEntryIDs());
						}
					}
				}
				return negateIfNecessary(fieldMeta, result);
			}
			else {
				AbstractExpressionEvaluator<?> eval = queryEvaluator.getExpressionEvaluator();
				Set<Long> result = eval.queryResultDataEntryIDs(new ResultDescriptor(variableExpr.getSymbol(), argumentType, subFieldMeta));
				return negateIfNecessary(fieldMeta, result);
			}
		}
	}

	/**
	 * Resolve {@link Collection#contains(Object)} with the argument being a concrete value (a 'constant').
	 * This concrete value is either a query parameter or a literal - i.e. no variable.
	 *
	 * @author Marco หงุ่ยตระกูล-Schulze - marco at nightlabs dot de
	 */
	private static class ContainsConstantResolver extends AbstractContainsResolver
	{
		private static Logger logger = LoggerFactory.getLogger(ContainsConstantResolver.class);
		private Object constant;

		public ContainsConstantResolver(
				QueryEvaluator queryEvaluator, PrimaryExpression primaryExpression,
				FieldMetaRole role, Object constant, boolean negate
		)
		{
			super(queryEvaluator, primaryExpression, role, negate);
			this.constant = constant;
		}

		private static Set<Long> emptyDataEntryIDs = Collections.emptySet();

		@Override
		public Set<Long> _queryEnd(
				PersistenceManager pm, FieldMeta fieldMeta,
				AbstractMemberMetaData mmd, FieldMeta subFieldMeta,
				boolean argumentIsPersistent, Class<?> argumentType
		)
		{
			if (constant != null && !argumentType.isInstance(constant)) {
				logger.debug(
						"_queryEnd: constant {} is of type {} but field {} is of type {} and thus constant cannot be contained. Returning empty set!",
						new Object[] {
								constant, constant.getClass().getName(), fieldMeta, argumentType.getClass().getName()
						}
				);
				return negateIfNecessary(fieldMeta, emptyDataEntryIDs);
			}

			if (argumentIsPersistent) {
				Long constantDataEntryID = null;
				if (constant != null) {
					ClassMeta constantClassMeta = queryEvaluator.getStoreManager().getClassMeta(executionContext, constant.getClass());
					Object constantID = executionContext.getApiAdapter().getIdForObject(constant);
					if (constantID == null)
						throw new IllegalStateException("The ApiAdapter returned null as object-ID for: " + constant);

					if (mmd.getMappedBy() != null) {
						DataEntry constantDataEntry = DataEntry.getDataEntry(pm, constantClassMeta, constantID.toString());
						ObjectContainer constantObjectContainer = queryEvaluator.getEncryptionHandler().decryptDataEntry(constantDataEntry, executionContext.getClassLoaderResolver());
						Object value = constantObjectContainer.getValue(
								fieldMeta.getMappedByFieldMeta(executionContext).getFieldID()
						);

						Long mappedByDataEntryID = (Long) value;
						if (mappedByDataEntryID == null)
							return negateIfNecessary(fieldMeta, emptyDataEntryIDs);
						else
							return negateIfNecessary(fieldMeta, Collections.singleton(mappedByDataEntryID));
					}

					constantDataEntryID = DataEntry.getDataEntryID(pm, constantClassMeta, constantID.toString());
				}
				IndexEntry indexEntry = IndexEntryObjectRelationHelper.getIndexEntry(pm, subFieldMeta, constantDataEntryID);
				if (indexEntry == null)
					return negateIfNecessary(fieldMeta, emptyDataEntryIDs);

				IndexValue indexValue = queryEvaluator.getEncryptionHandler().decryptIndexEntry(indexEntry);
				return negateIfNecessary(fieldMeta, indexValue.getDataEntryIDs());
			}
			else if (subFieldMeta.getMappedByFieldMeta(executionContext) != null) {
				FieldMeta oppositeFieldMeta = subFieldMeta.getMappedByFieldMeta(executionContext);
				IndexEntryFactory indexEntryFactory = IndexEntryFactoryRegistry.sharedInstance().getIndexEntryFactory(executionContext, oppositeFieldMeta, true);
				IndexEntry indexEntry = indexEntryFactory == null ? null : indexEntryFactory.getIndexEntry(pm, oppositeFieldMeta, constant);
				if (indexEntry == null)
					return negateIfNecessary(fieldMeta, emptyDataEntryIDs);

				IndexValue indexValue = queryEvaluator.getEncryptionHandler().decryptIndexEntry(indexEntry);
				Set<Long> result = new HashSet<Long>(indexValue.getDataEntryIDs().size());
				for (Long elementDataEntryID : indexValue.getDataEntryIDs()) {
					DataEntry elementDataEntry = DataEntry.getDataEntry(pm, elementDataEntryID);
					ObjectContainer elementObjectContainer = queryEvaluator.getEncryptionHandler().decryptDataEntry(elementDataEntry, executionContext.getClassLoaderResolver());
					Object value = elementObjectContainer.getValue(
							fieldMeta.getMappedByFieldMeta(executionContext).getFieldID()
					);

					Long mappedByDataEntryID = (Long) value;
					if (mappedByDataEntryID != null)
						result.add(mappedByDataEntryID);
				}
				return negateIfNecessary(fieldMeta, result);
			}
			else {
				IndexEntryFactory indexEntryFactory = IndexEntryFactoryRegistry.sharedInstance().getIndexEntryFactory(executionContext, subFieldMeta, true);
				IndexEntry indexEntry = indexEntryFactory == null ? null : indexEntryFactory.getIndexEntry(pm, subFieldMeta, constant);
				if (indexEntry == null)
					return negateIfNecessary(fieldMeta, emptyDataEntryIDs);

				IndexValue indexValue = queryEvaluator.getEncryptionHandler().decryptIndexEntry(indexEntry);
				return negateIfNecessary(fieldMeta, indexValue.getDataEntryIDs());
			}
		}
	}
}
