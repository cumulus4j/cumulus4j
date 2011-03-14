package org.cumulus4j.core.query.filter;

import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import org.cumulus4j.core.model.ClassMeta;
import org.cumulus4j.core.model.FieldMeta;
import org.cumulus4j.core.model.IndexEntry;
import org.cumulus4j.core.model.IndexEntryOneToOneRelationHelper;
import org.cumulus4j.core.model.IndexValue;
import org.cumulus4j.core.query.QueryEvaluator;
import org.datanucleus.query.expression.PrimaryExpression;
import org.datanucleus.query.symbol.Symbol;

/**
 * @author Marco หงุ่ยตระกูล-Schulze - marco at nightlabs dot de
 */
public abstract class PrimaryExpressionResolver
{
	protected QueryEvaluator queryEvaluator;
	protected PrimaryExpression primaryExpression;

	public PrimaryExpressionResolver(QueryEvaluator queryEvaluator, PrimaryExpression primaryExpression) {
		if (queryEvaluator == null)
			throw new IllegalArgumentException("queryEvaluator == null");

		if (primaryExpression == null)
			throw new IllegalArgumentException("primaryExpression == null");

		this.queryEvaluator = queryEvaluator;
		this.primaryExpression = primaryExpression;
	}

	public Set<Long> query()
	{
		List<String> tuples = new LinkedList<String>(primaryExpression.getTuples());
		if (tuples.size() < 1)
			throw new IllegalStateException("primaryExpression.tuples.size < 1");

		if (queryEvaluator.getCandidateAlias().equals(tuples.get(0))) {
			tuples.remove(0);
		}
		Symbol symbol = queryEvaluator.getCompilation().getSymbolTable().getSymbol(queryEvaluator.getCandidateAlias());
		if (symbol == null)
			throw new IllegalStateException("getQueryEvaluator().getCompilation().getSymbolTable().getSymbol(getQueryEvaluator().getCandidateAlias()) returned null! candidateAlias=" + queryEvaluator.getCandidateAlias());

		Class<?> clazz = symbol.getValueType();
		ClassMeta classMeta = queryEvaluator.getStoreManager().getClassMeta(
				queryEvaluator.getExecutionContext(), clazz
		);
		return queryMiddle(classMeta, tuples);
	}

	protected Set<Long> queryMiddle(ClassMeta classMeta, List<String> tuples)
	{
		if (tuples.size() < 1)
			throw new IllegalStateException("tuples.size < 1");

		tuples = new LinkedList<String>(tuples);
		String nextTuple = tuples.remove(0);
		FieldMeta fieldMetaForNextTuple = classMeta.getFieldMeta(null, nextTuple);
		if (fieldMetaForNextTuple == null)
			throw new IllegalStateException("Neither the class " + classMeta.getClassName() + " nor one of its superclasses contain a field named \"" + nextTuple + "\"!");

		if (tuples.isEmpty()) {
			return queryEnd(fieldMetaForNextTuple);
		}
		else {
			// join
			Class<?> nextTupleType = fieldMetaForNextTuple.getDataNucleusMemberMetaData(queryEvaluator.getExecutionContext()).getType();
			ClassMeta classMetaForNextTupleType = queryEvaluator.getStoreManager().getClassMeta(
					queryEvaluator.getExecutionContext(),
					nextTupleType
			);
			Set<Long> dataSetEntryIDsForNextTuple = queryMiddle(classMetaForNextTupleType, tuples);
			Set<Long> result = new HashSet<Long>();
			for (Long dataSetEntryIDForNextTuple : dataSetEntryIDsForNextTuple) {
				IndexEntry indexEntry = IndexEntryOneToOneRelationHelper.getIndexEntry(
						queryEvaluator.getPersistenceManager(), fieldMetaForNextTuple, dataSetEntryIDForNextTuple
				);
				if (indexEntry != null) {
					IndexValue indexValue = queryEvaluator.getEncryptionHandler().decryptIndexEntry(indexEntry);
					result.addAll(indexValue.getDataEntryIDs());
				}
			}
			return result;
		}
	}

	protected abstract Set<Long> queryEnd(FieldMeta fieldMeta);
}
