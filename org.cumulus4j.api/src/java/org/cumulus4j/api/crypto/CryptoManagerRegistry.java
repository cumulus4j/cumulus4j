package org.cumulus4j.api.crypto;

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
public class CryptoManagerRegistry
{
	private static final Logger logger = LoggerFactory.getLogger(CryptoManagerRegistry.class);
	private static Map<NucleusContext, CryptoManagerRegistry> nucleusContext2sharedInstance = new IdentityHashMap<NucleusContext, CryptoManagerRegistry>();

	public static CryptoManagerRegistry sharedInstance(NucleusContext nucleusContext)
	{
		synchronized (nucleusContext2sharedInstance) {
			CryptoManagerRegistry registry = nucleusContext2sharedInstance.get(nucleusContext);
			if (registry == null) {
				registry = new CryptoManagerRegistry(nucleusContext);
				nucleusContext2sharedInstance.put(nucleusContext, registry);
			}
			return registry;
		}
	}

	private NucleusContext nucleusContext;

	private Map<String, CryptoManager> id2keyManager = new HashMap<String, CryptoManager>();

	public CryptoManagerRegistry(NucleusContext nucleusContext)
	{
		if (nucleusContext == null)
			throw new IllegalArgumentException("nucleusContext == null");

		this.nucleusContext = nucleusContext;
	}

	public CryptoManager getCryptoManager(String cryptoManagerID)
	{
		synchronized (id2keyManager) {
			CryptoManager cryptoManager = id2keyManager.get(cryptoManagerID);
			if (cryptoManager == null) {
				cryptoManager = createCryptoManager(cryptoManagerID);
				id2keyManager.put(cryptoManagerID, cryptoManager);
			}
			return cryptoManager;
		}
	}

	private CryptoManager createCryptoManager(String cryptoManagerID)
	{
		CryptoManager cryptoManager;
		try {
			cryptoManager = (CryptoManager) nucleusContext.getPluginManager().createExecutableExtension(
					"org.cumulus4j.api.cryptoManager",
					"cryptoManagerID", cryptoManagerID,
					"class",
					null, null
			);
		} catch (Exception e) {
			logger.error("Could not create CryptoManager from extension: " + e, e);
			throw new RuntimeException(e);
		}

		if (cryptoManager == null)
			throw new IllegalStateException("There is no CryptoManager registered with cryptoManagerID=\"" + cryptoManagerID + "\"!");

		cryptoManager.setCryptoManagerID(cryptoManagerID);

		return cryptoManager;
	}
}
