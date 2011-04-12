package org.cumulus4j.api.crypto;

import java.util.HashMap;
import java.util.Map;
import java.util.WeakHashMap;

import org.datanucleus.NucleusContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * <p>
 * Registry holding instances of {@link CryptoManager}.
 * </p>
 * <p>
 * There is one JVM-singleton-instance of {@link CryptoManagerRegistry} per {@link NucleusContext}.
 * Since it is held in a {@link WeakHashMap}, a <code>CryptoManagerRegistry</code> will be garbage-collected
 * when the corresponding <code>NucleusContext</code> is "forgotten".
 * </p>
 *
 * @author Marco หงุ่ยตระกูล-Schulze - marco at nightlabs dot de
 */
public class CryptoManagerRegistry
{
	private static final Logger logger = LoggerFactory.getLogger(CryptoManagerRegistry.class);
	private static Map<NucleusContext, CryptoManagerRegistry> nucleusContext2sharedInstance = new WeakHashMap<NucleusContext, CryptoManagerRegistry>();

	/**
	 * <p>
	 * Get the {@link CryptoManagerRegistry} corresponding to a given {@link NucleusContext}.
	 * </p>
	 * <p>
	 * If there is no registry known for the given <code>NucleusContext</code>, yet, it will be created and
	 * associated to this context. If this method is later on called again for the same <code>NucleusContext</code>,
	 * the same <code>CryptoManagerRegistry</code> will be returned.
	 * </p>
	 * <p>
	 * This method is thread-safe.
	 * </p>
	 *
	 * @param nucleusContext the <code>NucleusContext</code> for which to get the <code>CryptoManagerRegistry</code>.
	 * @return the <code>CryptoManagerRegistry</code> associated to the given <code>NucleusContext</code>; never <code>null</code>.
	 */
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

	private CryptoManagerRegistry(NucleusContext nucleusContext)
	{
		if (nucleusContext == null)
			throw new IllegalArgumentException("nucleusContext == null");

		this.nucleusContext = nucleusContext;
	}

	/**
	 * <p>
	 * Get a {@link CryptoManager} for the specified <code>cryptoManagerID</code>.
	 * </p>
	 * <p>
	 * Within the context of one <code>CryptoManagerRegistry</code> instance, this method will always
	 * return the same instance of <code>CryptoManager</code> for a certain <code>cryptoManagerID</code>.
	 * In other words, there is exactly one <code>CryptoManager</code> instance for each unique combination
	 * of {@link NucleusContext} and <code>cryptoManagerID</code>.
	 * </p>
	 * <p>
	 * This method is thread-safe.
	 * </p>
	 *
	 * @param cryptoManagerID the identifier used in the extension-declaration (in the <code>plugin.xml</code>).
	 * @return the {@link CryptoManager} for the specified <code>cryptoManagerID</code>; never <code>null</code>.
	 * @throws UnknownCryptoManagerIDException if there is no {@link CryptoManager} registered for the given identifier.
	 */
	public CryptoManager getCryptoManager(String cryptoManagerID)
	throws UnknownCryptoManagerIDException
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
	throws UnknownCryptoManagerIDException
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
			throw new UnknownCryptoManagerIDException(cryptoManagerID);

		cryptoManager.setCryptoManagerID(cryptoManagerID);

		return cryptoManager;
	}
}
