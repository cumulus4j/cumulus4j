package org.cumulus4j.store.test.jpa;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;

import org.junit.runner.RunWith;

@RunWith(JPATransactionalRunner.class)
public abstract class AbstractJPATransactionalTest implements JPATransactionalTest {

	protected EntityManagerFactory emf;
	protected EntityManager em;

	public EntityManager getEntityManager() {
		return em;
	}

	public void setEntityManager(EntityManager entityManager) {
		this.em = entityManager;
		this.emf = em == null ? null : em.getEntityManagerFactory();
	}

	protected void commitAndBeginNewTransaction()
	{
		em.getTransaction().commit();

		// TODO BEGIN workaround for the em being closed :-(
		em.close();
		em = emf.createEntityManager();
		JPATransactionalRunner.setEncryptionCoordinates(em);
		// END workaround

		em.getTransaction().begin();
	}
}
