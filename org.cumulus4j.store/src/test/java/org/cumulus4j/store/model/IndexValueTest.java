package org.cumulus4j.store.model;

import junit.framework.Assert;

import org.junit.Test;

public class IndexValueTest {

	@Test
	public void simpleWriteRead() {
		IndexValue indexValue1 = new IndexValue();
		indexValue1.addDataEntryID(1);
		indexValue1.addDataEntryID(25);
		indexValue1.addDataEntryID(190);
		indexValue1.addDataEntryID(5700);
		indexValue1.addDataEntryID(85674);
		indexValue1.addDataEntryID(647211);

		byte[] byteArray = indexValue1.toByteArray();

		IndexValue indexValue2 = new IndexValue(byteArray);
		for (long dataEntryID : indexValue2.getDataEntryIDs()) {
			System.out.println(" * " + dataEntryID);
		}

		Assert.assertEquals(indexValue1, indexValue2);
	}

}
