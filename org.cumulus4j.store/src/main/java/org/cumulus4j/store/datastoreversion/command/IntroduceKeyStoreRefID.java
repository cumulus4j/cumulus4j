package org.cumulus4j.store.datastoreversion.command;


import java.util.Collection;

import javax.jdo.PersistenceManager;
import javax.jdo.Query;

import org.cumulus4j.store.crypto.CryptoContext;
import org.cumulus4j.store.datastoreversion.CommandApplyParam;
import org.cumulus4j.store.model.DataEntry;
import org.cumulus4j.store.model.IndexEntry;

public class IntroduceKeyStoreRefID extends AbstractDatastoreVersionCommand
{
	@Override
	public int getCommandVersion() {
		return 0;
	}

	@Override
	public void apply(CommandApplyParam commandApplyParam) {
		PersistenceManager pm = commandApplyParam.getPersistenceManager();
		CryptoContext cryptoContext = commandApplyParam.getCryptoContext();

		if (pm == cryptoContext.getPersistenceManagerForData())
			applyToData(commandApplyParam, pm);

		if (pm == cryptoContext.getPersistenceManagerForIndex())
			applyToIndex(commandApplyParam, pm);
	}

	protected void applyToData(CommandApplyParam commandApplyParam, PersistenceManager pm) {
		Collection<DataEntry> dataEntries = getAll(pm, DataEntry.class);
		for (DataEntry dataEntry : dataEntries) {
			dataEntry.setKeyStoreRefID(commandApplyParam.getCryptoContext().getKeyStoreRefID());
		}
	}

	protected void applyToIndex(CommandApplyParam commandApplyParam, PersistenceManager pm) {
		Collection<IndexEntry> indexEntries = getAll(pm, IndexEntry.class);
		for (IndexEntry indexEntry : indexEntries) {
			indexEntry.setKeyStoreRefID(commandApplyParam.getCryptoContext().getKeyStoreRefID());
		}
	}

	protected <T> Collection<T> getAll(PersistenceManager pm, Class<T> entityClass) {
		Query q = pm.newQuery(entityClass);
		@SuppressWarnings("unchecked")
		Collection<T> c = (Collection<T>) q.execute();
		return c;
	}
}
