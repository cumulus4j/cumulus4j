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
	public void before() throws Exception
	{
		System.out.println("before: Entered.");
		remote = null;

		boolean successful = false;
		int tryCounter = 0;
		while (!successful) {
			++tryCounter;
			try {
				InitialContext ic = createInitialContext();

				System.out.println("dataNucleusEjbInvocation: Created InitialContext instance. Looking up EJB.");

				remote = (DataNucleusTestRemote)ic.lookup(DataNucleusTestRemote.class.getName());

				System.out.println("dataNucleusEjbInvocation: Looked up EJB. Testing server availability.");

				if (!remote.isAvailable())
					throw new IllegalStateException("Server is not available!");

				System.out.println("dataNucleusEjbInvocation: Server is available.");
				successful = true;
			} catch (Exception x) {
				remote = null;
				if (tryCounter >= 3) // We try it 3 times - if it fails for the 3rd time, we rethrow.
					throw x;

				System.out.println("dataNucleusEjbInvocation: Caught exception! Will retry.");
				x.printStackTrace();
				System.out.println("dataNucleusEjbInvocation: Sleeping...");
				Thread.sleep(5000);
			}
		}

		if (remote == null)
			throw new IllegalStateException("Could not establish connection to server.");
	}

	@Test
	public void singleTransactionCommit() throws Exception {
		System.out.println("singleTransactionCommit: Entered.");

		UUID id = UUID.randomUUID();
		remote.test(id, false);

		boolean objectExists = remote.objectExists(id);
		Assert.assertTrue("remote.objectExists(id) == false!!! Expected object to be written into DB, but it is not there.", objectExists);
	}

	@Test
	public void singleTransactionRollback() throws Exception {
		System.out.println("singleTransactionRollback: Entered.");

		UUID id = UUID.randomUUID();
		boolean expectedExceptionThrown = false;
		try {
			remote.test(id, true);
		} catch (Exception x) {
			int index = ExceptionUtils.indexOfThrowable(x, TestRollbackException.class);
			if (index >= 0)
				expectedExceptionThrown = true;
			else
				throw x;
		}

		boolean objectExists = remote.objectExists(id);

		Assert.assertTrue("TestRollbackException was not thrown!", expectedExceptionThrown);
		Assert.assertFalse("remote.objectExists(id) == true!!! Expected object was written into DB, even though the transaction should have been rolled back.", objectExists);
	}

	public void mainTransactionCommitSubTransactionCommit() throws Exception
	{

	}

	public void mainTransactionCommitSubTransactionRollback() throws Exception
	{

	}

	public void mainTransactionRollbackSubTransactionRollback() throws Exception
	{

	}

	public void mainTransactionRollbackSubTransactionCommit() throws Exception
	{

	}
}
