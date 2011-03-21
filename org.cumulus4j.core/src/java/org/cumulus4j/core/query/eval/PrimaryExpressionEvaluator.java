package org.cumulus4j.core.query.eval;

import java.util.List;
import java.util.Set;

import org.cumulus4j.core.model.ClassMeta;
import org.cumulus4j.core.model.FieldMeta;
import org.cumulus4j.core.query.QueryEvaluator;
import org.datanucleus.query.expression.PrimaryExpression;
import org.datanucleus.query.symbol.Symbol;

public class PrimaryExpressionEvaluator extends AbstractExpressionEvaluator<PrimaryExpression>
{
	public PrimaryExpressionEvaluator(QueryEvaluator queryEvaluator, AbstractExpressionEvaluator<?> parent, PrimaryExpression expression) {
		super(queryEvaluator, parent, expression);
	}

	@Override
	protected Set<Long> _queryResultDataEntryIDs() {
		throw new UnsupportedOperationException("It is impossible to get a result set for a PrimaryExpression alone!");
	}

	/**
	 * Get the {@link FieldMeta}, if the {@link PrimaryExpression} represents a field or <code>null</code>,
	 * if the {@link PrimaryExpression} represents sth. else.
	 *
	 * @return the {@link FieldMeta} or <code>null</code>.
	 */
	public FieldMeta getFieldMeta()
	{
		// TODO find out, when to return null, i.e. when this PrimaryExpression does not reference a field!!!

		List<String> tuples = getExpression().getTuples();
		if (tuples.size() == 2) {
			Symbol symbol = getQueryEvaluator().getCompilation().getSymbolTable().getSymbol(tuples.get(0));
			if (symbol == null)
				throw new IllegalStateException("getQueryEvaluator().getCompilation().getSymbolTable().getSymbol(tuples.get(0)) returned null! tuples.get(0)=" + tuples.get(0));

			Class<?> clazz = symbol.getValueType();
			ClassMeta classMeta = getQueryEvaluator().getStoreManager().getClassMeta(getQueryEvaluator().getExecutionContext(), clazz);
			FieldMeta fieldMeta = classMeta.getFieldMeta(null, tuples.get(1));
			return fieldMeta;
		}
		else
			throw new IllegalStateException("getExpression().getTuples().size() == " + getExpression().getTuples().size() + " != 2");
	}
}
