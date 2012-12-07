/*
 * Cumulus4j - Securing your data in the cloud - http://cumulus4j.org
 * Copyright (C) 2011 NightLabs Consulting GmbH
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.cumulus4j.store.query.eval;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.cumulus4j.store.model.ClassMeta;
import org.cumulus4j.store.model.DataEntry;
import org.cumulus4j.store.model.DataEntryDAO;
import org.cumulus4j.store.model.FieldMeta;
import org.cumulus4j.store.model.FieldMetaRole;
import org.cumulus4j.store.model.IndexEntry;
import org.cumulus4j.store.model.IndexEntryFactory;
import org.cumulus4j.store.model.IndexEntryObjectRelationHelper;
import org.cumulus4j.store.model.IndexValue;
import org.cumulus4j.store.model.ObjectContainer;
import org.cumulus4j.store.query.QueryEvaluator;
import org.cumulus4j.store.query.QueryHelper;
import org.cumulus4j.store.query.method.MethodEvaluator;
import org.datanucleus.ClassLoaderResolver;
import org.datanucleus.metadata.AbstractMemberMetaData;
import org.datanucleus.plugin.ConfigurationElement;
import org.datanucleus.query.QueryUtils;
import org.datanucleus.query.expression.Expression;
import org.datanucleus.query.expression.Expression.Operator;
import org.datanucleus.query.expression.InvokeExpression;
import org.datanucleus.query.expression.Literal;
import org.datanucleus.query.expression.ParameterExpression;
import org.datanucleus.query.expression.PrimaryExpression;
import org.datanucleus.query.expression.VariableExpression;
import org.datanucleus.store.StoreManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Series of helper methods for processing expressions.
 */
public class ExpressionHelper
{
	private static Map<String, Class<? extends MethodEvaluator>> method2EvaluatorClass = new HashMap<String, Class<? extends MethodEvaluator>>();

	/**
	 * Accessor for the evaluator object for use of method xxx(...) of class Yyy in queries.
	 * @param storeMgr Store Manager
	 * @param clr ClassLoader resolver
	 * @param clsName The class on which to invoke the method
	 * @param method The method to call on the class
	 * @return The MethodEvaluator
	 */
	@SuppressWarnings("unchecked")
	public static MethodEvaluator createMethodEvaluatorForMethodOfClass(StoreManager storeMgr, ClassLoaderResolver clr,
			String clsName, String method) {

		String key = clsName + "." + method;

		MethodEvaluator eval = null;
		Class<? extends MethodEvaluator> evaluatorCls = method2EvaluatorClass.get(key);
		if (evaluatorCls == null) {
			ConfigurationElement elem =
					storeMgr.getNucleusContext().getPluginManager().getConfigurationElementForExtension(
							"org.cumulus4j.store.query_method", new String[]{"class", "method"}, new String[]{clsName, method});
			if (elem == null) {
				throw new UnsupportedOperationException("Invocation of method \""+method+"\" on object of type \""+clsName+"\" is not supported");
			}

			String evaluatorClassName = elem.getAttribute("evaluator");
			evaluatorCls = clr.classForName(evaluatorClassName);
			try {
				eval = evaluatorCls.newInstance();
			} catch (Exception e) {
				throw new UnsupportedOperationException("Attempt to instantiate an evaluator for " + key + " failed: " + e, e);
			}

			// Cache  the method for later use
			method2EvaluatorClass.put(key, evaluatorCls);
		}

		if (eval == null) {
			try {
				eval = evaluatorCls.newInstance();
			} catch (Exception e) {
				throw new UnsupportedOperationException("Attempt to instantiate an evaluator for " + key + " failed: " + e, e);
			}
		}

		return eval;
	}

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
		protected boolean negate;

		protected Set<Long> negateIfNecessary(FieldMeta fieldMeta, Set<Long> positiveResult)
		{
			if (!negate) {
				return positiveResult;
			}

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
			this.negate = negate;

			if (role != FieldMetaRole.collectionElement && role != FieldMetaRole.mapKey && role != FieldMetaRole.mapValue)
				throw new IllegalArgumentException("role == " + role);
		}

		@Override
		protected final Set<Long> queryEnd(FieldMeta fieldMeta, ClassMeta classMeta) {
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

			return _queryEnd(fieldMeta, classMeta, mmd, subFieldMeta, argumentIsPersistent, argumentType);
		}

		protected abstract Set<Long> _queryEnd(FieldMeta fieldMeta, ClassMeta classMeta, AbstractMemberMetaData mmd, FieldMeta subFieldMeta, boolean argumentIsPersistent, Class<?> argumentType
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
		public Set<Long> _queryEnd(FieldMeta fieldMeta, ClassMeta classMeta, AbstractMemberMetaData mmd,
				FieldMeta subFieldMeta, boolean argumentIsPersistent, Class<?> argumentType)
		{
			if (argumentIsPersistent || subFieldMeta.getMappedByFieldMeta(executionContext) != null) {
				AbstractExpressionEvaluator<?> eval = queryEvaluator.getExpressionEvaluator();

				Collection<Long> valueDataEntryIDs = eval.queryResultDataEntryIDs(
						new ResultDescriptor(variableExpr.getSymbol(), argumentType, subFieldMeta.getMappedByFieldMeta(executionContext), classMeta)
//						new ResultDescriptor(variableExpr.getSymbol(), argumentType, null, classMeta)
				);
				if (valueDataEntryIDs == null)
					return null;

				Set<Long> result = new HashSet<Long>();
				if (mmd.getMappedBy() != null) {
					for (Long valueDataEntryID : valueDataEntryIDs) {
						DataEntry valueDataEntry = new DataEntryDAO(
								queryEvaluator.getPersistenceManagerForData(), cryptoContext.getKeyStoreRefID()
						).getDataEntry(valueDataEntryID);
						ObjectContainer constantObjectContainer = queryEvaluator.getEncryptionHandler().decryptDataEntry(cryptoContext, valueDataEntry);
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
//						IndexEntry indexEntry =
//							IndexEntryObjectRelationHelper.getIndexEntry(cryptoContext, queryEvaluator.getPersistenceManagerForIndex(), subFieldMeta, classMeta, valueDataEntryID);
//						ClassMeta fieldOrElementTypeClassMeta = subFieldMeta.getFieldOrElementTypeClassMeta(executionContext);
						List<IndexEntry> indexEntries = IndexEntryObjectRelationHelper.getIndexEntriesIncludingSubClasses(
								cryptoContext, queryEvaluator.getPersistenceManagerForIndex(), subFieldMeta, fieldMeta.getClassMeta(), valueDataEntryID
						);
						for (IndexEntry indexEntry : indexEntries) {
							IndexValue indexValue = queryEvaluator.getEncryptionHandler().decryptIndexEntry(cryptoContext, indexEntry);
							result.addAll(indexValue.getDataEntryIDs());
						}
					}
				}
				return negateIfNecessary(fieldMeta, result);
			}
			else {
				AbstractExpressionEvaluator<?> eval = queryEvaluator.getExpressionEvaluator();
				Set<Long> result = eval.queryResultDataEntryIDs(new ResultDescriptor(variableExpr.getSymbol(), argumentType, subFieldMeta, classMeta));
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
		public Set<Long> _queryEnd(FieldMeta fieldMeta, ClassMeta classMeta, AbstractMemberMetaData mmd,
				FieldMeta subFieldMeta, boolean argumentIsPersistent, Class<?> argumentType)
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
						DataEntry constantDataEntry = new DataEntryDAO(
								queryEvaluator.getPersistenceManagerForData(), cryptoContext.getKeyStoreRefID()
						).getDataEntry(
								constantClassMeta, constantID.toString()
						);
						ObjectContainer constantObjectContainer = queryEvaluator.getEncryptionHandler().decryptDataEntry(cryptoContext, constantDataEntry);
						Object value = constantObjectContainer.getValue(
								fieldMeta.getMappedByFieldMeta(executionContext).getFieldID()
						);

						Long mappedByDataEntryID = (Long) value;
						if (mappedByDataEntryID == null)
							return negateIfNecessary(fieldMeta, emptyDataEntryIDs);
						else
							return negateIfNecessary(fieldMeta, Collections.singleton(mappedByDataEntryID));
					}

					constantDataEntryID = new DataEntryDAO(
							queryEvaluator.getPersistenceManagerForData(), cryptoContext.getKeyStoreRefID()
					).getDataEntryID(constantClassMeta, constantID.toString());
				}
//				IndexEntry indexEntry = IndexEntryObjectRelationHelper.getIndexEntry(cryptoContext,
//						queryEvaluator.getPersistenceManagerForIndex(), subFieldMeta, classMeta, constantDataEntryID);
//				ClassMeta fieldOrElementTypeClassMeta = subFieldMeta.getFieldOrElementTypeClassMeta(executionContext);
				List<IndexEntry> indexEntries = IndexEntryObjectRelationHelper.getIndexEntriesIncludingSubClasses(
						cryptoContext, queryEvaluator.getPersistenceManagerForIndex(), subFieldMeta, fieldMeta.getClassMeta(), constantDataEntryID
				);
				return negateIfNecessary(fieldMeta, getDataEntryIDsFromIndexEntries(indexEntries));
			}
			else if (subFieldMeta.getMappedByFieldMeta(executionContext) != null) {
				FieldMeta oppositeFieldMeta = subFieldMeta.getMappedByFieldMeta(executionContext);
				IndexEntryFactory indexEntryFactory =
					queryEvaluator.getStoreManager().getIndexFactoryRegistry().getIndexEntryFactory(executionContext, oppositeFieldMeta, true);

				if (indexEntryFactory == null)
					return negateIfNecessary(fieldMeta, emptyDataEntryIDs);

//				IndexEntry indexEntry = indexEntryFactory.getIndexEntry(cryptoContext, queryEvaluator.getPersistenceManagerForIndex(), oppositeFieldMeta, classMeta, constant);
//				ClassMeta fieldOrElementTypeClassMeta = oppositeFieldMeta.getFieldOrElementTypeClassMeta(executionContext);
				List<IndexEntry> indexEntries = indexEntryFactory.getIndexEntriesIncludingSubClasses(
						cryptoContext, queryEvaluator.getPersistenceManagerForIndex(), oppositeFieldMeta, oppositeFieldMeta.getClassMeta(), constant
				);
				if (indexEntries.isEmpty())
					return negateIfNecessary(fieldMeta, emptyDataEntryIDs);

				Set<Long> result = null;
				for (IndexEntry indexEntry : indexEntries) {
					IndexValue indexValue = queryEvaluator.getEncryptionHandler().decryptIndexEntry(cryptoContext, indexEntry);
					if (result == null)
						result = new HashSet<Long>(indexValue.getDataEntryIDs().size());

					for (Long elementDataEntryID : indexValue.getDataEntryIDs()) {
						DataEntry elementDataEntry = new DataEntryDAO(
								queryEvaluator.getPersistenceManagerForData(), cryptoContext.getKeyStoreRefID()
								).getDataEntry(elementDataEntryID);
						ObjectContainer elementObjectContainer = queryEvaluator.getEncryptionHandler().decryptDataEntry(cryptoContext, elementDataEntry);
						Object value = elementObjectContainer.getValue(
								fieldMeta.getMappedByFieldMeta(executionContext).getFieldID()
								);

						Long mappedByDataEntryID = (Long) value;
						if (mappedByDataEntryID != null)
							result.add(mappedByDataEntryID);
					}
				}
				return negateIfNecessary(fieldMeta, result);
			}
			else {
				IndexEntryFactory indexEntryFactory = queryEvaluator.getStoreManager().getIndexFactoryRegistry().getIndexEntryFactory(executionContext, subFieldMeta, true);
				if (indexEntryFactory == null)
					return negateIfNecessary(fieldMeta, emptyDataEntryIDs);

//				IndexEntry indexEntry = indexEntryFactory.getIndexEntry(cryptoContext, queryEvaluator.getPersistenceManagerForIndex(), subFieldMeta, classMeta, constant);
//				if (indexEntry == null)
//					return negateIfNecessary(fieldMeta, emptyDataEntryIDs);
//
//				IndexValue indexValue = queryEvaluator.getEncryptionHandler().decryptIndexEntry(cryptoContext, indexEntry);
//				return negateIfNecessary(fieldMeta, indexValue.getDataEntryIDs());
//				ClassMeta fieldOrElementTypeClassMeta = subFieldMeta.getFieldOrElementTypeClassMeta(executionContext);
				List<IndexEntry> indexEntries = indexEntryFactory.getIndexEntriesIncludingSubClasses(
						cryptoContext, queryEvaluator.getPersistenceManagerForIndex(), subFieldMeta, fieldMeta.getClassMeta(), constant
				);
				return negateIfNecessary(fieldMeta, getDataEntryIDsFromIndexEntries(indexEntries));
			}
		}

		protected Set<Long> getDataEntryIDsFromIndexEntries(Collection<? extends IndexEntry> indexEntries) {
			if (indexEntries.isEmpty())
				return emptyDataEntryIDs;

			Set<Long> dataEntryIDs = null;
			for (IndexEntry indexEntry : indexEntries) {
				IndexValue indexValue = queryEvaluator.getEncryptionHandler().decryptIndexEntry(cryptoContext, indexEntry);
				if (dataEntryIDs == null)
					dataEntryIDs = indexEntries.size() == 1 ? indexValue.getDataEntryIDs() : new HashSet<Long>(indexValue.getDataEntryIDs());
				else
					dataEntryIDs.addAll(indexValue.getDataEntryIDs());
			}
			return dataEntryIDs;
		}
	}

	public static String getOperatorAsJDOQLSymbol(Operator operator, boolean negate)
	{
		if (Expression.OP_EQ == operator)
			return negate ? "!=" : "==";
		if (Expression.OP_NOTEQ == operator)
			return negate ? "==" : "!=";
		if (Expression.OP_LT == operator)
			return negate ? ">=" : "<";
		if (Expression.OP_LTEQ == operator)
			return negate ? ">"  : "<=";
		if (Expression.OP_GT == operator)
			return negate ? "<=" : ">";
		if (Expression.OP_GTEQ == operator)
			return negate ? "<"  : ">=";

		throw new UnsupportedOperationException("NYI");
	}
}