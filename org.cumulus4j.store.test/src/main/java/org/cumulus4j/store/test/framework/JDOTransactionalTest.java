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
package org.cumulus4j.store.test.framework;

import javax.jdo.PersistenceManager;

public interface JDOTransactionalTest
{
	class State {
		private static ThreadLocal<Integer> testRunIndexThreadLocal = new ThreadLocal<Integer>();

		public static int getTestRunIndex() {
			Integer testRunIndex = testRunIndexThreadLocal.get();
			if (testRunIndex == null)
				throw new IllegalStateException("testRunIndex == null");
			return testRunIndex;
		}

		public static void setTestRunIndex(int testRunIndex) {
			testRunIndexThreadLocal.set(testRunIndex);
		}
	}

	PersistenceManager getPersistenceManager();
	void setPersistenceManager(PersistenceManager persistenceManager);
}
