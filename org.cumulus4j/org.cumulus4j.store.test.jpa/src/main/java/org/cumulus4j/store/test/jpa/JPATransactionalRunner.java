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

import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import java.util.UUID;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;

import org.cumulus4j.store.crypto.CryptoManager;
import org.cumulus4j.store.crypto.CryptoSession;
import org.junit.After;
import org.junit.Before;
import org.junit.internal.runners.model.MultipleFailureException;
import org.junit.runner.notification.RunNotifier;
import org.junit.runners.BlockJUnit4ClassRunner;
import org.junit.runners.model.FrameworkMethod;
import org.junit.runners.model.InitializationError;
import org.junit.runners.model.Statement;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class JPATransactionalRunner extends BlockJUnit4ClassRunner
{
    static {
        TestUtil.configureLoggingOnce();
    }

	private static final Logger logger = LoggerFactory.getLogger(JPATransactionalRunner.class);

	private EntityManagerFactory emf;

	@Override
	public void run(RunNotifier notifier) {
		logger.info("run: Shutting down Derby (in case it was used before).");
		// First shut down derby, in case it is open
		try {
			DriverManager.getConnection("jdbc:derby:;shutdown=true");
		} catch (SQLException x) {
			// ignore, because this is to be expected according to http://db.apache.org/derby/docs/dev/devguide/tdevdvlp40464.html
			doNothing(); // Remove warning from PMD report: http://cumulus4j.org/pmd.html
		}

//		logger.info("run: ************ children begin ************");
//		List<FrameworkMethod> children = getChildren();
//		for (FrameworkMethod child : children) {
//			logger.info("run: " + child.getName());
//		}
//		logger.info("run: ************ children end ************");

//		logger.info("run: Setting up PersistenceManagerFactory.");
//		pmf = JDOHelper.getPersistenceManagerFactory(TestUtil.loadProperties("cumulus4j-test-datanucleus.properties"));
		try {
			super.run(notifier);
		} finally {
			logger.info("run: Shutting down PersistenceManagerFactory.");
			if (emf != null)
				emf.close();
		}
	}

	public JPATransactionalRunner(Class<?> testClass) throws InitializationError {
		super(testClass);
	}

	@Override
	protected Statement methodInvoker(FrameworkMethod method, Object test)
	{
		Statement superMethodInvoker = super.methodInvoker(method, test);
		return new TransactionalInvokeMethod(method, test, superMethodInvoker);
	}

	private class TxRunBefores extends Statement {
		private final Statement fNext;

		private final Object fTarget;

		private final List<FrameworkMethod> fBefores;

		public TxRunBefores(Statement next, List<FrameworkMethod> befores, Object target) {
			fNext= next;
			fBefores= befores;
			fTarget= target;
		}

		@Override
		public void evaluate() throws Throwable {
			for (FrameworkMethod before : fBefores)
				runInTransaction(fTarget, before);
			fNext.evaluate();
		}
	}

	private class TxRunAfters extends Statement {
		private final Statement fNext;

		private final Object fTarget;

		private final List<FrameworkMethod> fAfters;

		public TxRunAfters(Statement next, List<FrameworkMethod> afters, Object target) {
			fNext= next;
			fAfters= afters;
			fTarget= target;
		}

		@Override
		public void evaluate() throws Throwable {
			List<Throwable> errors = new ArrayList<Throwable>();
			errors.clear();
			try {
				fNext.evaluate();
			} catch (Throwable e) {
				errors.add(e);
			} finally {
				for (FrameworkMethod each : fAfters)
					try {
						runInTransaction(fTarget, each);
					} catch (Throwable e) {
						errors.add(e);
					}
			}
			MultipleFailureException.assertEmpty(errors);
		}
	}

	@Override
	protected Statement withBefores(FrameworkMethod method, Object target, Statement statement) {
		List<FrameworkMethod> befores = getTestClass().getAnnotatedMethods(Before.class);
		return befores.isEmpty() ? statement : new TxRunBefores(statement, befores, target);
	}

	@Override
	protected Statement withAfters(FrameworkMethod method, Object target, Statement statement) {
		List<FrameworkMethod> afters= getTestClass().getAnnotatedMethods(After.class);
		return afters.isEmpty() ? statement : new TxRunAfters(statement, afters, target);
	}

//	@Override
//	protected List<MethodRule> rules(Object test) {
//		List<MethodRule> superRules = super.rules(test);
//		List<MethodRule> result = new ArrayList<MethodRule>(superRules.size() + 1);
//		result.addAll(superRules);
//		result.add(transactionalRule);
//		return result;
//	}
//
//	private MethodRule transactionalRule = new MethodRule() {
//		@Override
//		public Statement apply(Statement base, FrameworkMethod method, Object target) {
//			return new TransactionalInvokeMethod(method, target, base);
//		}
//	};

	private void runInTransaction(final Object test, final FrameworkMethod method)
	throws Throwable
	{
		runInTransaction(test, new Statement() {
			@Override
			public void evaluate() throws Throwable {
				method.invokeExplosively(test);
			}
		});
	}

	public static void setEncryptionCoordinates(EntityManager em)
	{
		em.setProperty(CryptoManager.PROPERTY_CRYPTO_MANAGER_ID, "dummy");
		String keyStoreID = "dummy";
		em.setProperty(CryptoSession.PROPERTY_CRYPTO_SESSION_ID, keyStoreID + '_' + UUID.randomUUID() + '*' + UUID.randomUUID());
	}

	private static Properties loadProperties()
	{
		Properties props = TestUtil.loadProperties("cumulus4j-test-datanucleus.properties");
		// Convert JDO-standard connection props to JPA-standard connection props
		if (props.containsKey("javax.jdo.PersistenceManagerFactoryClass")) {
			props.remove("javax.jdo.PersistenceManagerFactoryClass");
		}
		if (props.containsKey("javax.jdo.option.ConnectionDriverName")) {
			String val = props.getProperty("javax.jdo.option.ConnectionDriverName");
			props.remove("javax.jdo.option.ConnectionDriverName");
			props.setProperty("javax.persistence.jdbc.driver", val);
		}
		if (props.containsKey("javax.jdo.option.ConnectionURL")) {
			String val = props.getProperty("javax.jdo.option.ConnectionURL");
			props.remove("javax.jdo.option.ConnectionURL");
			props.setProperty("javax.persistence.jdbc.url", val);
		}
		if (props.containsKey("javax.jdo.option.ConnectionUserName")) {
			String val = props.getProperty("javax.jdo.option.ConnectionUserName");
			props.remove("javax.jdo.option.ConnectionUserName");
			props.setProperty("javax.persistence.jdbc.user", val);
		}
		if (props.containsKey("javax.jdo.option.ConnectionPassword")) {
			String val = props.getProperty("javax.jdo.option.ConnectionPassword");
			props.remove("javax.jdo.option.ConnectionPassword");
			props.setProperty("javax.persistence.jdbc.password", val);
		}
		return props;
	}

	public static EntityManagerFactory createEntityManagerFactory()
	{
		Properties props = loadProperties();
		return Persistence.createEntityManagerFactory("test", props);
	}

	private void runInTransaction(Object test, Statement statement)
	throws Throwable
	{
		EntityManager em = null;
		JPATransactionalTest transactionalTest = null;
		if (test instanceof JPATransactionalTest) {
			transactionalTest = (JPATransactionalTest) test;

			if (emf == null) {
				logger.info("run: Setting up EntityManagerFactory.");
				emf = createEntityManagerFactory();
			}

			em = emf.createEntityManager();
			setEncryptionCoordinates(em);
			transactionalTest.setEntityManager(em);
			em.getTransaction().begin();
		}
		try {
			statement.evaluate();

			em = transactionalTest.getEntityManager();
			if (em != null && em.isOpen() && em.getTransaction().isActive())
				em.getTransaction().commit();
		} finally {
			em = transactionalTest.getEntityManager();
			if (em != null && em.isOpen()) {
				try {
					if (em.getTransaction().isActive())
						em.getTransaction().rollback();

					em.close();
				} catch (Throwable t) {
					logger.warn("Rolling back or closing PM failed: " + t, t);
				}
			}
		}
	}

	private class TransactionalInvokeMethod extends Statement
	{
		@SuppressWarnings("unused")
		private FrameworkMethod method;
		private Object test;
		private Statement delegate;

		public TransactionalInvokeMethod(FrameworkMethod method, Object test, Statement delegate) {
			this.method = method;
			this.test = test;
			this.delegate = delegate;
		}

		@Override
		public void evaluate() throws Throwable {
			runInTransaction(test, delegate);

//			PersistenceManager pm = null;
//			TransactionalTest transactionalTest = null;
//			if (test instanceof TransactionalTest) {
//				transactionalTest = (TransactionalTest) test;
//
//				if (pmf == null) {
//					logger.info("run: Setting up PersistenceManagerFactory.");
//					pmf = JDOHelper.getPersistenceManagerFactory(TestUtil.loadProperties("cumulus4j-test-datanucleus.properties"));
//				}
//
//				pm = pmf.getPersistenceManager();
//				transactionalTest.setPersistenceManager(pm);
//				pm.currentTransaction().begin();
//			}
//			try {
//				delegate.evaluate();
//
//				pm = transactionalTest.getPersistenceManager();
//				if (pm != null && !pm.isClosed() && pm.currentTransaction().isActive())
//					pm.currentTransaction().commit();
//			} finally {
//				pm = transactionalTest.getPersistenceManager();
//				if (pm != null && !pm.isClosed()) {
//					try {
//						if (pm.currentTransaction().isActive())
//							pm.currentTransaction().rollback();
//
//						pm.close();
//					} catch (Throwable t) {
//						logger.warn("Rolling back or closing PM failed: " + t, t);
//					}
//				}
//			}
		}
	}

	private static final void doNothing() { }
}
