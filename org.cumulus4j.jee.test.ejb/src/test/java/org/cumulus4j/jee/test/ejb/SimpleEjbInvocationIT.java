package org.cumulus4j.jee.test.ejb;

import javax.naming.InitialContext;

import org.junit.Test;

public class SimpleEjbInvocationIT extends AbstractGlassfishIT {

	@Test
	public void simpleEjbInvocation() throws Exception {
		System.out.println("simpleEjbInvocation: Entered.");
		boolean successful = false;
		int tryCounter = 0;
		while (!successful) {
			++tryCounter;
			try {
				InitialContext ic = createInitialContext();

				System.out.println("simpleEjbInvocation: Created InitialContext instance. Looking up EJB.");

				SimpleTestRemote remote = (SimpleTestRemote)ic.lookup(SimpleTestRemote.class.getName());

				System.out.println("simpleEjbInvocation: Looked up EJB. Invoking remote method.");

				String result = remote.test("abcdefg");

				System.out.println("simpleEjbInvocation: Invoked remote method. result=" + result);
				successful = true;
			} catch (Exception x) {
				if (tryCounter >= 3) // We try it 3 times - if it fails for the 3rd time, we rethrow.
					throw x;

				System.out.println("simpleEjbInvocation: Caught exception! Will retry.");
				x.printStackTrace();
				System.out.println("simpleEjbInvocation: Sleeping...");
				Thread.sleep(5000);
			}
		}
	}

}
