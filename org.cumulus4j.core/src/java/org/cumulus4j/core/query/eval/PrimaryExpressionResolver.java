package org.cumulus4j.core.query.eval;

import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import org.cumulus4j.core.ObjectContainerHelper;
import org.cumulus4j.core.model.ClassMeta;
import org.cumulus4j.core.model.DataEntry;
import org.cumulus4j.core.model.FieldMeta;
import org.cumulus4j.core.model.IndexEntry;
import org.cumulus4j.core.model.IndexEntryObjectRelationHelper;
import org.cumulus4j.core.model.IndexValue;
import org.cumulus4j.core.model.ObjectContainer;
import org.cumulus4j.core.query.QueryEvaluator;
import org.datanucleus.query.expression.PrimaryExpression;
import org.datanucleus.query.expression.VariableExpression;
import org.datanucleus.query.symbol.Symbol;
import org.datanucleus.store.ExecutionContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Marco หงุ่ยตระกูล-Schulze - marco at nightlabs dot de
 */
public abstract class PrimaryExpressionResolver
{
	private static final Logger logger = LoggerFactory.getLogger(PrimaryExpressionResolver.class);

	protected QueryEvaluator queryEvaluator;
	protected PrimaryExpression primaryExpression;
	protected ExecutionContext executionContext;

	public PrimaryExpressionResolver(QueryEvaluator queryEvaluator, PrimaryExpression primaryExpression) {
		if (queryEvaluator == null)
			throw new IllegalArgumentException("queryEvaluator == null");

		if (primaryExpression == null)
			throw new IllegalArgumentException("primaryExpression == null");

		this.queryEvaluator = queryEvaluator;
		this.primaryExpression = primaryExpression;
		this.executionContext = queryEvaluator.getExecutionContext();
	}

	public Set<Long> query()
	{
		List<String> tuples = new LinkedList<String>(primaryExpression.getTuples());
		if (tuples.size() < 1)
			throw new IllegalStateException("primaryExpression.tuples.size < 1");

		Symbol symbol;
		if (primaryExpression.getLeft() instanceof VariableExpression) {
			symbol = ((VariableExpression)primaryExpression.getLeft()).getSymbol();
			if (symbol == null)
				throw new IllegalStateException("((VariableExpression)primaryExpression.getLeft()).getSymbol() returned null!");
		}
		else if (primaryExpression.getLeft() == null) {
			if (queryEvaluator.getCandidateAlias().equals(tuples.get(0)))
				tuples.remove(0);

			symbol = queryEvaluator.getCompilation().getSymbolTable().getSymbol(queryEvaluator.getCandidateAlias());
			if (symbol == null)
				throw new IllegalStateException("getQueryEvaluator().getCompilation().getSymbolTable().getSymbol(getQueryEvaluator().getCandidateAlias()) returned null! candidateAlias=" + queryEvaluator.getCandidateAlias());
		}
		else
			throw new UnsupportedOperationException("NYI");

		Class<?> clazz = symbol.getValueType();
		ClassMeta classMeta = queryEvaluator.getStoreManager().getClassMeta(executionContext, clazz);
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
			Class<?> nextTupleType = fieldMetaForNextTuple.getDataNucleusMemberMetaData(executionContext).getType();
			ClassMeta classMetaForNextTupleType = queryEvaluator.getStoreManager().getClassMeta(executionContext, nextTupleType);
			Set<Long> dataEntryIDsForNextTuple = queryMiddle(classMetaForNextTupleType, tuples);
			Set<Long> result = new HashSet<Long>();
			if (fieldMetaForNextTuple.getDataNucleusMemberMetaData(executionContext).getMappedBy() == null) {
				for (Long dataEntryIDForNextTuple : dataEntryIDsForNextTuple) {
					IndexEntry indexEntry = IndexEntryObjectRelationHelper.getIndexEntry(
							queryEvaluator.getPersistenceManager(), fieldMetaForNextTuple, dataEntryIDForNextTuple
					);
					if (indexEntry != null) {
						IndexValue indexValue = queryEvaluator.getEncryptionHandler().decryptIndexEntry(indexEntry);
						result.addAll(indexValue.getDataEntryIDs());
					}
				}
			}
			else {
				for (Long dataEntryIDForNextTuple : dataEntryIDsForNextTuple) {
					DataEntry dataEntry = DataEntry.getDataEntry(queryEvaluator.getPersistenceManager(), dataEntryIDForNextTuple);
					if (dataEntry == null)
						logger.warn("queryMiddle: There is no DataEntry with dataEntryID=" + dataEntryIDForNextTuple + "! " + fieldMetaForNextTuple);
					else {
						ObjectContainer objectContainer = queryEvaluator.getEncryptionHandler().decryptDataEntry(dataEntry, executionContext.getClassLoaderResolver());
						Object value = objectContainer.getValue(fieldMetaForNextTuple.getMappedByFieldMeta(executionContext).getFieldID());
						if (value != null)
							result.add(ObjectContainerHelper.referenceToDataEntryID(executionContext, queryEvaluator.getPersistenceManager(), value));
					}
				}
			}
			return result;
		}
	}

	protected abstract Set<Long> queryEnd(FieldMeta fieldMeta);
}
