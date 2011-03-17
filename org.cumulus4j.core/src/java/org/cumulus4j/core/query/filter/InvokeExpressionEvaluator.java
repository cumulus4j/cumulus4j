package org.cumulus4j.core.query.filter;

import java.util.Collection;
import java.util.Collections;
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

public class InvokeExpressionEvaluator
extends AbstractExpressionEvaluator<InvokeExpression>
{
	public InvokeExpressionEvaluator(QueryEvaluator queryEvaluator, AbstractExpressionEvaluator<?> parent, InvokeExpression expression)
	{
		super(queryEvaluator, parent, expression);
	}

	@Override
	protected Set<Long> _queryResultDataEntryIDs() {
		if ("contains".equals(this.getExpression().getOperation())) {
			if (this.getLeft() instanceof PrimaryExpressionEvaluator) {
				PrimaryExpressionEvaluator primaryEval = (PrimaryExpressionEvaluator) this.getLeft();
				PrimaryExpression primaryExpr = primaryEval.getExpression();
				Class<?> parameterType = primaryExpr.getSymbol().getValueType();

				if (this.getExpression().getArguments().size() != 1)
					throw new IllegalStateException("contains(...) expects exactly one argument, but there are " + this.getExpression().getArguments().size());

				Expression invokeArgExpr = this.getExpression().getArguments().get(0);

				Object invokeArgument;
				if (invokeArgExpr instanceof Literal)
					invokeArgument = ((Literal)invokeArgExpr).getLiteral();
				else if (invokeArgExpr instanceof ParameterExpression)
					invokeArgument = QueryUtils.getValueForParameterExpression(getQueryEvaluator().getParameterValues(), (ParameterExpression)invokeArgExpr);
				else
					throw new UnsupportedOperationException("NYI");

				if (Collection.class.isAssignableFrom(parameterType))
					return new CollectionContainsValueResolver(getQueryEvaluator(), primaryExpr, invokeArgument).query();

				throw new UnsupportedOperationException("NYI");
			}
			throw new UnsupportedOperationException("NYI");
		}

		throw new UnsupportedOperationException("Cannot be evaluated alone without loading *ALL* records of a certain type!");
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
