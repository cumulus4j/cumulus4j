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
import org.datanucleus.query.expression.PrimaryExpression;
import org.datanucleus.query.symbol.Symbol;

/**
 * Evaluator representing {@link PrimaryExpression}s.
 *
 * @author Marco หงุ่ยตระกูล-Schulze - marco at nightlabs dot de
 * @see PrimaryExpression
 */
public class PrimaryExpressionEvaluator extends AbstractExpressionEvaluator<PrimaryExpression>
{
	public PrimaryExpressionEvaluator(QueryEvaluator queryEvaluator, AbstractExpressionEvaluator<?> parent, PrimaryExpression expression) {
		super(queryEvaluator, parent, expression);
	}

	@Override
	protected Set<Long> _queryResultDataEntryIDs(ResultDescriptor resultDescriptor)
	{
		throw new UnsupportedOperationException("It is impossible to get a result set for a PrimaryExpression alone!");
	}

	@Override
	protected Set<Symbol> _getResultSymbols() {
		if (getLeft() instanceof VariableExpressionEvaluator)
			return getLeft().getResultSymbols();

		Symbol symbol = getQueryEvaluator().getCompilation().getSymbolTable().getSymbol(getQueryEvaluator().getCandidateAlias());
		if (symbol == null)
			throw new IllegalStateException("getQueryEvaluator().getCompilation().getSymbolTable().getSymbol(getQueryEvaluator().getCandidateAlias()) returned null! getQueryEvaluator().getCandidateAlias()==\"" + getQueryEvaluator().getCandidateAlias() + "\"");

		return Collections.singleton(symbol);
	}
}
