package org.cumulus4j.benchmark;

import org.cumulus4j.benchmark.framework.IScenario;
import org.cumulus4j.benchmark.framework.TestCase;
import org.cumulus4j.benchmark.simpledatatypescenario.PersonAllQueryableScenarioService;
import org.cumulus4j.benchmark.simpledatatypescenario.PersonHalfQueryableScenarioService;
import org.junit.Test;

public class BenchmarkTest {

	public TestCase getTestCase(){

		TestCase testCase = new TestCase();

		testCase.setServiceName(PersonAllQueryableScenarioService.PATH);

		testCase.addInvocation(IScenario.BULK_STORE_OBJECTS);
		testCase.addInvocation(IScenario.BULK_STORE_OBJECTS);
		testCase.addInvocation(IScenario.BULK_STORE_OBJECTS);

		testCase.addInvocation(IScenario.BULK_LOAD_OBJECTS);

		testCase.addInvocation(IScenario.STORE_SINGLE_OBJECT);
		testCase.addInvocation(IScenario.STORE_SINGLE_OBJECT);
		testCase.addInvocation(IScenario.STORE_SINGLE_OBJECT);
		testCase.addInvocation(IScenario.STORE_SINGLE_OBJECT);
		testCase.addInvocation(IScenario.STORE_SINGLE_OBJECT);

		testCase.addInvocation(IScenario.LOAD_SINGLE_OBJECT);

		testCase.addInvocation(IScenario.LOAD_ALL_OBJECTS);

		return testCase;
	}

	@Test
	public void personAllQueryable() throws Exception{

		TestCase testCase = getTestCase();

		testCase.setServiceName(PersonAllQueryableScenarioService.PATH);

		BenchmarkClient client = new BenchmarkClient();

		client.startInvocation(testCase);
	}

	@Test
	public void personHalfQueryable() throws Exception{

		TestCase testCase = getTestCase();

		testCase.setServiceName(PersonHalfQueryableScenarioService.PATH);

		BenchmarkClient client = new BenchmarkClient();

		client.startInvocation(testCase);
	}
}