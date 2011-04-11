package org.cumulus4j.api.keymanagement;

import java.util.HashMap;
import java.util.IdentityHashMap;
import java.util.Map;

import org.datanucleus.NucleusContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author Marco หงุ่ยตระกูล-Schulze - marco at nightlabs dot de
 */
public class KeyManagerRegistry
{
	private static final Logger logger = LoggerFactory.getLogger(KeyManagerRegistry.class);
	private static Map<NucleusContext, KeyManagerRegistry> nucleusContext2sharedInstance = new IdentityHashMap<NucleusContext, KeyManagerRegistry>();

	public static KeyManagerRegistry sharedInstance(NucleusContext nucleusContext)
	{
		synchronized (nucleusContext2sharedInstance) {
			KeyManagerRegistry registry = nucleusContext2sharedInstance.get(nucleusContext);
			if (registry == null) {
				registry = new KeyManagerRegistry(nucleusContext);
				nucleusContext2sharedInstance.put(nucleusContext, registry);
			}
			return registry;
		}
	}

	private NucleusContext nucleusContext;

	private Map<String, KeyManager> id2keyManager = new HashMap<String, KeyManager>();

	public KeyManagerRegistry(NucleusContext nucleusContext)
	{
		if (nucleusContext == null)
			throw new IllegalArgumentException("nucleusContext == null");

		this.nucleusContext = nucleusContext;
	}

	public KeyManager getKeyManager(String keyManagerID)
	{
		synchronized (id2keyManager) {
			KeyManager keyManager = id2keyManager.get(keyManagerID);
			if (keyManager == null) {
				keyManager = createKeyManager(keyManagerID);
				id2keyManager.put(keyManagerID, keyManager);
			}
			return keyManager;
		}
	}

	private KeyManager createKeyManager(String keyManagerID)
	{
		KeyManager keyManager;
		try {
			keyManager = (KeyManager) nucleusContext.getPluginManager().createExecutableExtension(
					"org.cumulus4j.api.keyManager",
					"keyManagerID", keyManagerID,
					"class",
					null, null
			);
		} catch (Exception e) {
			logger.error("Could not create KeyManager from extension: " + e, e);
			throw new RuntimeException(e);
		}

		if (keyManager == null)
			throw new IllegalStateException("There is no KeyManager registered with keyManagerID=\"" + keyManagerID + "\"!");

		keyManager.setKeyManagerID(keyManagerID);

		return keyManager;
	}
}
