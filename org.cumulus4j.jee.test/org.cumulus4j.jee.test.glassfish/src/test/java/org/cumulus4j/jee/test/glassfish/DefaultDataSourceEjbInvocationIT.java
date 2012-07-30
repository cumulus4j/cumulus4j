package org.cumulus4j.jee.test.glassfish;

import javax.naming.InitialContext;

import org.cumulus4j.jee.test.ejb.plaindatasource.DefaultDataSourceTestRemote;
import org.junit.Before;

public class DefaultDataSourceEjbInvocationIT extends AbstractGlassfishIT {

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
}
