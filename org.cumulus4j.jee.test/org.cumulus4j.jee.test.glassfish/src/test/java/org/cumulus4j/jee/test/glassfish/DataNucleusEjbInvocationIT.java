package org.cumulus4j.jee.test.glassfish;

import javax.naming.InitialContext;

import org.cumulus4j.jee.test.ejb.datanucleus.DataNucleusTestRemote;
import org.junit.Before;

public class DataNucleusEjbInvocationIT extends AbstractGlassfishIT {

	@Override
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
						.println("before: Created InitialContext instance. Looking up EJB.");

				remote = (DataNucleusTestRemote) ic
						.lookup(DataNucleusTestRemote.class.getName());

				System.out
						.println("before: Looked up EJB. Testing server availability.");

				if (!remote.isAvailable())
					throw new IllegalStateException("Server is not available!");

				System.out
						.println("before: Server is available.");
				successful = true;
			} catch (Exception x) {
				remote = null;
				if (tryCounter >= 3) // We try it 3 times - if it fails for the
										// 3rd time, we rethrow.
					throw x;

				System.out
						.println("before: Caught exception! Will retry.");
				x.printStackTrace();
				System.out.println("before: Sleeping...");
				Thread.sleep(5000);
			}
		}

		if (remote == null)
			throw new IllegalStateException(
					"Could not establish connection to server.");
	}
}
