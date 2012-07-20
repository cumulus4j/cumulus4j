package org.cumulus4j.store.datastoreversion;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.jdo.FetchPlan;
import javax.jdo.PersistenceManager;

import org.cumulus4j.store.crypto.CryptoContext;
import org.cumulus4j.store.datastoreversion.command.IntroduceKeyStoreRefID;
import org.cumulus4j.store.datastoreversion.command.MigrateToSequence2;
import org.cumulus4j.store.datastoreversion.command.MinimumCumulus4jVersion;
import org.cumulus4j.store.model.DatastoreVersion;
import org.cumulus4j.store.model.DatastoreVersionDAO;

/**
 * @author Marco หงุ่ยตระกูล-Schulze - marco at nightlabs dot de
 */
@SuppressWarnings("unchecked")
public class DatastoreVersionManager {

	public static final int MANAGER_VERSION = 1;

	private static final Class<?>[] datastoreVersionCommandClasses = {
		MinimumCumulus4jVersion.class,
		IntroduceKeyStoreRefID.class,
		MigrateToSequence2.class
	};

	private static final List<Class<? extends DatastoreVersionCommand>> datastoreVersionCommandClassList;
	static {
		List<Class<? extends DatastoreVersionCommand>> list = new ArrayList<Class<? extends DatastoreVersionCommand>>(datastoreVersionCommandClasses.length);
		for (Class<?> c : datastoreVersionCommandClasses) {
			if (c == null)
				throw new IllegalStateException("datastoreVersionCommandClasses contains null element!");

			if (!DatastoreVersionCommand.class.isAssignableFrom(c))
				throw new IllegalStateException(String.format("%s does not implement %s!", c.getName(), DatastoreVersionCommand.class.getName()));

			list.add((Class<? extends DatastoreVersionCommand>) c);
		}
		datastoreVersionCommandClassList = Collections.unmodifiableList(list);
	}

	private volatile boolean performed = false;

	public void applyOnce(CryptoContext cryptoContext) {
		if (performed)
			return;

		synchronized (this) {
			if (performed)
				return;

			apply(cryptoContext);

			// only set performed, if we didn't encounter an exception => after perform(...)!
			performed = true;
		}
	}

	protected void apply(CryptoContext cryptoContext) {
		List<DatastoreVersionCommand> datastoreVersionCommands = createDatastoreVersionCommands();

		List<PersistenceManager> persistenceManagers = new ArrayList<PersistenceManager>(2);
		persistenceManagers.add(cryptoContext.getPersistenceManagerForData());
		if (cryptoContext.getPersistenceManagerForData() != cryptoContext.getPersistenceManagerForIndex())
			persistenceManagers.add(cryptoContext.getPersistenceManagerForIndex());

		for (PersistenceManager pm : persistenceManagers) {
			DatastoreVersionDAO datastoreVersionDAO = new DatastoreVersionDAO(pm);
			Map<String, DatastoreVersion> datastoreVersionID2DatastoreVersionMap = check(
					cryptoContext, pm, datastoreVersionDAO, datastoreVersionCommands
			);
			for (DatastoreVersionCommand datastoreVersionCommand : datastoreVersionCommands) {
				try {
					applyOneCommand(cryptoContext, pm, datastoreVersionDAO, datastoreVersionID2DatastoreVersionMap, datastoreVersionCommand);
				} catch (Exception x) {
					throw new CommandApplyException(
							String.format("Applying command failed: datastoreVersionID='%s': %s", datastoreVersionCommand.getDatastoreVersionID(), x.toString()),
							x
					);
				}
			}
		}
	}

	protected List<DatastoreVersionCommand> createDatastoreVersionCommands() {
		List<DatastoreVersionCommand> datastoreVersionCommands = new ArrayList<DatastoreVersionCommand>(datastoreVersionCommandClassList.size());
		try {
			for (Class<? extends DatastoreVersionCommand> klass : datastoreVersionCommandClassList) {
				DatastoreVersionCommand command = klass.newInstance();
				datastoreVersionCommands.add(command);
			}
		} catch (InstantiationException e) {
			throw new RuntimeException(e);
		} catch (IllegalAccessException e) {
			throw new RuntimeException(e);
		}
		return datastoreVersionCommands;
	}

	protected Map<String, DatastoreVersion> check(CryptoContext cryptoContext, PersistenceManager pm, DatastoreVersionDAO datastoreVersionDAO, List<DatastoreVersionCommand> datastoreVersionCommands) {
		Map<String, DatastoreVersion> datastoreVersionID2DatastoreVersionMap = datastoreVersionDAO.getDatastoreVersionID2DatastoreVersionMap();

		for (DatastoreVersionCommand datastoreVersionCommand : datastoreVersionCommands) {
			DatastoreVersion datastoreVersion = datastoreVersionID2DatastoreVersionMap.get(datastoreVersionCommand.getDatastoreVersionID());
			if (datastoreVersionCommand.isFinal()) {
				if (datastoreVersion != null && datastoreVersion.getCommandVersion() != datastoreVersionCommand.getCommandVersion()) {
					throw new IllegalStateException(String.format(
							"Final command class version does not match persistent version! datastoreVersionID='%s' datastoreVersionCommand.class='%s' datastoreVersionCommand.commandVersion=%s persistentDatastoreVersion.commandVersion=%s",
							datastoreVersionCommand.getDatastoreVersionID(),
							datastoreVersionCommand.getClass().getName(),
							datastoreVersionCommand.getCommandVersion(),
							datastoreVersion.getCommandVersion()
					));
				}
			}
			else if (datastoreVersion != null && datastoreVersion.getCommandVersion() > datastoreVersionCommand.getCommandVersion()) {
				throw new IllegalStateException(String.format(
						"Non-final command class version is lower than persistent version! Downgrading is not supported! datastoreVersionID='%s' datastoreVersionCommand.class='%s' datastoreVersionCommand.commandVersion=%s persistentDatastoreVersion.commandVersion=%s",
						datastoreVersionCommand.getDatastoreVersionID(),
						datastoreVersionCommand.getClass().getName(),
						datastoreVersionCommand.getCommandVersion(),
						datastoreVersion.getCommandVersion()
				));
			}
		}

		return datastoreVersionID2DatastoreVersionMap;
	}

	protected void applyOneCommand(
			CryptoContext cryptoContext, PersistenceManager pm, DatastoreVersionDAO datastoreVersionDAO,
			Map<String, DatastoreVersion> datastoreVersionID2DatastoreVersionMap, DatastoreVersionCommand datastoreVersionCommand
	)
	{
		String datastoreVersionID = datastoreVersionCommand.getDatastoreVersionID();
		DatastoreVersion datastoreVersion = datastoreVersionID2DatastoreVersionMap.get(datastoreVersionID);
		if (datastoreVersion == null ||
				(
					!datastoreVersionCommand.isFinal() &&
					datastoreVersionCommand.getCommandVersion() != datastoreVersion.getCommandVersion()
				)
		)
		{
			DatastoreVersion datastoreVersionCopy = detachDatastoreVersion(pm, datastoreVersion);
			Map<String, DatastoreVersion> datastoreVersionID2DatastoreVersionMapCopy = detachDatastoreVersionID2DatastoreVersionMap(cryptoContext, pm, datastoreVersionID2DatastoreVersionMap);
			datastoreVersionCommand.apply(new CommandApplyParam(cryptoContext, pm, datastoreVersionCopy, datastoreVersionID2DatastoreVersionMapCopy));
			if (datastoreVersion == null)
				datastoreVersion = new DatastoreVersion(datastoreVersionID);

			datastoreVersion.setApplyTimestamp(new Date());
			datastoreVersion.setCommandVersion(datastoreVersionCommand.getCommandVersion());
			datastoreVersion.setManagerVersion(MANAGER_VERSION);
			pm.makePersistent(datastoreVersion); // just in case, it's new - otherwise doesn't hurt
			pm.flush(); // provoke early failure
		}
	}

	protected DatastoreVersion detachDatastoreVersion(PersistenceManager pm, DatastoreVersion attached) {
		pm.getFetchPlan().setGroup(FetchPlan.ALL);
		pm.getFetchPlan().setMaxFetchDepth(-1);
		return attached == null ? null : pm.detachCopy(attached);
	}

	protected Map<String, DatastoreVersion> detachDatastoreVersionID2DatastoreVersionMap(CryptoContext cryptoContext, PersistenceManager pm, Map<String, DatastoreVersion> datastoreVersionID2DatastoreVersionMap) {
		Map<String, DatastoreVersion> result = new HashMap<String, DatastoreVersion>(datastoreVersionID2DatastoreVersionMap.size());
		for (Map.Entry<String, DatastoreVersion> me : datastoreVersionID2DatastoreVersionMap.entrySet()) {
			result.put(me.getKey(), detachDatastoreVersion(pm, me.getValue()));
		}
		return Collections.unmodifiableMap(result);
	}
}
