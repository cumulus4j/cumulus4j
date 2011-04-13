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
import org.cumulus4j.core.model.IndexEntryObjectRelationHelper;
import org.cumulus4j.core.model.IndexValue;
import org.cumulus4j.core.model.ObjectContainer;
import org.cumulus4j.core.query.QueryEvaluator;
import org.cumulus4j.core.query.QueryHelper;
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
 * Series of helper methods for processing expressions.
 */
public class ExpressionHelper
{
	/**
	 * Method to evaluate the arguments for passing in to a method invocation.
	 * @param queryEval The QueryEvaluator
	 * @param expr The invoke expression
	 * @return The argument(s)
	 */
	public static Object[] getEvaluatedInvokeArguments(QueryEvaluator queryEval, InvokeExpression expr) {
		Object[] invokeArgs = new Object[expr.getArguments().size()];

		int i=0;
		for (Expression argExpr : expr.getArguments()) {
			if (argExpr instanceof Literal)
				invokeArgs[i++] = ((Literal)argExpr).getLiteral();
			else if (argExpr instanceof ParameterExpression)
				invokeArgs[i++] = QueryUtils.getValueForParameterExpression(queryEval.getParameterValues(),
						(ParameterExpression)argExpr);
			else
				throw new UnsupportedOperationException("NYI");
		}
		return invokeArgs;
	}

	/**
	 * Method to evaluate the argument for passing in to a method invocation.
	 * @param queryEval The QueryEvaluator
	 * @param expr The invoke expression
	 * @return The argument
	 */
	public static Object getEvaluatedInvokeArgument(QueryEvaluator queryEval, InvokeExpression expr) {
		if (expr.getArguments().size() != 1) {
			throw new UnsupportedOperationException("Invalid number of arguments to " + expr.getOperation());
		}

		Object argExpr = expr.getArguments().get(0);
		if (argExpr instanceof Literal)
			return ((Literal)argExpr).getLiteral();
		else if (argExpr instanceof ParameterExpression)
			return QueryUtils.getValueForParameterExpression(queryEval.getParameterValues(), (ParameterExpression)argExpr);
		else
			throw new UnsupportedOperationException("NYI");
	}

	private static abstract class AbstractContainsResolver extends PrimaryExpressionResolver
	{
		protected FieldMetaRole role;
		protected PersistenceManager pm;
		protected boolean negate;

		protected Set<Long> negateIfNecessary(FieldMeta fieldMeta, Set<Long> positiveResult)
		{
			if (!negate)
				return positiveResult;

			Class<?> candidateClass = executionContext.getClassLoaderResolver().classForName(fieldMeta.getClassMeta().getClassName());
			Set<ClassMeta> candidateClassMetas = QueryHelper.getCandidateClassMetas(queryEvaluator.getStoreManager(), 
					executionContext, candidateClass, true);
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
	public static class ContainsVariableResolver extends AbstractContainsResolver
	{
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
						ObjectContainer constantObjectContainer = queryEvaluator.getEncryptionHandler().decryptDataEntry(executionContext, valueDataEntry);
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
							IndexValue indexValue = queryEvaluator.getEncryptionHandler().decryptIndexEntry(executionContext, indexEntry);
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
	public static class ContainsConstantResolver extends AbstractContainsResolver
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
						ObjectContainer constantObjectContainer = queryEvaluator.getEncryptionHandler().decryptDataEntry(executionContext, constantDataEntry);
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

				IndexValue indexValue = queryEvaluator.getEncryptionHandler().decryptIndexEntry(executionContext, indexEntry);
				return negateIfNecessary(fieldMeta, indexValue.getDataEntryIDs());
			}
			else if (subFieldMeta.getMappedByFieldMeta(executionContext) != null) {
				FieldMeta oppositeFieldMeta = subFieldMeta.getMappedByFieldMeta(executionContext);
				IndexEntryFactory indexEntryFactory = IndexEntryFactoryRegistry.sharedInstance().getIndexEntryFactory(executionContext, oppositeFieldMeta, true);
				IndexEntry indexEntry = indexEntryFactory == null ? null : indexEntryFactory.getIndexEntry(pm, oppositeFieldMeta, constant);
				if (indexEntry == null)
					return negateIfNecessary(fieldMeta, emptyDataEntryIDs);

				IndexValue indexValue = queryEvaluator.getEncryptionHandler().decryptIndexEntry(executionContext, indexEntry);
				Set<Long> result = new HashSet<Long>(indexValue.getDataEntryIDs().size());
				for (Long elementDataEntryID : indexValue.getDataEntryIDs()) {
					DataEntry elementDataEntry = DataEntry.getDataEntry(pm, elementDataEntryID);
					ObjectContainer elementObjectContainer = queryEvaluator.getEncryptionHandler().decryptDataEntry(executionContext, elementDataEntry);
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

				IndexValue indexValue = queryEvaluator.getEncryptionHandler().decryptIndexEntry(executionContext, indexEntry);
				return negateIfNecessary(fieldMeta, indexValue.getDataEntryIDs());
			}
		}
	}
}