package org.cumulus4j.jee.test.jboss;

import javax.naming.InitialContext;

import org.cumulus4j.jee.test.ejb.RollbackTestRemote;
import org.cumulus4j.jee.test.ejb.plaindatasource.PlainDataSourceTestRemote;


public class DefaultDataSourceEjbInvocationIT extends AbstractJBossIT {

	@Override
	protected void init(RollbackTestRemote remote) throws Exception{
		remote.init();
	}

	@Override
	protected RollbackTestRemote getRemote() throws Exception{

		InitialContext ic = createInitialContext();

//		return (PlainDataSourceTestRemote) ic
//				.lookup(PlainDataSourceTestRemote.class.getName());

		return (PlainDataSourceTestRemote) ic.lookup("org.cumulus4j.jee.test.ear-1.1.0-SNAPSHOT/PlainDataSourceTestBean/remote");
	}
}
