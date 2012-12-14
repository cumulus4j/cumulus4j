package org.cumulus4j.store.test.compatibility.data;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import org.cumulus4j.store.test.collection.mappedby.Element1;
import org.cumulus4j.store.test.collection.mappedby.Element1SetOwner;
import org.cumulus4j.store.test.collection.mappedby.Element1SetQueryTest;
import org.cumulus4j.store.test.compatibility.CompatibilityTestData;
import org.junit.Assert;

public class MappedByElement1Set extends CompatibilityTestData {

	@Override
	public String getSinceVersion() {
		return VERSION_1_0_0;
	}

	@Override
	public void create() {
		Element1SetQueryTest test = new Element1SetQueryTest();
		test.setPersistenceManager(pm);
		test.createTestData();
	}

	@Override
	public void verify() {
		@SuppressWarnings("unchecked")
		Collection<Element1SetOwner> c = (Collection<Element1SetOwner>) pm.newQuery(Element1SetOwner.class).execute();
		Assert.assertTrue("c.size() == " + c.size() + " < 5", c.size() >= 5); // There might be more because of other CompatibilityTestData classes => tolerant check!

		Set<String> expectedOwnerNames = new HashSet<String>();
		expectedOwnerNames.add("Owner 1");
		expectedOwnerNames.add("Owner 2");
		expectedOwnerNames.add("Owner 3");
		expectedOwnerNames.add("Owner 4");
		expectedOwnerNames.add("Owner 5");

		Set<String> expectedElementNames = new HashSet<String>();
		expectedElementNames.add("Element 1.1");
		expectedElementNames.add("Element 1.2");
		expectedElementNames.add("Element 1.3");
		expectedElementNames.add("Element 1.4");
		expectedElementNames.add("Element 2.1");
		expectedElementNames.add("Element 2.2");
		expectedElementNames.add("Element 2.3");
		expectedElementNames.add("Element 2.4");
		expectedElementNames.add("Element 3.1");
		expectedElementNames.add("Element 3.2");
		expectedElementNames.add("Element 3.3");
		expectedElementNames.add("Element 4.3");

		for (Element1SetOwner owner : c) {
			expectedOwnerNames.remove(owner.getName());
			for (Element1 element : owner.getSet()) {
				expectedElementNames.remove(element.getName());
			}
		}

		Assert.assertTrue("Not all expected owners found! Not found: " + expectedOwnerNames, expectedOwnerNames.isEmpty());
		Assert.assertTrue("Not all expected elements found! Not found: " + expectedElementNames, expectedElementNames.isEmpty());

		Element1SetQueryTest test = new Element1SetQueryTest();
		test.setPersistenceManager(pm);
		test.queryContainsParameter();
		test.queryContainsVariableAndVariableIndexOf();
		test.queryContainsVariableAndVariableEquals();
		test.queryNotContainsParameter();
		test.queryContainsVariableAndVariableNotIndexOf();
		test.queryContainsVariableAndNotVariableIndexOf();
		test.queryContainsVariableAndNotVariableEquals1();
		test.queryContainsVariableAndNotVariableEquals2();
		test.queryNotContainsVariableAndVariableEquals();
		test.queryNotContainsVariableAndVariableIndexOf();
	}
}
