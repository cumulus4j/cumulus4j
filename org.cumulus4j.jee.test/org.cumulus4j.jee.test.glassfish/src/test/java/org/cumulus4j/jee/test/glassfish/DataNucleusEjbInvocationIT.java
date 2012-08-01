package org.cumulus4j.jee.test.glassfish;

import javax.naming.InitialContext;

import org.cumulus4j.jee.test.ejb.RollbackTestRemote;
import org.cumulus4j.jee.test.ejb.datanucleus.DataNucleusTestRemote;


public class DataNucleusEjbInvocationIT extends AbstractGlassfishIT {

	@Override
	protected RollbackTestRemote getRemote() throws Exception{

		InitialContext ic = createInitialContext();

		return (DataNucleusTestRemote) ic
		.lookup(DataNucleusTestRemote.class.getName());
	}
}
