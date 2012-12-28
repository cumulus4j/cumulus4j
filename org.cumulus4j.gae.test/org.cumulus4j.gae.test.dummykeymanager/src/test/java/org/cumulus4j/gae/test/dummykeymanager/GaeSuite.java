package org.cumulus4j.gae.test.dummykeymanager;

import org.cumulus4j.store.test.framework.AbstractJDOTransactionalTestClearingDatabase;
import org.junit.internal.builders.AllDefaultPossibilitiesBuilder;
import org.junit.internal.builders.AnnotatedBuilder;
import org.junit.internal.builders.JUnit4Builder;
import org.junit.runner.Runner;
import org.junit.runners.Suite;
import org.junit.runners.model.InitializationError;
import org.junit.runners.model.RunnerBuilder;

public class GaeSuite extends Suite
{
	static {
		AbstractJDOTransactionalTestClearingDatabase.clearDatabaseEnabled = false;
	}

	public GaeSuite(Class<?> klass, RunnerBuilder builder) throws InitializationError
	{
		super(klass, new GaeRunnerBuilder(true));
	}

	public static class GaeRunnerBuilder extends AllDefaultPossibilitiesBuilder
	{
		public GaeRunnerBuilder(boolean canUseSuiteMethod)
		{
			super(canUseSuiteMethod);
		}

		@Override
		protected AnnotatedBuilder annotatedBuilder() {
			return new NullAnnotatedBuilder(this); // overwrite the annotations and force the GaeTransactionalRunner instead.
		}

		@Override
		protected JUnit4Builder junit4Builder()
		{
			return new GaeJUnit4Builder();
		}
	}

	public static class GaeJUnit4Builder extends JUnit4Builder
	{
		@Override
		public Runner runnerForClass(Class<?> testClass) throws Throwable
		{
			return new GaeTransactionalRunner(testClass);
		}
	}

	public static class NullAnnotatedBuilder extends AnnotatedBuilder
	{
		public NullAnnotatedBuilder(RunnerBuilder suiteBuilder) {
			super(suiteBuilder);
		}
		@Override
		public Runner runnerForClass(Class<?> testClass) throws Exception {
			return null;
		}
	}
}
