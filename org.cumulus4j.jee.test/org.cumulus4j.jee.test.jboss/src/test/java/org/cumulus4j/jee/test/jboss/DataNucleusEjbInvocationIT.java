//package org.cumulus4j.jee.test.jboss;
//
//import javax.naming.InitialContext;
//
//import org.cumulus4j.jee.test.ejb.RollbackTestRemote;
//import org.cumulus4j.jee.test.ejb.datanucleus.DataNucleusTestRemote;
//
//
//public class DataNucleusEjbInvocationIT extends AbstractJBossIT {
//
//	@Override
//	protected void init(RollbackTestRemote remote) throws Exception{
//
//		remote.init();
//	}
//
//	@Override
//	protected RollbackTestRemote getRemote() throws Exception{
//
//		InitialContext ic = createInitialContext();
//
////		return (DataNucleusTestRemote) ic
////		.lookup(DataNucleusTestRemote.class.getName());
//		return (DataNucleusTestRemote) ic.lookup("org.cumulus4j.jee.test.ear-1.1.0-SNAPSHOT/DataNucleusTestBean/remote");
//	}
//}
