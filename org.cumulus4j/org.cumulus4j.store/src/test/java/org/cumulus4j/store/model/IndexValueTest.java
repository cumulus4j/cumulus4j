package org.cumulus4j.store.model;

import junit.framework.Assert;

import org.cumulus4j.testutil.Stopwatch;
import org.junit.Test;

public class IndexValueTest {

	private static final long QUANTITY = 10000000;

	@Test
	public void simpleWriteRead() {
		IndexValue indexValue1 = new IndexValue();
		indexValue1.addDataEntryID(0);
		indexValue1.addDataEntryID(1);
		indexValue1.addDataEntryID(25);
		indexValue1.addDataEntryID(190);
		indexValue1.addDataEntryID(5700);
		indexValue1.addDataEntryID(85674);
		indexValue1.addDataEntryID(647211);
		indexValue1.addDataEntryID(12396244721123L);
		indexValue1.addDataEntryID( 999999L);
		indexValue1.addDataEntryID(1000000L);
		indexValue1.addDataEntryID(1000001L);

		System.out.println(">>> indexValue1 >>>");
		for (long dataEntryID : indexValue1.getDataEntryIDs()) {
			System.out.println(" * " + dataEntryID);
		}
		System.out.println("<<< indexValue1 <<<");
		byte[] byteArray = indexValue1.toByteArray();

		IndexValue indexValue2 = new IndexValue(byteArray);
		System.out.println(">>> indexValue2 >>>");
		for (long dataEntryID : indexValue2.getDataEntryIDs()) {
			System.out.println(" * " + dataEntryID);
		}
		System.out.println("<<< indexValue2 <<<");

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
		System.out.println();
		System.out.println("counterLowWriteRead");
		for (int run = 0; run < 2; ++ run) {
			System.gc(); // important for minimizing GC influence to call this before stopping time!!!
			Stopwatch stopwatch = new Stopwatch();
			IndexValue indexValue1 = new IndexValue();
			final long firstDataEntryID = 0;
			System.out.println("firstDataEntryID=" + firstDataEntryID);
			stopwatch.start("010.populate");
			for (long l = 0; l < QUANTITY; ++l) {
				indexValue1.addDataEntryID(firstDataEntryID + l);
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

	@Test
	public void counterMidWriteRead() {
		System.out.println();
		System.out.println("counterMidWriteRead");
		for (int run = 0; run < 2; ++ run) {
			System.gc(); // important for minimizing GC influence to call this before stopping time!!!
			Stopwatch stopwatch = new Stopwatch();
			IndexValue indexValue1 = new IndexValue();
			final long firstDataEntryID = Long.MAX_VALUE / 0xffffff;
			System.out.println("firstDataEntryID=" + firstDataEntryID);
			stopwatch.start("010.populate");
			for (long l = 0; l < QUANTITY; ++l) {
				indexValue1.addDataEntryID(firstDataEntryID + l);
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

	@Test
	public void counterHighWriteRead() {
		System.out.println();
		System.out.println("counterHighWriteRead");
		for (int run = 0; run < 2; ++ run) {
			System.gc(); // important for minimizing GC influence to call this before stopping time!!!
			Stopwatch stopwatch = new Stopwatch();
			IndexValue indexValue1 = new IndexValue();
			System.out.println("firstDataEntryID=" + (Long.MAX_VALUE - QUANTITY));
			stopwatch.start("010.populate");
			for (long l = 0; l < 10000000; ++l) {
				indexValue1.addDataEntryID(Long.MAX_VALUE - l);
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
}
