package org.cumulus4j.jee.test.glassfish;

import javax.naming.InitialContext;

import org.cumulus4j.jee.test.ejb.RollbackTestRemote;
import org.cumulus4j.jee.test.ejb.plaindatasource.PlainDataSourceTestRemote;


public class DefaultDataSourceEjbInvocationIT extends AbstractGlassfishIT {

	@Override
	protected RollbackTestRemote getRemote() throws Exception{

		InitialContext ic = createInitialContext();

		return (PlainDataSourceTestRemote) ic
				.lookup(PlainDataSourceTestRemote.class.getName());
	}
}
