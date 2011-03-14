package org.cumulus4j.test.movie;

import org.cumulus4j.test.core.AbstractTest;
import org.junit.Test;

public class MovieTest
extends AbstractTest
{
	@Test
	public void createData()
	{
		System.out.println(pm.currentTransaction().isActive());
	}


}
