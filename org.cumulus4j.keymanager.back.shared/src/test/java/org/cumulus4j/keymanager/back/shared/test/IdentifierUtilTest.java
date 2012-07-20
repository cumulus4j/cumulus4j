package org.cumulus4j.keymanager.back.shared.test;

import java.util.HashSet;
import java.util.Set;

import junit.framework.Assert;

import org.cumulus4j.keymanager.back.shared.IdentifierUtil;
import org.junit.Test;

public class IdentifierUtilTest {

	/**
	 * This test generates multiple times a big bunch (100000) of random IDs with a certain length (8 characters) using
	 * {@link IdentifierUtil#createRandomID()}. It expects that there is no collision in each bunch.
	 */
	@Test
	public void simpleUniquenessTest() {
		// How many characters shall each randomID have?
		final int randomIDLength = 8;
		// How many randomIDs are generated (and must be unique) per run?
		final int randomIDQtyPerRun = 100000;
		// How many runs are performed?
		final int runQty = 10;

		for (int run = 0; run < runQty; ++run) {
			Set<String> generated = new HashSet<String>(randomIDQtyPerRun);
			for (int i = 0; i < randomIDQtyPerRun; ++i) {
				String randomID = IdentifierUtil.createRandomID(randomIDLength);
				if (!generated.add(randomID))
					Assert.fail("Duplicate randomID! run=" + run + " i=" + i);
			}
		}
	}

}
