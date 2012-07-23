package org.cumulus4j.jee.test.glassfish;

import java.sql.SQLException;
import java.util.UUID;

import javax.naming.InitialContext;

import junit.framework.Assert;

import org.apache.commons.lang.exception.ExceptionUtils;
import org.cumulus4j.jee.test.ejb.TestRollbackException;
import org.cumulus4j.jee.test.ejb.plaindatasource.DefaultDataSourceTestRemote;
import org.junit.Before;
import org.junit.Test;

public class DefaultDataSourceEjbInvocationIT extends AbstractGlassfishIT {

	private DefaultDataSourceTestRemote remote;

	@Before
	public void before() throws Exception {

		System.out.println("before: Entered.");

		boolean successful = false;
		int tryCounter = 0;
		while (!successful) {
			++tryCounter;
			try {
				InitialContext ic = createInitialContext();

				System.out
						.println("before: Created InitialContext instance. Looking up EJB.");

				remote = (DefaultDataSourceTestRemote) ic
						.lookup(DefaultDataSourceTestRemote.class.getName());

				System.out
						.println("before: Looked up EJB. Invoking remote method.");

				if (!remote.isAvailable())
					throw new IllegalStateException("Server is not available!");

				remote.init();

				System.out
						.println("before: Server is available and initialized.");

				successful = true;
			} catch (Exception x) {
				if (tryCounter >= 3) // We try it 3 times - if it fails for
										// the 3rd time, we rethrow.
					throw x;

				System.out.println("before: Caught exception! Will retry.");
				x.printStackTrace();
				System.out.println("before: Sleeping...");
				Thread.sleep(5000);
			}

		}

		if (remote == null)
			throw new IllegalStateException(
					"Could not establish connection to server.");
	}

	@Test
	public void testDataStoreConnection() throws SQLException {

		boolean result = remote.testDataStoreConnection();

		Assert.assertTrue(
				"Error happend while testing the connection to the datastore!",
				result);
	}

	@Test
	public void singleTransactionCommit() throws Exception {
		System.out.println("singleTransactionCommit: Entered.");

		UUID id = UUID.randomUUID();
		remote.testRollbackOnException(id, false);

		boolean objectExists = remote.keyExists(id);
		Assert.assertTrue(
				"remote.objectExists(id) == false!!! Expected object to be written into DB, but it is not there.",
				objectExists);
	}

	@Test
	public void singleTransactionRollback() throws Exception {
		System.out.println("singleTransactionRollback: Entered.");

		UUID id = UUID.randomUUID();
		boolean expectedExceptionThrown = false;
		try {
			remote.testRollbackOnException(id, true);
		} catch (Exception x) {
			int index = ExceptionUtils.indexOfThrowable(x,
					TestRollbackException.class);
			if (index >= 0)
				expectedExceptionThrown = true;
			else
				throw x;
		}

		boolean objectExists = remote.keyExists(id);

		Assert.assertTrue("TestRollbackException was not thrown!",
				expectedExceptionThrown);
		Assert.assertFalse(
				"remote.objectExists(id) == true!!! Expected object was written into DB, even though the transaction should have been rolled back.",
				objectExists);
	}

	@Test
	public void nestedTransactionMainTransactionCommitSubTransactionCommit() throws Exception {

		System.out
				.println("nestedTransactionMainTransactionCommitSubTransactionCommit: Entered.");

		UUID id1 = UUID.randomUUID();
		UUID id2 = UUID.randomUUID();
		remote.testRollbackOnNestedTransactionException(id1, id2, false, false);

		boolean object1Exists = remote.keyExists(id1);
		boolean object2Exists = remote.keyExists(id2);

		Assert.assertTrue(
				"remote.objectExists(id1) == false!!! Expected object to be written into DB, but it is not there.",
				object1Exists);
		Assert.assertTrue(
				"remote.objectExists(id2) == false!!! Expected object to be written into DB, but it is not there.",
				object2Exists);
	}

	@Test
	public void nestedTransactionMainTransactionCommitSubTransactionRollback() throws Exception {

		System.out
				.println("nestedTransactionMainTransactionCommitSubTransactionRollback: Entered.");

		UUID id1 = UUID.randomUUID();
		UUID id2 = UUID.randomUUID();

		remote.testRollbackOnNestedTransactionException(id1, id2, false, true);

		boolean object1Exists = remote.keyExists(id1);
		boolean object2Exists = remote.keyExists(id2);

		Assert.assertTrue(
				"remote.objectExists(id1) == false!!! Expected object to be written into DB, but it is not there.",
				object1Exists);
		Assert.assertFalse(
				"remote.objectExists(id2) == true!!! Expected object was written into DB, even though the transaction should have been rolled back.",
				object2Exists);
	}

	@Test
	public void nestedTransactionMainTransactionRollbackSubTransactionRollback()
			throws Exception {

		System.out
				.println("nestedTransactionMainTransactionRollbackSubTransactionRollback: Entered.");

		UUID id1 = UUID.randomUUID();
		UUID id2 = UUID.randomUUID();
		boolean expectedExceptionThrown = false;
		try {
			remote.testRollbackOnNestedTransactionException(id1, id2, true,
					true);
		} catch (Exception x) {
			int index = ExceptionUtils.indexOfThrowable(x,
					TestRollbackException.class);
			if (index >= 0)
				expectedExceptionThrown = true;
			else
				throw x;
		}

		boolean object1Exists = remote.keyExists(id1);
		boolean object2Exists = remote.keyExists(id2);

		Assert.assertTrue("TestRollbackException was not thrown!",
				expectedExceptionThrown);
		Assert.assertFalse(
				"remote.objectExists(id1) == true!!! Expected object was written into DB, even though the transaction should have been rolled back.",
				object1Exists);
		Assert.assertFalse(
				"remote.objectExists(id2) == true!!! Expected object was written into DB, even though the transaction should have been rolled back.",
				object2Exists);
	}

	@Test
	public void nestedTransactionMainTransactionRollbackSubTransactionCommit() throws Exception {

		System.out
				.println("nestedTransactionMainTransactionRollbackSubTransactionCommit: Entered.");

		UUID id1 = UUID.randomUUID();
		UUID id2 = UUID.randomUUID();
		boolean expectedExceptionThrown = false;
		try {
			remote.testRollbackOnNestedTransactionException(id1, id2, true,
					false);
		} catch (Exception x) {
			int index = ExceptionUtils.indexOfThrowable(x,
					TestRollbackException.class);
			if (index >= 0)
				expectedExceptionThrown = true;
			else
				throw x;
		}

		boolean object1Exists = remote.keyExists(id1);
		boolean object2Exists = remote.keyExists(id2);

		Assert.assertTrue("TestRollbackException was not thrown!",
				expectedExceptionThrown);
		Assert.assertFalse(
				"remote.objectExists(id1) == true!!! Expected object was written into DB, even though the transaction should have been rolled back.",
				object1Exists);
		Assert.assertTrue(
				"remote.objectExists(id2) == false!!! Expected object to be written into DB, but it is not there.",
				object2Exists);
	}
}
