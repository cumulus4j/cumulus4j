package org.cumulus4j.store.test.compatibility.data;

import org.cumulus4j.store.test.collection.join.StringStringMapQueryTest;
import org.cumulus4j.store.test.compatibility.CompatibilityTestData;

public class JoinStringStringMap extends CompatibilityTestData {

	@Override
	public String getSinceVersion() {
		return VERSION_1_0_0;
	}

	@Override
	public void create() {
		StringStringMapQueryTest test = new StringStringMapQueryTest();
		test.setPersistenceManager(pm);
		test.createTestData();
	}

	@Override
	public void verify() {
		StringStringMapQueryTest test = new StringStringMapQueryTest();
		test.setPersistenceManager(pm);
		test.queryContainsKeyParameter();
		test.queryNotContainsKeyParameter();
		test.queryContainsValueParameter();
		test.queryNotContainsValueParameter();
		test.queryContainsKeyVariableAndVariableIndexOf();
		test.queryNotContainsKeyVariableAndVariableIndexOf();
		test.queryContainsKeyVariableAndVariableNotIndexOf();
		test.queryContainsKeyVariableAndNotVariableIndexOf();
		test.queryContainsValueVariableAndVariableIndexOf();
		test.queryNotContainsValueVariableAndVariableIndexOf();
		test.queryContainsValueVariableAndVariableNotIndexOf();
		test.queryContainsValueVariableAndNotVariableIndexOf();
		test.queryContainsKeyVariableAndVariableEquals();
		test.queryContainsKeyVariableAndVariableNotEquals();
		test.queryContainsKeyVariableAndNotVariableEquals();
		test.queryContainsValueVariableAndVariableEquals();
		test.queryContainsValueVariableAndVariableNotEquals();
		test.queryContainsValueVariableAndNotVariableEquals();
	}

}
