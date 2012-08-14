package org.cumulus4j.jee.test.glassfish;

import java.util.Properties;
import java.util.UUID;

import javax.naming.InitialContext;
import javax.naming.NamingException;

import org.apache.commons.lang.exception.ExceptionUtils;
import org.cumulus4j.jee.test.ejb.RollbackTestRemote;
import org.cumulus4j.jee.test.ejb.TestRollbackException;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

public abstract class AbstractGlassfishIT {

	protected RollbackTestRemote remote;

	protected abstract RollbackTestRemote getRemote() throws Exception;

	protected abstract void init(RollbackTestRemote remote) throws Exception;

	@Before
	public void before() throws Exception {

		System.out.println("before: Entered.");
		remote = null;

		boolean successful = false;
		int tryCounter = 0;
		while (!successful) {
			++tryCounter;
			try {
				System.out
						.println("before: Created InitialContext instance. Looking up EJB.");

				remote = getRemote();

				System.out
						.println("before: Looked up EJB. Invoking remote method.");

				if (!remote.isAvailable())
					throw new IllegalStateException("Server is not available!");

				init(remote);

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

	public InitialContext createInitialContext() throws NamingException {
	Properties props = new Properties();
	props.setProperty("java.naming.factory.initial", "org.jnp.interfaces.LocalOnlyContextFactory");
	props.setProperty("java.naming.factory.url.pkgs", "org.jboss.naming:org.jnp.interfaces");
	props.setProperty("java.naming.factory.state", "com.sun.corba.ee.impl.presentation.rmi.JNDIStateFactoryImpl");

	// optional.  Defaults to localhost.  Only needed if web server is running
	// on a different host than the appserver
//	props.setProperty("org.omg.CORBA.ORBInitialHost", "127.0.0.1");
	props.setProperty("ejb.java.naming.provider.url", "127.0.0.1");
//	// optional.  Defaults to 3700.  Only needed if target orb port is not 3700.
//	props.setProperty("org.omg.CORBA.ORBInitialPort", "3700");

	InitialContext ic = new InitialContext(props);

//		// It's recommended according to http://glassfish.java.net/javaee5/ejb/EJB_FAQ.html#StandaloneRemoteEJB
//		// to use the no-arg constructor of InitialContext().
//		InitialContext ic = new InitialContext();
		return ic;
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
	public void nestedTransactionMainTransactionCommitSubTransactionCommit() throws Exception {

		System.out
				.println("nestedTransactionMainTransactionCommitSubTransactionCommit: Entered.");

		UUID id1 = UUID.randomUUID();
		UUID id2 = UUID.randomUUID();
		remote.testRollbackOnExceptionWithNestedTransaction(id1, id2, false, false);

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
	public void nestedTransactionMainTransactionCommitSubTransactionRollback() throws Exception {

		System.out
				.println("nestedTransactionMainTransactionCommitSubTransactionRollback: Entered.");

		UUID id1 = UUID.randomUUID();
		UUID id2 = UUID.randomUUID();

		remote.testRollbackOnExceptionWithNestedTransaction(id1, id2, false, true);

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
	public void nestedTransactionMainTransactionRollbackSubTransactionRollback()
			throws Exception {

		System.out
				.println("nestedTransactionMainTransactionRollbackSubTransactionRollback: Entered.");

		UUID id1 = UUID.randomUUID();
		UUID id2 = UUID.randomUUID();
		boolean expectedExceptionThrown = false;
		try {
			remote.testRollbackOnExceptionWithNestedTransaction(id1, id2, true,
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
	public void nestedTransactionMainTransactionRollbackSubTransactionCommit() throws Exception {

		System.out
				.println("nestedTransactionMainTransactionRollbackSubTransactionCommit: Entered.");

		UUID id1 = UUID.randomUUID();
		UUID id2 = UUID.randomUUID();
		boolean expectedExceptionThrown = false;
		try {
			remote.testRollbackOnExceptionWithNestedTransaction(id1, id2, true,
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
				.println("sharedTransactionCommitCommit: Entered.");

		UUID id1 = UUID.randomUUID();
		UUID id2 = UUID.randomUUID();

		remote.testRollbackOnExceptionWithSharedTransaction(id1, id2, false, false);

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

		remote.testRollbackOnExceptionWithSharedTransaction(id1, id2, false, true);

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
			remote.testRollbackOnExceptionWithSharedTransaction(id1, id2, true,
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
			remote.testRollbackOnExceptionWithSharedTransaction(id1, id2, true,
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
