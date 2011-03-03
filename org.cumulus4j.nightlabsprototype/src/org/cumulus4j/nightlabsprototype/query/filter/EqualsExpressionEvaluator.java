package org.cumulus4j.nightlabsprototype.query.filter;

import java.util.Collections;
import java.util.Set;

import javax.jdo.PersistenceManager;

import org.cumulus4j.nightlabsprototype.model.FieldMeta;
import org.cumulus4j.nightlabsprototype.model.IndexEntry;
import org.cumulus4j.nightlabsprototype.model.IndexValue;
import org.cumulus4j.nightlabsprototype.query.QueryEvaluator;
import org.datanucleus.query.expression.DyadicExpression;
import org.datanucleus.query.expression.PrimaryExpression;

public class EqualsExpressionEvaluator
extends AbstractExpressionEvaluator<DyadicExpression>
{
	public EqualsExpressionEvaluator(QueryEvaluator queryEvaluator, AbstractExpressionEvaluator<?> parent, DyadicExpression expression) {
		super(queryEvaluator, parent, expression);
	}

	@Override
	protected Set<Long> _queryResultDataEntryIDs()
	{
		PersistenceManager pm = getPersistenceManager();

		PrimaryExpressionEvaluator primaryEval = getLeftOrRightExactlyOne(PrimaryExpressionEvaluator.class, true);
		PrimaryExpression primaryExpr = primaryEval.getExpression();
		ParameterExpressionEvaluator parameterEval = getLeftOrRightExactlyOne(ParameterExpressionEvaluator.class, true);

		FieldMeta fieldMeta = primaryEval.getFieldMeta();
		Object parameterValue = parameterEval.getParameterValue();
		Class<?> parameterType = primaryExpr.getSymbol().getValueType();

		if (String.class.isAssignableFrom(parameterType)) {
			IndexEntry indexEntry = IndexEntry.getIndexEntry(pm, fieldMeta, (String) parameterValue);
			if (indexEntry == null)
				return Collections.emptySet();

			IndexValue indexValue = getQueryEvaluator().getEncryptionHandler().decryptIndexEntry(indexEntry);
			return indexValue.getDataEntryIDs();
		}

		if (
				Long.class.isAssignableFrom(parameterType) || Integer.class.isAssignableFrom(parameterType) ||
				Short.class.isAssignableFrom(parameterType) || Byte.class.isAssignableFrom(parameterType) ||
				long.class.isAssignableFrom(parameterType) || int.class.isAssignableFrom(parameterType) ||
				short.class.isAssignableFrom(parameterType) || byte.class.isAssignableFrom(parameterType)
		)
		{
			IndexEntry indexEntry = IndexEntry.getIndexEntry(pm, fieldMeta, (Long) parameterValue);
			if (indexEntry == null)
				return Collections.emptySet();

			IndexValue indexValue = getQueryEvaluator().getEncryptionHandler().decryptIndexEntry(indexEntry);
			return indexValue.getDataEntryIDs();
		}

		if (
				Double.class.isAssignableFrom(parameterType) || Float.class.isAssignableFrom(parameterType) ||
				double.class.isAssignableFrom(parameterType) || float.class.isAssignableFrom(parameterType)
		)
		{
			IndexEntry indexEntry = IndexEntry.getIndexEntry(pm, fieldMeta, (Double) parameterValue);
			if (indexEntry == null)
				return Collections.emptySet();

			IndexValue indexValue = getQueryEvaluator().getEncryptionHandler().decryptIndexEntry(indexEntry);
			return indexValue.getDataEntryIDs();
		}

		throw new UnsupportedOperationException("Unsupported type: " + primaryExpr.getSymbol().getValueType());
	}
}
