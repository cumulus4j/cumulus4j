package org.cumulus4j.benchmark;

import org.cumulus4j.benchmark.framework.IScenario;
import org.cumulus4j.benchmark.framework.TestCase;
import org.cumulus4j.benchmark.scenario.inheritance.InheritanceScenarioService;
import org.cumulus4j.benchmark.scenario.simpledatatype.PersonAllQueryableScenarioService;
import org.cumulus4j.benchmark.scenario.simpledatatype.PersonHalfQueryableScenarioService;
import org.junit.Test;

public class BenchmarkTest {

	private TestCase getTestCase(){

		TestCase testCase = new TestCase();

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

	@Test
	public void inheritance() throws Exception{

		TestCase testCase = getTestCase();

		testCase.setServiceName(InheritanceScenarioService.PATH);

		BenchmarkClient client = new BenchmarkClient();

		client.startInvocation(testCase);
	}

//	@Test
//	public void poleposition() throws Exception{
//
//		TestCase testCase = new TestCase();
//		testCase.addInvocation(PolePositionScenarioService.START_POLE_POSITION);
//		testCase.setServiceName(PolePositionScenarioService.PATH);
//
//		BenchmarkClient client = new BenchmarkClient();
//		client.startInvocation(testCase);
//
//	}
}