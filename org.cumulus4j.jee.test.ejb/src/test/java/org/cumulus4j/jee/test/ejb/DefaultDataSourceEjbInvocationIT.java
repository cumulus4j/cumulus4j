package org.cumulus4j.jee.test.ejb;

import javax.naming.InitialContext;

import org.junit.Test;

public class DefaultDataSourceEjbInvocationIT extends AbstractGlassfishIT {

	@Test
	public void defaultDataSourceEjbInvocation() throws Exception {
		System.out.println("defaultDataSourceEjbInvocation: Entered.");
		boolean successful = false;
		int tryCounter = 0;
		while (!successful) {
			++tryCounter;
			try {
				InitialContext ic = createInitialContext();

				System.out.println("defaultDataSourceEjbInvocation: Created InitialContext instance. Looking up EJB.");

				DefaultDataSourceTestRemote remote = (DefaultDataSourceTestRemote)ic.lookup(DefaultDataSourceTestRemote.class.getName());

				System.out.println("defaultDataSourceEjbInvocation: Looked up EJB. Invoking remote method.");

				remote.test();

				System.out.println("defaultDataSourceEjbInvocation: Invoked remote method successfully.");
				successful = true;
			} catch (Exception x) {
				if (tryCounter >= 3) // We try it 3 times - if it fails for the 3rd time, we rethrow.
					throw x;

				System.out.println("defaultDataSourceEjbInvocation: Caught exception! Will retry.");
				x.printStackTrace();
				System.out.println("defaultDataSourceEjbInvocation: Sleeping...");
				Thread.sleep(5000);
			}
		}
	}

}
