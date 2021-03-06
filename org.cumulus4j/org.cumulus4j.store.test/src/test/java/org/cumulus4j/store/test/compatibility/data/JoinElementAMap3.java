package org.cumulus4j.store.test.compatibility.data;

import org.cumulus4j.store.test.collection.join.ElementAMap3QueryTest;
import org.cumulus4j.store.test.compatibility.CompatibilityTestData;

public class JoinElementAMap3 extends CompatibilityTestData {

	@Override
	public String getSinceVersion() {
		return VERSION_1_0_0;
	}

	@Override
	public void create() {
		ElementAMap3QueryTest test = new ElementAMap3QueryTest();
		test.setPersistenceManager(pm);
		test.createTestData();
	}

	@Override
	public void verify() {
		ElementAMap3QueryTest test = new ElementAMap3QueryTest();
		test.setPersistenceManager(pm);
		test.queryContainsKeyParameter1();
		test.queryContainsKeyParameter2();
		test.queryNotContainsKeyParameter1();
		test.queryNotContainsKeyParameter2();
		test.queryContainsKeyVariableAndIndexOfMatches();
		test.queryContainsValueParameter1();
		test.queryContainsValueParameter2();
		test.queryContainsValueVariableAndIndexOfMatches();
		test.queryContainsKeyVariableAndIndexOfNotMatches();
		test.queryContainsKeyVariableAndNotIndexOfMatches();
		test.queryNotContainsValueParameter1();
		test.queryNotContainsValueParameter2();
		test.queryContainsValueVariableAndNotIndexOfMatches();
		test.queryContainsValueVariableAndIndexOfNotMatches();
	}

}
