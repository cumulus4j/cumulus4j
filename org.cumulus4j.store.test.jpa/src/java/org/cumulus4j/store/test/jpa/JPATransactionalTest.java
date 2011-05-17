package org.cumulus4j.store.test.jpa;

import javax.persistence.EntityManager;

public interface JPATransactionalTest
{
	EntityManager getEntityManager();
	void setEntityManager(EntityManager entityManager);
}
