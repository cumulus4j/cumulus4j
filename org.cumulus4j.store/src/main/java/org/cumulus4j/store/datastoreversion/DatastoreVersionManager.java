package org.cumulus4j.store.datastoreversion;

import java.io.StringReader;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Properties;
import java.util.Set;

import javax.jdo.FetchPlan;
import javax.jdo.PersistenceManager;

import org.cumulus4j.store.Cumulus4jStoreManager;
import org.cumulus4j.store.WorkInProgressException;
import org.cumulus4j.store.crypto.CryptoContext;
import org.cumulus4j.store.datastoreversion.command.IntroduceKeyStoreRefID;
import org.cumulus4j.store.datastoreversion.command.MigrateToSequence2;
import org.cumulus4j.store.datastoreversion.command.MinimumCumulus4jVersion;
import org.cumulus4j.store.datastoreversion.command.RecreateIndex;
import org.cumulus4j.store.model.DatastoreVersion;
import org.cumulus4j.store.model.DatastoreVersionDAO;
import org.cumulus4j.store.model.KeyStoreRef;

/**
 * @author Marco หงุ่ยตระกูล-Schulze - marco at nightlabs dot de
 */
@SuppressWarnings("unchecked")
public class DatastoreVersionManager {

	public static final int MANAGER_VERSION = 1;

	private static final Class<?>[] datastoreVersionCommandClasses = {
		// MinimumCumulus4jVersion should be the very first entry!
		MinimumCumulus4jVersion.class,

		IntroduceKeyStoreRefID.class,
		MigrateToSequence2.class,
		RecreateIndex.class
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

	private Cumulus4jStoreManager storeManager;
	private Set<Integer> performedKeyStoreRefIDs = Collections.synchronizedSet(new HashSet<Integer>());
	private volatile boolean performedGlobally = false;

	public DatastoreVersionManager(Cumulus4jStoreManager storeManager) {
		if (storeManager == null)
			throw new IllegalArgumentException("storeManager == null");

		this.storeManager = storeManager;
	}

	public void applyOnce(CryptoContext cryptoContext) {
		final Integer keyStoreRefID = cryptoContext.getKeyStoreRefID();
		if (performedKeyStoreRefIDs.contains(keyStoreRefID))
			return;

		synchronized (this) {
			if (performedKeyStoreRefIDs.contains(keyStoreRefID))
				return;

			if (!performedGlobally) {
				apply(cryptoContext, KeyStoreRef.GLOBAL_KEY_STORE_REF_ID);

				// only set performed, if we didn't encounter an exception => after apply(...)!
				performedGlobally = true;
			}

			apply(cryptoContext, keyStoreRefID);

			// only set performed, if we didn't encounter an exception => after apply(...)!
			performedKeyStoreRefIDs.add(keyStoreRefID);
		}
	}

	protected void apply(CryptoContext cryptoContext, int keyStoreRefID) {

		List<PersistenceManager> persistenceManagers = new ArrayList<PersistenceManager>(2);
		persistenceManagers.add(cryptoContext.getPersistenceManagerForData());
		if (cryptoContext.getPersistenceManagerForData() != cryptoContext.getPersistenceManagerForIndex())
			persistenceManagers.add(cryptoContext.getPersistenceManagerForIndex());

		for (PersistenceManager pm : persistenceManagers) {
			List<DatastoreVersionCommand> datastoreVersionCommands = createDatastoreVersionCommands();

			DatastoreVersionDAO datastoreVersionDAO = new DatastoreVersionDAO(pm);
			Map<String, DatastoreVersion> datastoreVersionID2DatastoreVersionMap = check(
					cryptoContext, keyStoreRefID, pm, datastoreVersionDAO, datastoreVersionCommands
			);
			for (DatastoreVersionCommand datastoreVersionCommand : datastoreVersionCommands) {
				if (KeyStoreRef.GLOBAL_KEY_STORE_REF_ID == keyStoreRefID && datastoreVersionCommand.isKeyStoreDependent())
					continue;

				if (KeyStoreRef.GLOBAL_KEY_STORE_REF_ID != keyStoreRefID && !datastoreVersionCommand.isKeyStoreDependent())
					continue;

				if (!isDatastoreVersionCommandEnabled(cryptoContext, datastoreVersionCommand))
					continue;

				try {
					applyOneCommand(cryptoContext, keyStoreRefID, pm, datastoreVersionDAO, datastoreVersionID2DatastoreVersionMap, datastoreVersionCommand);
				} catch (WorkInProgressException x) {
					throw x;
				} catch (Exception x) {
					throw new CommandApplyException(
							String.format("Applying command failed: commandID='%s': %s", datastoreVersionCommand.getCommandID(), x.toString()),
							x
					);
				}
			}
		}
	}

	protected boolean isDatastoreVersionCommandEnabled(CryptoContext cryptoContext, DatastoreVersionCommand datastoreVersionCommand) {
		String propertyKey = String.format("cumulus4j.DatastoreVersionCommand[%s].enabled", datastoreVersionCommand.getCommandID());
		Object propertyValue = cryptoContext.getExecutionContext().getStoreManager().getProperty(propertyKey);
		return propertyValue == null || !Boolean.FALSE.toString().toLowerCase(Locale.UK).equals(propertyValue.toString().toLowerCase(Locale.UK));
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

	protected Map<String, DatastoreVersion> check(CryptoContext cryptoContext, int keyStoreRefID, PersistenceManager pm, DatastoreVersionDAO datastoreVersionDAO, List<DatastoreVersionCommand> datastoreVersionCommands) {
		Map<String, DatastoreVersion> datastoreVersionID2DatastoreVersionMap = datastoreVersionDAO.getCommandID2DatastoreVersionMap(keyStoreRefID);

		for (DatastoreVersionCommand datastoreVersionCommand : datastoreVersionCommands) {
			DatastoreVersion datastoreVersion = datastoreVersionID2DatastoreVersionMap.get(datastoreVersionCommand.getCommandID());
			if (datastoreVersionCommand.isFinal()) {
				if (datastoreVersion != null && datastoreVersion.getCommandVersion() != datastoreVersionCommand.getCommandVersion()) {
					throw new IllegalStateException(String.format(
							"Final command class version does not match persistent version! datastoreVersionID='%s' datastoreVersionCommand.class='%s' datastoreVersionCommand.commandVersion=%s persistentDatastoreVersion.commandVersion=%s",
							datastoreVersionCommand.getCommandID(),
							datastoreVersionCommand.getClass().getName(),
							datastoreVersionCommand.getCommandVersion(),
							datastoreVersion.getCommandVersion()
					));
				}
			}
			else if (datastoreVersion != null && datastoreVersion.getCommandVersion() > datastoreVersionCommand.getCommandVersion()) {
				throw new IllegalStateException(String.format(
						"Non-final command class version is lower than persistent version! Downgrading is not supported! datastoreVersionID='%s' datastoreVersionCommand.class='%s' datastoreVersionCommand.commandVersion=%s persistentDatastoreVersion.commandVersion=%s",
						datastoreVersionCommand.getCommandID(),
						datastoreVersionCommand.getClass().getName(),
						datastoreVersionCommand.getCommandVersion(),
						datastoreVersion.getCommandVersion()
				));
			}
		}

		return datastoreVersionID2DatastoreVersionMap;
	}

	protected void applyOneCommand(
			CryptoContext cryptoContext, int keyStoreRefID, PersistenceManager pm,
			DatastoreVersionDAO datastoreVersionDAO, Map<String, DatastoreVersion> datastoreVersionID2DatastoreVersionMap, DatastoreVersionCommand datastoreVersionCommand
	) throws Exception
	{
		String datastoreVersionID = datastoreVersionCommand.getCommandID();
		DatastoreVersion datastoreVersion = datastoreVersionID2DatastoreVersionMap.get(datastoreVersionID);
		if (datastoreVersion == null ||
				(
					!datastoreVersionCommand.isFinal() &&
					datastoreVersionCommand.getCommandVersion() != datastoreVersion.getCommandVersion()
				)
		)
		{
			DatastoreVersion datastoreVersionCopy = detachDatastoreVersion(pm, datastoreVersion);
//			Map<String, DatastoreVersion> datastoreVersionID2DatastoreVersionMapCopy = detachDatastoreVersionID2DatastoreVersionMap(cryptoContext, pm, datastoreVersionID2DatastoreVersionMap);

			Properties workInProgressStateProperties = new Properties();
			if (datastoreVersion == null)
				datastoreVersion = new DatastoreVersion(datastoreVersionID, keyStoreRefID);
			else {
				if (datastoreVersion.getWorkInProgressStateProperties() != null)
					workInProgressStateProperties.load(new StringReader(datastoreVersion.getWorkInProgressStateProperties()));
			}

			// apply
			try {
				datastoreVersionCommand.apply(new CommandApplyParam(
						storeManager, cryptoContext, pm, datastoreVersionCopy, workInProgressStateProperties
				));
			} catch (WorkInProgressException x) {
				datastoreVersion.setApplyTimestamp(new Date());
				datastoreVersion.setWorkInProgressCommandVersion(datastoreVersionCommand.getCommandVersion());
				datastoreVersion.setWorkInProgressManagerVersion(MANAGER_VERSION);
				StringWriter writer = new StringWriter();
				workInProgressStateProperties.store(writer, null);
				datastoreVersion.setWorkInProgressStateProperties(writer.toString());
				pm.flush();
				throw x;
			}

			datastoreVersion.setApplyTimestamp(new Date());
			datastoreVersion.setCommandVersion(datastoreVersionCommand.getCommandVersion());
			datastoreVersion.setManagerVersion(MANAGER_VERSION);
			datastoreVersion.setWorkInProgressCommandVersion(null);
			datastoreVersion.setWorkInProgressManagerVersion(null);
			datastoreVersion.setWorkInProgressStateProperties(""); // field does not accept null (no need for this extra info in the DB)
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
