package org.cumulus4j.crypto.test;

import org.cumulus4j.crypto.util.ChecksumAlgorithm;
import org.junit.Assert;
import org.junit.Test;

public class ChecksumAlgorithmTest
{
	@Test
	public void testToByteAndValueOf()
	{
		for (ChecksumAlgorithm checksumAlgorithm : ChecksumAlgorithm.values()) {
			byte b = checksumAlgorithm.toByte();
			ChecksumAlgorithm fromByte = ChecksumAlgorithm.valueOf(b);
			Assert.assertEquals(checksumAlgorithm, fromByte);
		}
	}

}
