package org.cumulus4j.store.test.metadata;

import java.util.List;

import javax.jdo.JDOUserException;
import javax.jdo.Query;

import junit.framework.Assert;

import org.cumulus4j.store.query.MemberNotQueryableException;
import org.cumulus4j.store.test.framework.AbstractJDOTransactionalTest;
import org.cumulus4j.store.test.framework.CleanupUtil;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Marco หงุ่ยตระกูล-Schulze - marco at nightlabs dot de
 */
public class MetaDataTest
extends AbstractJDOTransactionalTest
{
	private static final Logger logger = LoggerFactory.getLogger(MetaDataTest.class);

	@BeforeClass
	public static void clearDatabase()
	throws Exception
	{
		logger.info("clearDatabase: Clearing database (dropping all tables).");
		CleanupUtil.dropAllTables();
	}

	@Before
	public void createTestData()
	{
		pm.makePersistent(new Entity1("Müller", "Peter", "Otto", "Emil"));
		pm.makePersistent(new Entity1("Meier", "Peter", "Franz", "Emil"));
		pm.makePersistent(new Entity1("Schmidt", "Hans", "Franz", "Liesel"));
	}

	@Test
	public void queryQueryableField()
	{
		Query q = pm.newQuery(Entity1.class);
		q.setFilter("this.field1 == :arg");

		@SuppressWarnings("unchecked")
		List<Entity1> resultElements = (List<Entity1>) q.execute("Peter");

		Assert.assertEquals("Wrong number of result elements!", 2, resultElements.size());
		// No need for further asserts, because we have other tests for verifying the query functionality.
		// Here we primarily check that querying the 'field1' does work.
	}

	@Test
	public void queryFieldAnnotatedWithNotQueryable()
	{
		Query q = pm.newQuery(Entity1.class);
		q.setFilter("this.field2 == :arg");

		try {
			q.execute("Franz");
			Assert.fail("Executing the query should have caused a MemberNotQueryableException, but it didn't!");
		} catch (JDOUserException x) {
			MemberNotQueryableException mnqe = getCause(MemberNotQueryableException.class, x);
			if (mnqe == null)
				throw x; // not the expected exception => rethrow and cause test to have an error!
		}
	}

	@Test
	public void queryFieldAnnotatedWithVendorExtensionNotQueryable()
	{
		Query q = pm.newQuery(Entity1.class);
		q.setFilter("this.field3 == :arg");

		try {
			q.execute("Emil");
			Assert.fail("Executing the query should have caused a MemberNotQueryableException, but it didn't!");
		} catch (JDOUserException x) {
			MemberNotQueryableException mnqe = getCause(MemberNotQueryableException.class, x);
			if (mnqe == null)
				throw x; // not the expected exception => rethrow and cause test to have an error!
		}
	}

	private static <T extends Throwable> T getCause(Class<T> searchedThrowableType, Throwable t)
	{
		Throwable cause = t;
		while (cause != null) {
			if (searchedThrowableType.isInstance(cause))
				return searchedThrowableType.cast(cause);

			cause = cause.getCause();
		}
		return null;
	}
}
