package org.cumulus4j.test.framework;

import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import javax.jdo.JDOHelper;
import javax.jdo.PersistenceManager;
import javax.jdo.PersistenceManagerFactory;

import org.cumulus4j.api.crypto.CryptoManager;
import org.cumulus4j.api.crypto.CryptoSession;
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

public class TransactionalRunner extends BlockJUnit4ClassRunner
{
	static {
		TestUtil.configureLoggingOnce();
	}

	private static final Logger logger = LoggerFactory.getLogger(TransactionalRunner.class);

	private PersistenceManagerFactory pmf;

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
			if (pmf != null)
				pmf.close();
		}
	}

	public TransactionalRunner(Class<?> testClass) throws InitializationError {
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

	public static void setEncryptionCoordinates(PersistenceManager pm)
	{
		pm.setProperty(CryptoManager.PROPERTY_CRYPTO_MANAGER_ID, "dummy");
		pm.setProperty(CryptoSession.PROPERTY_CRYPTO_SESSION_ID, UUID.randomUUID().toString());
	}

	private void runInTransaction(Object test, Statement statement)
	throws Throwable
	{
		PersistenceManager pm = null;
		TransactionalTest transactionalTest = null;
		if (test instanceof TransactionalTest) {
			transactionalTest = (TransactionalTest) test;

			if (pmf == null) {
				logger.info("run: Setting up PersistenceManagerFactory.");
				pmf = JDOHelper.getPersistenceManagerFactory(TestUtil.loadProperties("cumulus4j-test-datanucleus.properties"));
			}

			pm = pmf.getPersistenceManager();
			setEncryptionCoordinates(pm);
			transactionalTest.setPersistenceManager(pm);
			pm.currentTransaction().begin();
		}
		try {
			statement.evaluate();

			pm = transactionalTest.getPersistenceManager();
			if (pm != null && !pm.isClosed() && pm.currentTransaction().isActive())
				pm.currentTransaction().commit();
		} finally {
			pm = transactionalTest.getPersistenceManager();
			if (pm != null && !pm.isClosed()) {
				try {
					if (pm.currentTransaction().isActive())
						pm.currentTransaction().rollback();

					pm.close();
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
