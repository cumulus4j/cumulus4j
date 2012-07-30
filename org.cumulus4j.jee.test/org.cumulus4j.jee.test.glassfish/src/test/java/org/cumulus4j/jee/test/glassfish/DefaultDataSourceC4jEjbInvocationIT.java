package org.cumulus4j.jee.test.glassfish;


public class DefaultDataSourceC4jEjbInvocationIT{// extends AbstractGlassfishIT {
//
//	@Test
//	public void defaultDataSourceC4jEjbInvocation() throws Exception {
//		System.out.println("defaultDataSourceC4jEjbInvocation: Entered.");
//		boolean successful = false;
//		int tryCounter = 0;
//		while (!successful) {
//			++tryCounter;
//			try {
//				InitialContext ic = createInitialContext();
//
//				System.out.println("defaultDataSourceC4jEjbInvocation: Created InitialContext instance. Looking up EJB.");
//
//				Cumulus4jTestRemote remote = (Cumulus4jTestRemote)ic.lookup(Cumulus4jTestRemote.class.getName());
//
//				System.out.println("defaultDataSourceC4jEjbInvocation: Looked up EJB. Invoking remote method.");
//
//				remote.test();
//
//				System.out.println("defaultDataSourceC4jEjbInvocation: Invoked remote method successfully.");
//				successful = true;
//			} catch (Exception x) {
//				if (tryCounter >= 3) // We try it 3 times - if it fails for the 3rd time, we rethrow.
//					throw x;
//
//				System.out.println("defaultDataSourceC4jEjbInvocation: Caught exception! Will retry.");
//				x.printStackTrace();
//				System.out.println("defaultDataSourceC4jEjbInvocation: Sleeping...");
//				Thread.sleep(5000);
//			}
//		}
//	}

}
