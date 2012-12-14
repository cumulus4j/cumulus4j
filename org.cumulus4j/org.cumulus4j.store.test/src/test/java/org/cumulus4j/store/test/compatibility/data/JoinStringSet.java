package org.cumulus4j.store.test.compatibility.data;

import org.cumulus4j.store.test.collection.join.StringSetQueryTest;
import org.cumulus4j.store.test.compatibility.CompatibilityTestData;

public class JoinStringSet extends CompatibilityTestData {

	@Override
	public String getSinceVersion() {
		return VERSION_1_0_0;
	}

	@Override
	public void create() {
		StringSetQueryTest test = new StringSetQueryTest();
		test.setPersistenceManager(pm);
		test.createTestData();
	}

	@Override
	public void verify() {
		StringSetQueryTest test = new StringSetQueryTest();
		test.setPersistenceManager(pm);
		test.queryContainsParameter();
		test.queryContainsVariableAndVariableIndexOf();
		test.queryContainsVariableAndVariableEquals();
		test.queryNotContainsParameter();
		test.queryContainsVariableAndVariableNotIndexOf();
		test.queryContainsVariableAndNotVariableIndexOf();
		test.queryContainsVariableAndVariableNotEquals();
		test.queryContainsVariableAndNotVariableEquals();
		test.queryNotContainsVariableAndVariableEquals();
		test.queryIsEmpty();
	}

}
