package org.cumulus4j.nightlabsprototype.query.filter;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import javax.jdo.PersistenceManager;
import javax.jdo.Query;

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
		PersistenceManager pm = getPersistenceManager();

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
						Query q = pm.newQuery(IndexEntryString.class);
						q.setFilter(
								"this.fieldMeta == :fieldMeta && " +
								"this.indexKeyString.indexOf(:invokeArgument) " + getOperatorAsJDOQLSymbol() + " :compareToArgument"
						);
						Map<String, Object> params = new HashMap<String, Object>(3);
						params.put("fieldMeta", primaryEval.getFieldMeta());
						params.put("invokeArgument", invokeArgument);
						params.put("compareToArgument", compareToArgument);

						@SuppressWarnings("unchecked")
						Collection<? extends IndexEntry> indexEntries = (Collection<? extends IndexEntry>) q.executeWithMap(params);

						Set<Long> result = new HashSet<Long>();
						for (IndexEntry indexEntry : indexEntries) {
							IndexValue indexValue = getQueryEvaluator().getEncryptionHandler().decryptIndexEntry(indexEntry);
							result.addAll(indexValue.getDataEntryIDs());
						}
						return result;
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
				return handleEquals(((PrimaryExpressionEvaluator)getLeft()).getFieldMeta(), compareToArgument);

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
				return handleEquals(((PrimaryExpressionEvaluator)getRight()).getFieldMeta(), compareToArgument);

			throw new UnsupportedOperationException("NYI"); // TODO other operators [<, >, <=, >=]!
		}

		throw new UnsupportedOperationException("NYI");
	}

	private Set<Long> handleEquals(FieldMeta fieldMeta, Object value)
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
			// TODO

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
