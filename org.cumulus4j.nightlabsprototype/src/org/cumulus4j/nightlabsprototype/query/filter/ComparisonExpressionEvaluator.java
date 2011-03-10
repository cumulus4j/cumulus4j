package org.cumulus4j.nightlabsprototype.query.filter;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.jdo.PersistenceManager;
import javax.jdo.Query;

import org.cumulus4j.nightlabsprototype.model.ClassMeta;
import org.cumulus4j.nightlabsprototype.model.DataEntry;
import org.cumulus4j.nightlabsprototype.model.FieldMeta;
import org.cumulus4j.nightlabsprototype.model.IndexEntry;
import org.cumulus4j.nightlabsprototype.model.IndexEntryDouble;
import org.cumulus4j.nightlabsprototype.model.IndexEntryLong;
import org.cumulus4j.nightlabsprototype.model.IndexEntryString;
import org.cumulus4j.nightlabsprototype.model.IndexValue;
import org.cumulus4j.nightlabsprototype.query.QueryEvaluator;
import org.datanucleus.metadata.AbstractMemberMetaData;
import org.datanucleus.metadata.Relation;
import org.datanucleus.query.QueryUtils;
import org.datanucleus.query.expression.DyadicExpression;
import org.datanucleus.query.expression.Expression;
import org.datanucleus.query.expression.Literal;
import org.datanucleus.query.expression.ParameterExpression;
import org.datanucleus.query.expression.PrimaryExpression;
import org.datanucleus.query.expression.Expression.Operator;
import org.datanucleus.query.symbol.Symbol;

/**
 * Handles the comparisons ==, &lt;, &lt;=, &gt;, &gt;=.
 *
 * @author Marco หงุ่ยตระกูล-Schulze - marco at nightlabs dot de
 */
public class ComparisonExpressionEvaluator
extends AbstractExpressionEvaluator<DyadicExpression>
{
	public ComparisonExpressionEvaluator(QueryEvaluator queryEvaluator, AbstractExpressionEvaluator<?> parent, DyadicExpression expression) {
		super(queryEvaluator, parent, expression);
	}

	@Override
	protected Set<Long> _queryResultDataEntryIDs()
	{
		if (getLeft() instanceof InvokeExpressionEvaluator) {
			InvokeExpressionEvaluator invokeEval = (InvokeExpressionEvaluator) getLeft();
			if ("indexOf".equals(invokeEval.getExpression().getOperation())) {
				if (invokeEval.getLeft() instanceof PrimaryExpressionEvaluator) {
					PrimaryExpressionEvaluator primaryEval = (PrimaryExpressionEvaluator) invokeEval.getLeft();
					PrimaryExpression primaryExpr = primaryEval.getExpression();
					Class<?> parameterType = primaryExpr.getSymbol().getValueType();

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

					Object compareToArgument;
					if (getRight() instanceof LiteralEvaluator)
						compareToArgument = ((LiteralEvaluator)getRight()).getLiteralValue();
					else if (getRight() instanceof ParameterExpressionEvaluator)
						compareToArgument = ((ParameterExpressionEvaluator)getRight()).getParameterValue();
					else
						throw new UnsupportedOperationException("NYI");

					if (String.class.isAssignableFrom(parameterType)) {
						return handleStringIndexOf(primaryEval, invokeArgument, compareToArgument);
//						Query q = pm.newQuery(IndexEntryString.class);
//						q.setFilter(
//								"this.fieldMeta == :fieldMeta && " +
//								"this.indexKeyString.indexOf(:invokeArgument) " + getOperatorAsJDOQLSymbol() + " :compareToArgument"
//						);
//						Map<String, Object> params = new HashMap<String, Object>(3);
//						params.put("fieldMeta", primaryEval.getFieldMeta());
//						params.put("invokeArgument", invokeArgument);
//						params.put("compareToArgument", compareToArgument);
//
//						@SuppressWarnings("unchecked")
//						Collection<? extends IndexEntry> indexEntries = (Collection<? extends IndexEntry>) q.executeWithMap(params);
//
//						Set<Long> result = new HashSet<Long>();
//						for (IndexEntry indexEntry : indexEntries) {
//							IndexValue indexValue = getQueryEvaluator().getEncryptionHandler().decryptIndexEntry(indexEntry);
//							result.addAll(indexValue.getDataEntryIDs());
//						}
//						q.closeAll();
//						return result;
					}
					throw new UnsupportedOperationException("NYI");
				}
				throw new UnsupportedOperationException("NYI");
			}
			throw new UnsupportedOperationException("NYI");
		}

		if (getLeft() instanceof PrimaryExpressionEvaluator) {
			Object compareToArgument;
			if (getRight() instanceof LiteralEvaluator)
				compareToArgument = ((LiteralEvaluator)getRight()).getLiteralValue();
			else if (getRight() instanceof ParameterExpressionEvaluator)
				compareToArgument = ((ParameterExpressionEvaluator)getRight()).getParameterValue();
			else
				throw new UnsupportedOperationException("NYI");

			if (Expression.OP_EQ == getExpression().getOperator())
				return handleEqualsWithConcreteValue(((PrimaryExpressionEvaluator)getLeft()).getFieldMeta(), compareToArgument);

			throw new UnsupportedOperationException("NYI"); // TODO other operators [<, >, <=, >=]!
		}

		if (getRight() instanceof PrimaryExpressionEvaluator) {
			Object compareToArgument;
			if (getLeft() instanceof LiteralEvaluator)
				compareToArgument = ((LiteralEvaluator)getRight()).getLiteralValue();
			else if (getLeft() instanceof ParameterExpressionEvaluator)
				compareToArgument = ((ParameterExpressionEvaluator)getRight()).getParameterValue();
			else
				throw new UnsupportedOperationException("NYI");

			if (Expression.OP_EQ == getExpression().getOperator())
				return handleEqualsWithConcreteValue(((PrimaryExpressionEvaluator)getRight()).getFieldMeta(), compareToArgument);

			throw new UnsupportedOperationException("NYI"); // TODO other operators [<, >, <=, >=]!
		}

		throw new UnsupportedOperationException("NYI");
	}

	private Set<Long> handleStringIndexOf(
			PrimaryExpressionEvaluator primaryEval,
			Object invokeArgument, Object compareToArgument
	)
	{
		List<String> tuples = new LinkedList<String>(primaryEval.getExpression().getTuples());
		if (tuples.size() < 1)
			throw new IllegalStateException("primaryExpression.tuples.size < 1");

		if (getQueryEvaluator().getCandidateAlias().equals(tuples.get(0))) {
			tuples.remove(0);
		}
		Symbol symbol = getQueryEvaluator().getCompilation().getSymbolTable().getSymbol(getQueryEvaluator().getCandidateAlias());
		if (symbol == null)
			throw new IllegalStateException("getQueryEvaluator().getCompilation().getSymbolTable().getSymbol(getQueryEvaluator().getCandidateAlias()) returned null! candidateAlias=" + getQueryEvaluator().getCandidateAlias());

		Class<?> clazz = symbol.getValueType();
		ClassMeta classMeta = getQueryEvaluator().getStoreManager().getClassMeta(
				getQueryEvaluator().getExecutionContext(), clazz
		);
		return handleStringIndexOf(classMeta, tuples, invokeArgument, compareToArgument);
	}

	private Set<Long> handleStringIndexOf(
			ClassMeta classMeta, List<String> tuples,
			Object invokeArgument, Object compareToArgument
	)
	{
		if (tuples.size() < 1)
			throw new IllegalStateException("tuples.size < 1");

		tuples = new LinkedList<String>(tuples);
		String nextTuple = tuples.remove(0);
		FieldMeta fieldMetaForNextTuple = classMeta.getFieldMeta(null, nextTuple);
		if (fieldMetaForNextTuple == null)
			throw new IllegalStateException("Neither the class " + classMeta.getClassName() + " nor one of its superclasses contain a field named \"" + nextTuple + "\"!");

		if (tuples.isEmpty()) {
			return handleStringIndexOf(fieldMetaForNextTuple, invokeArgument, compareToArgument);
		}
		else {
			// join
			Class<?> nextTupleType = fieldMetaForNextTuple.getDataNucleusMemberMetaData(getQueryEvaluator().getExecutionContext()).getType();
			ClassMeta classMetaForNextTupleType = getQueryEvaluator().getStoreManager().getClassMeta(
					getQueryEvaluator().getExecutionContext(),
					nextTupleType
			);
			Set<Long> dataSetEntryIDsForNextTuple = handleStringIndexOf(
					classMetaForNextTupleType, tuples, invokeArgument, compareToArgument
			);
			Set<Long> result = new HashSet<Long>();
			for (Long dataSetEntryIDForNextTuple : dataSetEntryIDsForNextTuple) {
				IndexEntry indexEntry = IndexEntryLong.getIndexEntry(
						getPersistenceManager(), fieldMetaForNextTuple, dataSetEntryIDForNextTuple
				);
				if (indexEntry != null) {
					IndexValue indexValue = getQueryEvaluator().getEncryptionHandler().decryptIndexEntry(indexEntry);
					result.addAll(indexValue.getDataEntryIDs());
				}
			}
			return result;
		}
	}

	private Set<Long> handleStringIndexOf(
			FieldMeta fieldMeta,
			Object invokeArgument, Object compareToArgument
	)
	{
		Query q = getPersistenceManager().newQuery(IndexEntryString.class);
		q.setFilter(
				"this.fieldMeta == :fieldMeta && " +
				"this.indexKeyString.indexOf(:invokeArgument) " + getOperatorAsJDOQLSymbol() + " :compareToArgument"
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

	private Set<Long> handleEqualsWithConcreteValue(FieldMeta fieldMeta, Object value)
	{
		PersistenceManager pm = getPersistenceManager();

		AbstractMemberMetaData mmd = fieldMeta.getDataNucleusMemberMetaData(getQueryEvaluator().getExecutionContext());
		Class<?> fieldType = mmd.getType();

		if (String.class.isAssignableFrom(fieldType)) {
			IndexEntry indexEntry = IndexEntryString.getIndexEntry(pm, fieldMeta, (String) value);
			if (indexEntry == null)
				return Collections.emptySet();

			IndexValue indexValue = getQueryEvaluator().getEncryptionHandler().decryptIndexEntry(indexEntry);
			return indexValue.getDataEntryIDs();
		}

		if (
				Long.class.isAssignableFrom(fieldType) || Integer.class.isAssignableFrom(fieldType) ||
				Short.class.isAssignableFrom(fieldType) || Byte.class.isAssignableFrom(fieldType) ||
				long.class.isAssignableFrom(fieldType) || int.class.isAssignableFrom(fieldType) ||
				short.class.isAssignableFrom(fieldType) || byte.class.isAssignableFrom(fieldType)
		)
		{
			IndexEntry indexEntry = IndexEntryLong.getIndexEntry(pm, fieldMeta, (Long) value);
			if (indexEntry == null)
				return Collections.emptySet();

			IndexValue indexValue = getQueryEvaluator().getEncryptionHandler().decryptIndexEntry(indexEntry);
			return indexValue.getDataEntryIDs();
		}

		if (
				Double.class.isAssignableFrom(fieldType) || Float.class.isAssignableFrom(fieldType) ||
				double.class.isAssignableFrom(fieldType) || float.class.isAssignableFrom(fieldType)
		)
		{
			IndexEntry indexEntry = IndexEntryDouble.getIndexEntry(pm, fieldMeta, (Double) value);
			if (indexEntry == null)
				return Collections.emptySet();

			IndexValue indexValue = getQueryEvaluator().getEncryptionHandler().decryptIndexEntry(indexEntry);
			return indexValue.getDataEntryIDs();
		}

		int relationType = mmd.getRelationType(getQueryEvaluator().getExecutionContext().getClassLoaderResolver());

		if (Relation.isRelationSingleValued(relationType)) {
			Long valueDataEntryID = null;
			if (value != null) {
				ClassMeta valueClassMeta = getQueryEvaluator().getStoreManager().getClassMeta(getQueryEvaluator().getExecutionContext(), value.getClass());
				Object valueID = getQueryEvaluator().getExecutionContext().getApiAdapter().getIdForObject(value);
				if (valueID == null)
					throw new IllegalStateException("The ApiAdapter returned null as object-ID for: " + value);

				valueDataEntryID = DataEntry.getDataEntryID(pm, valueClassMeta, valueID.toString());
			}
			IndexEntry indexEntry = IndexEntryLong.getIndexEntry(pm, fieldMeta, valueDataEntryID);

			IndexValue indexValue = getQueryEvaluator().getEncryptionHandler().decryptIndexEntry(indexEntry);
			return indexValue.getDataEntryIDs();
		}

		throw new UnsupportedOperationException("NYI");
	}

	private String getOperatorAsJDOQLSymbol()
	{
		Operator op = getExpression().getOperator();
		if (Expression.OP_EQ == op)
			return "==";
		if (Expression.OP_LT == op)
			return "<";
		if (Expression.OP_LTEQ == op)
			return "<=";
		if (Expression.OP_GT == op)
			return ">";
		if (Expression.OP_GTEQ == op)
			return ">=";

		throw new UnsupportedOperationException("NYI");
	}
}
