package org.cumulus4j.store.test.compatibility.data;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import org.cumulus4j.store.test.collection.join.ElementA;
import org.cumulus4j.store.test.collection.join.ElementASetOwner;
import org.cumulus4j.store.test.collection.join.ElementASetQueryTest;
import org.cumulus4j.store.test.compatibility.CompatibilityTestData;
import org.junit.Assert;

public class JoinElementASet extends CompatibilityTestData {

	@Override
	public String getSinceVersion() {
		return VERSION_1_0_0;
	}

	@Override
	public void create() {
		ElementASetQueryTest test = new ElementASetQueryTest();
		test.setPersistenceManager(pm);
		test.createTestData();
	}

	@Override
	public void verify() {
		@SuppressWarnings("unchecked")
		Collection<ElementASetOwner> c = (Collection<ElementASetOwner>) pm.newQuery(ElementASetOwner.class).execute();
		Assert.assertEquals(5, c.size());

		Set<String> expectedOwnerNames = new HashSet<String>();
		expectedOwnerNames.add("Owner 1");
		expectedOwnerNames.add("Owner 2");
		expectedOwnerNames.add("Owner 3");
		expectedOwnerNames.add("Owner 4");
		expectedOwnerNames.add("Owner 5");

		Set<String> expectedElementNames = new HashSet<String>();
		expectedElementNames.add("S.Element 1.1");
		expectedElementNames.add("S.Element 1.2");
		expectedElementNames.add("S.Element 1.3");
		expectedElementNames.add("S.Element 1.4");
		expectedElementNames.add("S.Element 2.1");
		expectedElementNames.add("S.Element 2.2");
		expectedElementNames.add("S.Element 2.3");
		expectedElementNames.add("S.Element 2.4");
		expectedElementNames.add("S.Element 3.1");
		expectedElementNames.add("S.Element 3.2");
		expectedElementNames.add("S.Element 3.3");
		expectedElementNames.add("S.Element 4.3");

		for (ElementASetOwner owner : c) {
			expectedOwnerNames.remove(owner.getName());
			for (ElementA element : owner.getSet()) {
				expectedElementNames.remove(element.getName());
			}
		}

		Assert.assertTrue("Not all expected owners found! Not found: " + expectedOwnerNames, expectedOwnerNames.isEmpty());
		Assert.assertTrue("Not all expected elements found! Not found: " + expectedElementNames, expectedElementNames.isEmpty());

		ElementASetQueryTest test = new ElementASetQueryTest();
		test.setPersistenceManager(pm);
		test.queryIsEmpty();
		test.querySize();
		test.queryContainsParameter();
		test.queryContainsVariableAndVariableIndexOf();
		test.queryContainsVariableAndVariableEquals();
		test.queryNotContainsParameter();
		test.queryContainsVariableAndVariableNotIndexOf();
		test.queryContainsVariableAndNotVariableIndexOf();
		test.queryContainsVariableAndNotVariableEquals4();
		test.queryContainsVariableAndNotVariableEquals2();
		test.queryNotContainsVariableAndVariableEquals();
		test.queryNotContainsVariableAndVariableIndexOf();
		test.queryCollectionParameterContainsField();
	}

}
