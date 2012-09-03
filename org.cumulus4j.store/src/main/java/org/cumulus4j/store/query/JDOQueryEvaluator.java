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
package org.cumulus4j.store.query;

import java.util.Collection;
import java.util.Map;

import org.cumulus4j.store.PersistenceManagerConnection;
import org.cumulus4j.store.crypto.CryptoContext;
import org.datanucleus.ClassLoaderResolver;
import org.datanucleus.query.compiler.QueryCompilation;
import org.datanucleus.store.query.Query;

/**
 * JDO-specific sub-class of {@link QueryEvaluator} binding the JDO-agnostic query logic
 * to the JDO API.
 * @author Marco หงุ่ยตระกูล-Schulze - marco at nightlabs dot de
 */
public class JDOQueryEvaluator extends QueryEvaluator {

	public JDOQueryEvaluator(Query query, QueryCompilation compilation, Map<String, Object> parameterValues,
			ClassLoaderResolver clr, PersistenceManagerConnection pmConn, CryptoContext cryptoContext)
	{
		super("JDOQL", query, compilation, parameterValues, clr, pmConn, cryptoContext);
	}

	@Override
	protected Collection<Object> evaluateSubquery(
			Query subquery, QueryCompilation compilation, Object outerCandidate
	)
	{
		JDOQueryEvaluator evaluator = new JDOQueryEvaluator(getQuery(), compilation, getParameterValues(),
				getClassLoaderResolver(), getPersistenceManagerConnection(), getCryptoContext());
		return evaluator.execute();
	}
}
