package org.cumulus4j.store.test.compatibility.data;

import org.cumulus4j.store.test.collection.join.ElementAMap1QueryTest;
import org.cumulus4j.store.test.compatibility.CompatibilityTestData;

public class JoinElementAMap1 extends CompatibilityTestData {

	@Override
	public String getSinceVersion() {
		return VERSION_1_0_0;
	}

	@Override
	public void create() {
		ElementAMap1QueryTest test = new ElementAMap1QueryTest();
		test.setPersistenceManager(pm);
		test.createTestData();
	}

	@Override
	public void verify() {
		ElementAMap1QueryTest test = new ElementAMap1QueryTest();
		test.setPersistenceManager(pm);
		test.queryContainsKeyParameter();
		test.queryContainsKeyVariableAndIndexOfMatches();
		test.queryContainsValueParameter1();
		test.queryContainsValueParameter2();
		test.queryContainsValueVariableAndIndexOfMatches();
		test.queryNotContainsKeyParameter();
		test.queryContainsKeyVariableAndIndexOfNotMatches();
		test.queryContainsKeyVariableAndNotIndexOfMatches();
		test.queryNotContainsValueParameter1();
		test.queryNotContainsValueParameter2();
		test.queryContainsValueVariableAndNotIndexOfMatches();
		test.queryContainsValueVariableAndIndexOfNotMatches();
	}

}
