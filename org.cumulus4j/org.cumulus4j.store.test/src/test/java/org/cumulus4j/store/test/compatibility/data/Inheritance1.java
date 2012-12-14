package org.cumulus4j.store.test.compatibility.data;

import org.cumulus4j.store.test.compatibility.CompatibilityTestData;
import org.cumulus4j.store.test.inheritance.InheritanceTest;

public class Inheritance1 extends CompatibilityTestData {

	@Override
	public String getSinceVersion() {
		return VERSION_1_0_0;
	}

	@Override
	public void create() {
		InheritanceTest test = new InheritanceTest();
		test.setPersistenceManager(pm);
		test.persistSomeInstances();
	}

	@Override
	public void verify() {
		InheritanceTest test = new InheritanceTest();
		test.setPersistenceManager(pm);
		test.iterateExtent();
	}
}
