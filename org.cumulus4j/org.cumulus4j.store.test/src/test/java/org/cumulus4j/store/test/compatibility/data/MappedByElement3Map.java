package org.cumulus4j.store.test.compatibility.data;

import org.cumulus4j.store.test.collection.mappedby.Element3MapQueryTest;
import org.cumulus4j.store.test.compatibility.CompatibilityTestData;

public class MappedByElement3Map extends CompatibilityTestData {

	@Override
	public String getSinceVersion() {
		return VERSION_1_0_0;
	}

	@Override
	public void create() {
		Element3MapQueryTest test = new Element3MapQueryTest();
		test.setPersistenceManager(pm);
		test.createTestData();
	}

	@Override
	public void verify() {
		Element3MapQueryTest test = new Element3MapQueryTest();
		test.setPersistenceManager(pm);
		test.queryContainsValueParameter();
		test.queryNotContainsValueParameter();
		test.queryContainsValueVariableAndIndexOfMatches();
		test.queryContainsKeyParameter();
		test.queryNotContainsKeyParameter();
		test.queryContainsKeyVariableAndIndexOfMatches();
	}
}
