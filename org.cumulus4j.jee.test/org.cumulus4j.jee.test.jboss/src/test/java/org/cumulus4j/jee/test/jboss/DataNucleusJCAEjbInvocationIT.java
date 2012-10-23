//package org.cumulus4j.jee.test.jboss;
//
//import javax.naming.InitialContext;
//
//import org.cumulus4j.jee.test.ejb.RollbackTestRemote;
//import org.cumulus4j.jee.test.ejb.datanucleusJCA.DataNucleusJCATestRemote;
//
//
//public class DataNucleusJCAEjbInvocationIT extends AbstractJBossIT {
//
//	@Override
//	protected RollbackTestRemote getRemote() throws Exception{
//
//		InitialContext ic = createInitialContext();
//
//		return (DataNucleusJCATestRemote) ic
//		.lookup(DataNucleusJCATestRemote.class.getName());
//	}
//}
