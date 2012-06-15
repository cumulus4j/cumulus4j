package org.cumulus4j.jee.test.glassfish;

import javax.naming.InitialContext;
import javax.naming.NamingException;

public abstract class AbstractGlassfishIT {

	public InitialContext createInitialContext() throws NamingException {
//	Properties props = new Properties();
//	props.setProperty("java.naming.factory.initial", "com.sun.enterprise.naming.SerialInitContextFactory");
//	props.setProperty("java.naming.factory.url.pkgs", "com.sun.enterprise.naming");
//	props.setProperty("java.naming.factory.state", "com.sun.corba.ee.impl.presentation.rmi.JNDIStateFactoryImpl");
//
//	// optional.  Defaults to localhost.  Only needed if web server is running
//	// on a different host than the appserver
//	props.setProperty("org.omg.CORBA.ORBInitialHost", "127.0.0.1");
//	// optional.  Defaults to 3700.  Only needed if target orb port is not 3700.
//	props.setProperty("org.omg.CORBA.ORBInitialPort", "3700");
//
//	InitialContext ic = new InitialContext(props);

		// It's recommended according to http://glassfish.java.net/javaee5/ejb/EJB_FAQ.html#StandaloneRemoteEJB
		// to use the no-arg constructor of InitialContext().
		InitialContext ic = new InitialContext();
		return ic;
	}

}
