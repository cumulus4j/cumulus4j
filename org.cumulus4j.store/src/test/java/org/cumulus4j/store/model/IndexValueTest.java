package org.cumulus4j.store.model;

import junit.framework.Assert;

import org.junit.Test;
import org.nightlabs.util.Stopwatch;

public class IndexValueTest {

	@Test
	public void simpleWriteRead() {
		IndexValue indexValue1 = new IndexValue();
//		indexValue1.addDataEntryID(0);
//		indexValue1.addDataEntryID(1);
//		indexValue1.addDataEntryID(25);
//		indexValue1.addDataEntryID(190);
//		indexValue1.addDataEntryID(5700);
//		indexValue1.addDataEntryID(85674);
//		indexValue1.addDataEntryID(647211);
//		indexValue1.addDataEntryID(12396244721123L);
		indexValue1.addDataEntryID( 999999L);
		indexValue1.addDataEntryID(1000000L);
		indexValue1.addDataEntryID(1000001L);

		byte[] byteArray = indexValue1.toByteArray();

		IndexValue indexValue2 = new IndexValue(byteArray);
		for (long dataEntryID : indexValue2.getDataEntryIDs()) {
			System.out.println(" * " + dataEntryID);
		}

		Assert.assertEquals(indexValue1, indexValue2);
	}

//	@Test
//	public void single() {
//		for (long l = 40000; l < 80000; ++l) {
//			System.out.println(l);
//			IndexValue indexValue1 = new IndexValue();
//			indexValue1.addDataEntryID(l);
//
//			byte[] byteArray = indexValue1.toByteArray();
//			IndexValue indexValue2 = new IndexValue(byteArray);
//
//			for (long dataEntryID : new TreeSet<Long>(indexValue2.getDataEntryIDs())) {
//				System.out.println(" * " + dataEntryID);
//			}
//
//			Assert.assertEquals(indexValue1, indexValue2);
//		}
//	}

	@Test
	public void counterLowWriteRead() {
		Stopwatch stopwatch = new Stopwatch();
		stopwatch.start("010.populate");
		IndexValue indexValue1 = new IndexValue();
		for (long l = 0; l < 10000000; ++l) {
			indexValue1.addDataEntryID(l);
		}
		stopwatch.stop("010.populate");

		stopwatch.start("020.toByteArray");
		byte[] byteArray = indexValue1.toByteArray();
		stopwatch.stop("020.toByteArray");

		stopwatch.start("030.parseByteArray");
		IndexValue indexValue2 = new IndexValue(byteArray);
		stopwatch.stop("030.parseByteArray");

		Assert.assertEquals(indexValue1, indexValue2);
		System.out.println("byteArray.length=" + byteArray.length);
		System.out.println(stopwatch.createHumanReport(true));
	}
}
