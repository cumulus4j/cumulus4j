package org.cumulus4j.store.test.compatibility.data;

import org.cumulus4j.store.test.collection.join.ElementAMap2QueryTest;
import org.cumulus4j.store.test.compatibility.CompatibilityTestData;

public class JoinElementAMap2 extends CompatibilityTestData {

	@Override
	public String getSinceVersion() {
		return VERSION_1_0_0;
	}

	@Override
	public void create() {
		ElementAMap2QueryTest test = new ElementAMap2QueryTest();
		test.setPersistenceManager(pm);
		test.createTestData();
	}

	@Override
	public void verify() {
		ElementAMap2QueryTest test = new ElementAMap2QueryTest();
		test.setPersistenceManager(pm);
		test.queryContainsValueParameter();
		test.queryNotContainsValueParameter();
		test.queryContainsValueVariableAndIndexOfMatches();
		test.queryContainsKeyParameter1();
		test.queryContainsKeyParameter2();
		test.queryNotContainsKeyParameter1();
		test.queryNotContainsKeyParameter2();
		test.queryContainsKeyVariableAndIndexOfMatches();
	}

}
