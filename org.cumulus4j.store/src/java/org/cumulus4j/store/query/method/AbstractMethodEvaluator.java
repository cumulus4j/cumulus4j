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
package org.cumulus4j.store.query.method;

/**
 * Abstract base for all method evaluators.
 */
public abstract class AbstractMethodEvaluator implements MethodEvaluator {
	protected Object compareToArgument = null;

	/* (non-Javadoc)
	 * @see org.cumulus4j.store.query.method.MethodEvaluator#setCompareToArgument(java.lang.Object)
	 */
	@Override
	public void setCompareToArgument(Object obj) {
		this.compareToArgument = obj;
	}

	/* (non-Javadoc)
	 * @see org.cumulus4j.store.query.method.MethodEvaluator#requiresComparisonArgument()
	 */
	@Override
	public boolean requiresComparisonArgument() {
		return false;
	}
}