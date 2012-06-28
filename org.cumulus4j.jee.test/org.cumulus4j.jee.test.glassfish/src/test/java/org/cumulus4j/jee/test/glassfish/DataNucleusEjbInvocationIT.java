package org.cumulus4j.jee.test.glassfish;

import javax.naming.InitialContext;

import org.cumulus4j.jee.test.ejb.DataNucleusTestRemote;
import org.junit.Test;

public class DataNucleusEjbInvocationIT extends AbstractGlassfishIT {

	@Test
	public void dataNucleusEjbInvocation() throws Exception {
		System.out.println("dataNucleusEjbInvocation: Entered.");
		boolean successful = false;
		int tryCounter = 0;
		while (!successful) {
			++tryCounter;
			try {
				InitialContext ic = createInitialContext();

				System.out.println("dataNucleusEjbInvocation: Created InitialContext instance. Looking up EJB.");

				DataNucleusTestRemote remote = (DataNucleusTestRemote)ic.lookup(DataNucleusTestRemote.class.getName());

				System.out.println("dataNucleusEjbInvocation: Looked up EJB. Invoking remote method.");

				remote.test();

				System.out.println("dataNucleusEjbInvocation: Invoked remote method successfully.");
				successful = true;
			} catch (Exception x) {
				if (tryCounter >= 3) // We try it 3 times - if it fails for the 3rd time, we rethrow.
					throw x;

				System.out.println("dataNucleusEjbInvocation: Caught exception! Will retry.");
				x.printStackTrace();
				System.out.println("dataNucleusEjbInvocation: Sleeping...");
				Thread.sleep(5000);
			}
		}
	}

}
