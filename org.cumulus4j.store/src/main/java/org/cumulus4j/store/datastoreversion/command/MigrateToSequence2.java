package org.cumulus4j.store.datastoreversion.command;


import java.util.ArrayList;
import java.util.Collection;

import javax.jdo.PersistenceManager;
import javax.jdo.Query;

import org.cumulus4j.store.crypto.CryptoContext;
import org.cumulus4j.store.datastoreversion.AbstractDatastoreVersionCommand;
import org.cumulus4j.store.datastoreversion.CommandApplyParam;
import org.cumulus4j.store.model.Sequence;
import org.cumulus4j.store.model.Sequence2;
import org.cumulus4j.store.model.Sequence2DAO;

@SuppressWarnings("deprecation")
public class MigrateToSequence2 extends AbstractDatastoreVersionCommand
{
	@Override
	public int getCommandVersion() {
		return 0;
	}

	@Override
	public void apply(CommandApplyParam commandApplyParam) {
		// The Sequence[2] only exists in the data-datastore (not in the index-datastore), hence we return immediately, if the
		// current datastore is not the data-datastore.
		PersistenceManager pm = commandApplyParam.getPersistenceManager();
		CryptoContext cryptoContext = commandApplyParam.getCryptoContext();
		if (pm != cryptoContext.getPersistenceManagerForData())
			return;

		Sequence2DAO sequence2DAO = new Sequence2DAO(pm, cryptoContext.getKeyStoreRefID());

		Query q = pm.newQuery(Sequence.class);
		@SuppressWarnings("unchecked")
		Collection<Sequence> c = (Collection<Sequence>) q.execute();
		for (Sequence sequence : new ArrayList<Sequence>(c)) { // should work without the 'new ArrayList...', but I can't test and it doesn't harm
			Sequence2 sequence2 = sequence2DAO.createSequence2(sequence.getSequenceName());
			sequence2.setNextValue(sequence.getNextValue());
			pm.deletePersistent(sequence);
		}
	}
}
