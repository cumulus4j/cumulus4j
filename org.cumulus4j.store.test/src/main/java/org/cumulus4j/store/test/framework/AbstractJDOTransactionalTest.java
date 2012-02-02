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
import javax.jdo.PersistenceManagerFactory;

import org.junit.runner.RunWith;

@RunWith(JDOTransactionalRunner.class)
public abstract class AbstractJDOTransactionalTest implements JDOTransactionalTest {

	protected PersistenceManagerFactory pmf;
	protected PersistenceManager pm;

	@Override
	public PersistenceManager getPersistenceManager() {
		return pm;
	}

	@Override
	public void setPersistenceManager(PersistenceManager persistenceManager) {
		this.pm = persistenceManager;
		this.pmf = pm == null ? null : pm.getPersistenceManagerFactory();
	}

	protected void commitAndBeginNewTransaction()
	{
		if (pm.currentTransaction().isActive())
			pm.currentTransaction().commit();

		// TODO BEGIN workaround for the pm being closed :-(
		pm.close();
		pm = pmf.getPersistenceManager();
		JDOTransactionalRunner.setEncryptionCoordinates(pm);
		// END workaround

		pm.currentTransaction().begin();
	}
}
