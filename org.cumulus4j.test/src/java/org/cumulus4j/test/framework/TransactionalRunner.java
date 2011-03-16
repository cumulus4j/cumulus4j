package org.cumulus4j.test.framework;

import javax.jdo.JDOHelper;
import javax.jdo.PersistenceManager;
import javax.jdo.PersistenceManagerFactory;

import org.junit.runner.notification.Failure;
import org.junit.runner.notification.RunNotifier;
import org.junit.runners.BlockJUnit4ClassRunner;
import org.junit.runners.model.FrameworkMethod;
import org.junit.runners.model.InitializationError;
import org.junit.runners.model.Statement;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class TransactionalRunner extends BlockJUnit4ClassRunner
{
	private static final Logger logger = LoggerFactory.getLogger(TransactionalRunner.class);

	private PersistenceManagerFactory pmf;

	@Override
	public void run(RunNotifier notifier) {
		logger.info("run: Clearing database (dropping all tables).");
		try {
			CleanupUtil.dropAllTables();
		} catch (Exception e) {
			logger.error("Dropping all tables failed!", e);
			notifier.fireTestFailure(new Failure(getDescription(), e));
			return;
		}

		logger.info("run: Setting up PersistenceManagerFactory.");
		pmf = JDOHelper.getPersistenceManagerFactory(TestUtil.loadProperties("cumulus4j-test-datanucleus.properties"));
		try {
			super.run(notifier);
		} finally {
			logger.info("run: Shutting down PersistenceManagerFactory.");
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
			PersistenceManager pm = null;
			TransactionalTest transactionalTest = null;
			if (test instanceof TransactionalTest) {
				transactionalTest = (TransactionalTest) test;
				pm = pmf.getPersistenceManager();
				transactionalTest.setPersistenceManager(pm);
				pm.currentTransaction().begin();
			}
			try {
				delegate.evaluate();

				if (pm != null)
					pm.currentTransaction().commit();
			} finally {
				if (pm != null) {
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
	}
}
