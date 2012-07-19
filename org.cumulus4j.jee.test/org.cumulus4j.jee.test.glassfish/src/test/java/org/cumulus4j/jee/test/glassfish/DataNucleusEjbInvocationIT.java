package org.cumulus4j.jee.test.glassfish;

import java.util.UUID;

import javax.naming.InitialContext;

import org.apache.commons.lang.exception.ExceptionUtils;
import org.cumulus4j.jee.test.ejb.DataNucleusTestRemote;
import org.cumulus4j.jee.test.ejb.TestRollbackException;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

public class DataNucleusEjbInvocationIT extends AbstractGlassfishIT {

	protected DataNucleusTestRemote remote;

	@Before
	public void before() throws Exception {
		System.out.println("before: Entered.");
		remote = null;

		boolean successful = false;
		int tryCounter = 0;
		while (!successful) {
			++tryCounter;
			try {
				InitialContext ic = createInitialContext();

				System.out
						.println("dataNucleusEjbInvocation: Created InitialContext instance. Looking up EJB.");

				remote = (DataNucleusTestRemote) ic
						.lookup(DataNucleusTestRemote.class.getName());

				System.out
						.println("dataNucleusEjbInvocation: Looked up EJB. Testing server availability.");

				if (!remote.isAvailable())
					throw new IllegalStateException("Server is not available!");

				System.out
						.println("dataNucleusEjbInvocation: Server is available.");
				successful = true;
			} catch (Exception x) {
				remote = null;
				if (tryCounter >= 3) // We try it 3 times - if it fails for the
										// 3rd time, we rethrow.
					throw x;

				System.out
						.println("dataNucleusEjbInvocation: Caught exception! Will retry.");
				x.printStackTrace();
				System.out.println("dataNucleusEjbInvocation: Sleeping...");
				Thread.sleep(5000);
			}
		}

		if (remote == null)
			throw new IllegalStateException(
					"Could not establish connection to server.");
	}

	@Test
	public void singleTransactionCommit() throws Exception {
		System.out.println("singleTransactionCommit: Entered.");

		UUID id = UUID.randomUUID();
		remote.testRollbackOnException(id, false);

		boolean objectExists = remote.objectExists(id);
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

		boolean objectExists = remote.objectExists(id);

		Assert.assertTrue("TestRollbackException was not thrown!",
				expectedExceptionThrown);
		Assert.assertFalse(
				"remote.objectExists(id) == true!!! Expected object was written into DB, even though the transaction should have been rolled back.",
				objectExists);
	}

	@Test
	public void mainTransactionCommitSubTransactionCommit() throws Exception {

		System.out
				.println("mainTransactionCommitSubTransactionCommit: Entered.");

		UUID id1 = UUID.randomUUID();
		UUID id2 = UUID.randomUUID();
		remote.testRollbackOnNestedTransactionException(id1, id2, false, false);

		boolean object1Exists = remote.objectExists(id1);
		boolean object2Exists = remote.objectExists(id2);

		Assert.assertTrue(
				"remote.objectExists(id1) == false!!! Expected object to be written into DB, but it is not there.",
				object1Exists);
		Assert.assertTrue(
				"remote.objectExists(id2) == false!!! Expected object to be written into DB, but it is not there.",
				object2Exists);
	}

	@Test
	public void mainTransactionCommitSubTransactionRollback() throws Exception {

		System.out
				.println("mainTransactionCommitSubTransactionRollback: Entered.");

		UUID id1 = UUID.randomUUID();
		UUID id2 = UUID.randomUUID();

		remote.testRollbackOnNestedTransactionException(id1, id2, false, true);

		boolean object1Exists = remote.objectExists(id1);
		boolean object2Exists = remote.objectExists(id2);

		Assert.assertTrue(
				"remote.objectExists(id1) == false!!! Expected object to be written into DB, but it is not there.",
				object1Exists);
		Assert.assertFalse(
				"remote.objectExists(id2) == true!!! Expected object was written into DB, even though the transaction should have been rolled back.",
				object2Exists);
	}

	@Test
	public void mainTransactionRollbackSubTransactionRollback()
			throws Exception {

		System.out
				.println("mainTransactionRollbackSubTransactionRollback: Entered.");

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

		boolean object1Exists = remote.objectExists(id1);
		boolean object2Exists = remote.objectExists(id2);

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
	public void mainTransactionRollbackSubTransactionCommit() throws Exception {

		System.out
				.println("mainTransactionRollbackSubTransactionCommit: Entered.");

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

		boolean object1Exists = remote.objectExists(id1);
		boolean object2Exists = remote.objectExists(id2);

		Assert.assertTrue("TestRollbackException was not thrown!",
				expectedExceptionThrown);
		Assert.assertFalse(
				"remote.objectExists(id1) == true!!! Expected object was written into DB, even though the transaction should have been rolled back.",
				object1Exists);
		Assert.assertTrue(
				"remote.objectExists(id2) == false!!! Expected object to be written into DB, but it is not there.",
				object2Exists);
	}

	@Test
	public void sharedTransactionCommitCommit() throws Exception {

		System.out
				.println("@TransactionAttribute(TransactionAttributeType.REQUIRES_NEW): Entered.");

		UUID id1 = UUID.randomUUID();
		UUID id2 = UUID.randomUUID();

		remote.testRollbackWithSharedTransaction(id1, id2, false, false);

		boolean object1Exists = remote.objectExists(id1);
		boolean object2Exists = remote.objectExists(id2);

		Assert.assertTrue(
				"remote.objectExists(id1) == false!!! Expected object to be written into DB, but it is not there.",
				object1Exists);
		Assert.assertTrue(
				"remote.objectExists(id2) == false!!! Expected object to be written into DB, but it is not there.",
				object2Exists);
	}

	@Test
	public void sharedTransactionCommitRollback() throws Exception {

		System.out
				.println("sharedTransactionCommitCommit: Entered.");

		UUID id1 = UUID.randomUUID();
		UUID id2 = UUID.randomUUID();

		remote.testRollbackWithSharedTransaction(id1, id2, false, true);

		boolean object1Exists = remote.objectExists(id1);
		boolean object2Exists = remote.objectExists(id2);

		Assert.assertFalse(
				"remote.objectExists(id1) == true!!! Expected object was written into DB, even though the transaction should have been rolled back.",
				object1Exists);
		Assert.assertFalse(
				"remote.objectExists(id2) == true!!! Expected object was written into DB, even though the transaction should have been rolled back.",
				object2Exists);
	}

	@Test
	public void sharedTransactionRollbackCommit() throws Exception {

		System.out
				.println("sharedTransactionRollbackCommit: Entered.");

		UUID id1 = UUID.randomUUID();
		UUID id2 = UUID.randomUUID();
		boolean expectedExceptionThrown = false;
		try {
			remote.testRollbackWithSharedTransaction(id1, id2, true,
					false);
		} catch (Exception x) {
			int index = ExceptionUtils.indexOfThrowable(x,
					TestRollbackException.class);
			if (index >= 0)
				expectedExceptionThrown = true;
			else
				throw x;
		}

		boolean object1Exists = remote.objectExists(id1);
		boolean object2Exists = remote.objectExists(id2);

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
	public void sharedTransactionRollbackRollback() throws Exception {

		System.out
				.println("sharedTransactionRollbackRollback: Entered.");

		UUID id1 = UUID.randomUUID();
		UUID id2 = UUID.randomUUID();
		boolean expectedExceptionThrown = false;
		try {
			remote.testRollbackWithSharedTransaction(id1, id2, true,
					true);
		} catch (Exception x) {
			int index = ExceptionUtils.indexOfThrowable(x,
					TestRollbackException.class);
			if (index >= 0)
				expectedExceptionThrown = true;
			else
				throw x;
		}

		boolean object1Exists = remote.objectExists(id1);
		boolean object2Exists = remote.objectExists(id2);

		Assert.assertTrue("TestRollbackException was not thrown!",
				expectedExceptionThrown);
		Assert.assertFalse(
				"remote.objectExists(id1) == true!!! Expected object was written into DB, even though the transaction should have been rolled back.",
				object1Exists);
		Assert.assertFalse(
				"remote.objectExists(id2) == true!!! Expected object was written into DB, even though the transaction should have been rolled back.",
				object2Exists);
	}
}
