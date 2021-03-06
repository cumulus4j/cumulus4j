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
package org.cumulus4j.store.test.jpa;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;

import org.junit.runner.RunWith;

@RunWith(JPATransactionalRunner.class)
public abstract class AbstractJPATransactionalTest implements JPATransactionalTest {

	protected EntityManagerFactory emf;
	protected EntityManager em;

	@Override
	public EntityManager getEntityManager() {
		return em;
	}

	@Override
	public void setEntityManager(EntityManager entityManager) {
		this.em = entityManager;
		this.emf = em == null ? null : em.getEntityManagerFactory();
	}

	protected void commitAndBeginNewTransaction()
	{
		if (em.getTransaction().isActive())
			em.getTransaction().commit();

		// TODO BEGIN workaround for the em being closed :-(
		em.close();
		em = emf.createEntityManager();
		JPATransactionalRunner.setEncryptionCoordinates(em);
		// END workaround

		em.getTransaction().begin();
	}
}
