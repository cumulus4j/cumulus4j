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

import java.util.Set;

import org.cumulus4j.store.query.QueryEvaluator;
import org.datanucleus.query.QueryUtils;
import org.datanucleus.query.expression.ParameterExpression;

/**
 * Evaluator representing query parameters.
 *
 * @author Marco หงุ่ยตระกูล-Schulze - marco at nightlabs dot de
 * @see ParameterExpression
 */
public class ParameterExpressionEvaluator
extends AbstractExpressionEvaluator<ParameterExpression>
{
	public ParameterExpressionEvaluator(QueryEvaluator queryEvaluator, AbstractExpressionEvaluator<?> parent, ParameterExpression expression) {
		super(queryEvaluator, parent, expression);
	}

	@Override
	protected Set<Long> _queryResultDataEntryIDs(ResultDescriptor resultDescriptor)
	{
		throw new UnsupportedOperationException("It is impossible to get a result set for a ParameterExpression alone!");
	}

	public Object getParameterValue()
	{
		return QueryUtils.getValueForParameterExpression(getQueryEvaluator().getParameterValues(), getExpression());
	}
}
