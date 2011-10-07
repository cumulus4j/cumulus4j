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
import org.datanucleus.query.expression.SubqueryExpression;

/**
 * Evaluator handling a <a target="_blank" href="http://www.datanucleus.org/products/accessplatform_3_0/jdo/jdoql_subquery.html">sub-query</a>.
 * @author Andy Jefferson
 * @author Marco หงุ่ยตระกูล-Schulze - marco at nightlabs dot de (only added javadoc)
 */
public class SubqueryExpressionEvaluator
extends AbstractExpressionEvaluator<SubqueryExpression>
{
	/**
	 * Create an instance.
	 * @param queryEvaluator the evaluator responsible for the evaluation of the whole query.
	 * @param parent the parent in the tree structure or <code>null</code>, if this is the root.
	 * @param expression the expression to evaluate.
	 */
	public SubqueryExpressionEvaluator(QueryEvaluator queryEvaluator, AbstractExpressionEvaluator<?> parent, SubqueryExpression expression)
	{
		super(queryEvaluator, parent, expression);
	}

	@Override
	protected Set<Long> _queryResultDataEntryIDs(ResultDescriptor resultDescriptor)
	throws UnsupportedOperationException
	{
		throw new UnsupportedOperationException("NYI");
	}

}
