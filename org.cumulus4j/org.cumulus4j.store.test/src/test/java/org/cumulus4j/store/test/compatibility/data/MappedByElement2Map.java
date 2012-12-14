package org.cumulus4j.store.test.compatibility.data;

import org.cumulus4j.store.test.collection.mappedby.Element2MapQueryTest;
import org.cumulus4j.store.test.compatibility.CompatibilityTestData;

public class MappedByElement2Map extends CompatibilityTestData {

	@Override
	public String getSinceVersion() {
		return VERSION_1_0_0;
	}

	@Override
	public void create() {
		Element2MapQueryTest test = new Element2MapQueryTest();
		test.setPersistenceManager(pm);
		test.createTestData();
	}

	@Override
	public void verify() {
		Element2MapQueryTest test = new Element2MapQueryTest();
		test.setPersistenceManager(pm);
		test.queryContainsKeyParameter();
		test.queryContainsKeyVariableAndIndexOfMatches();
		test.queryContainsValueParameter();
		test.queryContainsValueVariableAndIndexOfMatches();
		test.queryNotContainsKeyParameter();
		test.queryContainsKeyVariableAndIndexOfNotMatches();
		test.queryContainsKeyVariableAndNotIndexOfMatches();
		test.queryNotContainsValueParameter();
		test.queryContainsValueVariableAndNotIndexOfMatches();
		test.queryContainsValueVariableAndIndexOfNotMatches();
	}
}
