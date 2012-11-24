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

import java.util.Collections;
import java.util.Set;

import org.cumulus4j.store.query.QueryEvaluator;
import org.datanucleus.query.expression.VariableExpression;
import org.datanucleus.query.symbol.Symbol;

/**
 * Evaluator representing variables.
 *
 * @author Marco หงุ่ยตระกูล-Schulze - marco at nightlabs dot de
 */
public class VariableExpressionEvaluator extends AbstractExpressionEvaluator<VariableExpression>
{
	public VariableExpressionEvaluator(QueryEvaluator queryEvaluator, AbstractExpressionEvaluator<?> parent, VariableExpression expression)
	{
		super(queryEvaluator, parent, expression);
	}

	@Override
	protected Set<Long> _queryResultDataEntryIDs(ResultDescriptor resultDescriptor)
	{
		throw new UnsupportedOperationException("Cannot evaluate a variable without any context!");
	}

	@Override
	protected Set<Symbol> _getResultSymbols()
	{
		Symbol symbol = getExpression().getSymbol();
		if (symbol == null)
			throw new IllegalStateException("getExpression().getSymbol() returned null!");

		return Collections.singleton(symbol);
	}
}
