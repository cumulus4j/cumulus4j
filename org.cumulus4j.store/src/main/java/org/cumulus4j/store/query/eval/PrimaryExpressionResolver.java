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

import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import javax.jdo.PersistenceManager;

import org.cumulus4j.store.Cumulus4jStoreManager;
import org.cumulus4j.store.ObjectContainerHelper;
import org.cumulus4j.store.crypto.CryptoContext;
import org.cumulus4j.store.model.ClassMeta;
import org.cumulus4j.store.model.DataEntry;
import org.cumulus4j.store.model.DataEntryDAO;
import org.cumulus4j.store.model.FieldMeta;
import org.cumulus4j.store.model.IndexEntry;
import org.cumulus4j.store.model.IndexEntryObjectRelationHelper;
import org.cumulus4j.store.model.IndexValue;
import org.cumulus4j.store.model.ObjectContainer;
import org.cumulus4j.store.query.MemberNotQueryableException;
import org.cumulus4j.store.query.QueryEvaluator;
import org.datanucleus.metadata.AbstractMemberMetaData;
import org.datanucleus.query.expression.PrimaryExpression;
import org.datanucleus.query.expression.VariableExpression;
import org.datanucleus.query.symbol.Symbol;
import org.datanucleus.store.ExecutionContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * <p>
 * Abstract base class for easy resolving of {@link PrimaryExpression}s. This class
 * takes care of following one-to-one-relations inside the <code>PrimaryExpression</code>.
 * </p>
 * <p>
 * For example, <code>this.aaa.bbb.ccc.ddd.someSet.contains(:param)</code> requires first to
 * evaluate <code>DDD.someSet.contains(:param)</code> and then to follow the field chain back from
 * <code>ddd</code> over <code>ccc</code> over <code>bbb</code> over <code>aaa</code> finally to <code>this</code>.
 * The subclasses of <code>PrimaryExpressionResolver</code> only need to take care of the implementation
 * of the last part in the chain (in our example <code>DDD.someSet.contains(:param)</code>) - the rest is done
 * here.
 * </p>
 *
 * @author Marco หงุ่ยตระกูล-Schulze - marco at nightlabs dot de
 */
public abstract class PrimaryExpressionResolver
{
	private static final Logger logger = LoggerFactory.getLogger(PrimaryExpressionResolver.class);

	protected QueryEvaluator queryEvaluator;
	protected PrimaryExpression primaryExpression;
	protected CryptoContext cryptoContext;
	protected ExecutionContext executionContext;

	public PrimaryExpressionResolver(QueryEvaluator queryEvaluator, PrimaryExpression primaryExpression) {
		if (queryEvaluator == null)
			throw new IllegalArgumentException("queryEvaluator == null");

		if (primaryExpression == null)
			throw new IllegalArgumentException("primaryExpression == null");

		this.queryEvaluator = queryEvaluator;
		this.primaryExpression = primaryExpression;
		this.cryptoContext = queryEvaluator.getCryptoContext();
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

		Class<?> clazz = queryEvaluator.getValueType(symbol);
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

		AbstractMemberMetaData mmd = fieldMetaForNextTuple.getDataNucleusMemberMetaData(executionContext);
		if (mmd.hasExtension(Cumulus4jStoreManager.CUMULUS4J_QUERYABLE) && mmd.getValueForExtension(Cumulus4jStoreManager.CUMULUS4J_QUERYABLE).equalsIgnoreCase("false")) {
			throw new MemberNotQueryableException("Field/property " + mmd.getFullFieldName() + " is not queryable!");
		}

		if (tuples.isEmpty()) {
			return queryEnd(fieldMetaForNextTuple, classMeta);
		}
		else {
			// join
//			Class<?> nextTupleType = mmd.getType();
			Class<?> nextTupleType = fieldMetaForNextTuple.getFieldOrElementType(executionContext);
			boolean isEmbedded = true;
			ClassMeta classMetaForNextTupleType = fieldMetaForNextTuple.getEmbeddedClassMeta();
			if (classMetaForNextTupleType == null) {
				isEmbedded = false;
				classMetaForNextTupleType = queryEvaluator.getStoreManager().getClassMeta(executionContext, nextTupleType);
			}

			Set<Long> dataEntryIDsForNextTuple = queryMiddle(classMetaForNextTupleType, tuples);

			if (isEmbedded)
				return dataEntryIDsForNextTuple;

			Set<Long> result = new HashSet<Long>();
			if (fieldMetaForNextTuple.getDataNucleusMemberMetaData(executionContext).getMappedBy() == null) {
				for (Long dataEntryIDForNextTuple : dataEntryIDsForNextTuple) {
//					IndexEntry indexEntry = IndexEntryObjectRelationHelper.getIndexEntry(
//							cryptoContext, queryEvaluator.getPersistenceManagerForIndex(), fieldMetaForNextTuple, classMeta, dataEntryIDForNextTuple
//					);
					List<IndexEntry> indexEntries = IndexEntryObjectRelationHelper.getIndexEntriesIncludingSubClasses(
							cryptoContext, queryEvaluator.getPersistenceManagerForIndex(), fieldMetaForNextTuple, classMeta, dataEntryIDForNextTuple
					);
					for (IndexEntry indexEntry : indexEntries) {
						IndexValue indexValue = queryEvaluator.getEncryptionHandler().decryptIndexEntry(
								cryptoContext, indexEntry
						);
						result.addAll(indexValue.getDataEntryIDs());
					}
				}
			}
			else {
				PersistenceManager pmData = queryEvaluator.getPersistenceManagerForData();
				int keyStoreRefID = cryptoContext.getKeyStoreRefID();
				for (Long dataEntryIDForNextTuple : dataEntryIDsForNextTuple) {
					DataEntry dataEntry = new DataEntryDAO(pmData, keyStoreRefID).getDataEntry(dataEntryIDForNextTuple);
					if (dataEntry == null)
						logger.warn("queryMiddle: There is no DataEntry with dataEntryID=" + dataEntryIDForNextTuple + "! " + fieldMetaForNextTuple);
					else {
						ObjectContainer objectContainer = queryEvaluator.getEncryptionHandler().decryptDataEntry(cryptoContext, dataEntry);
						Object value = objectContainer.getValue(fieldMetaForNextTuple.getMappedByFieldMeta(executionContext).getFieldID());
						if (value != null)
							result.add(ObjectContainerHelper.referenceToDataEntryID(cryptoContext, pmData, value));
					}
				}
			}
			return result;
		}
	}

	protected abstract Set<Long> queryEnd(FieldMeta fieldMeta, ClassMeta classMeta);
}
