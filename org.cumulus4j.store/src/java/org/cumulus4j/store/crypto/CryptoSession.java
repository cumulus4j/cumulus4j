package org.cumulus4j.store.crypto;

import java.util.Date;

import org.cumulus4j.crypto.Cipher;
import org.datanucleus.NucleusContext;

/**
 * <p>
 * A {@link CryptoSession} is a session managed by a client to decrypt/encrypt data.
 * </p>
 * <p>
 * Data can only be decrypted (or encrypted) within the scope of a valid session. That means,
 * the client must open a <code>CryptoSession</code> prior to persisting/querying data.
 * </p>
 * <p>
 * There exists one <code>CryptoSession</code> instance for each unique combination of
 * {@link NucleusContext}, {@link CryptoManager#getCryptoManagerID() cryptoManagerID} and
 * {@link #getCryptoSessionID() cryptoSessionID}. Therefore, it can happen, that multiple
 * <code>PersistenceManager</code>/<code>EntityManager</code> instances access the same
 * <code>CryptoSession</code> from multiple threads.
 * </p>
 * <p>
 * Thus all implementations of CryptoSession must be thread-safe! In this context, it is important
 * to know that {@link Cipher} is <b>not</b> thread-safe! You should thus always synchronize on the <code>Cipher</code>
 * instance before using it (if you share them, which you probably do due to the expensiveness of key-initialisations).
 * </p>
 * <p>
 * A <code>CryptoSession</code> must not be instantiated directly, but instead obtained via
 * {@link CryptoManager#getCryptoSession(String)}. In other words, a new instance of
 * <code>CryptoSession</code> must only be created within the {@link CryptoManager}
 * implementation.
 * </p>
 * <p>
 * <b>Important:</b> It is strongly recommended to subclass {@link AbstractCryptoSession}
 * instead of directly implementing this interface!
 * </p>
 *
 * @author Marco หงุ่ยตระกูล-Schulze - marco at nightlabs dot de
 */
public interface CryptoSession
{
	/**
	 * <p>
	 * Property-name used to pass the {@link #getCryptoSessionID() cryptoSessionID} to the Cumulus4j-core.
	 * </p>
	 * <p>
	 * The property must be passed to the Cumulus4j-core via
	 * {@link javax.jdo.PersistenceManager#setProperty(String, Object)} or
	 * {@link javax.persistence.EntityManager#setProperty(String, Object)}.
	 * </p>
	 */
	static final String PROPERTY_CRYPTO_SESSION_ID = "cumulus4j.cryptoSessionID";

	/**
	 * <p>
	 * Set the {@link CryptoManager} to which this session belongs.
	 * </p>
	 * <p>
	 * If you subclass {@link AbstractCryptoManager} (instead of directly implementing the {@link CryptoManager} interface)
	 * you must never call this method. Otherwise, it is expected, that you call this method once in {@link CryptoManager#getCryptoSession(String)}
	 * after creating a new <code>CryptoSession</code>, before returning it.
	 * </p>
	 *
	 * @param cryptoManager the <code>CryptoManager</code> to which this session belongs.
	 * @see #getCryptoManager()
	 */
	void setCryptoManager(CryptoManager cryptoManager);

	/**
	 * <p>
	 * Get the {@link CryptoManager} to which this session belongs.
	 * </p>
	 * @return the <code>CryptoManager</code> to which this session belongs.
	 */
	CryptoManager getCryptoManager();

	/**
	 * <p>
	 * Set the {@link #getCryptoSessionID() cryptoSessionID}.
	 * </p>
	 * <p>
	 * If you subclass {@link AbstractCryptoManager} (instead of directly implementing the {@link CryptoManager} interface)
	 * you must never call this method. Otherwise, it is expected, that you call this method once in {@link CryptoManager#getCryptoSession(String)}
	 * after creating a new <code>CryptoSession</code>, before returning it.
	 * </p>
	 *
	 * @param cryptoSessionID the identifier of this session.
	 * @see #getCryptoSessionID()
	 */
	void setCryptoSessionID(String cryptoSessionID);

	String getCryptoSessionID();

	Date getCreationTimestamp();

	/**
	 * <p>
	 * Get the timestamp of the last call to {@link #release()}.
	 * If {@link #release()} was not yet called, get the time when this
	 * instance was created (just like {@link #getCreationTimestamp()} does).
	 * </p>
	 * <p>
	 * Therefore, this method always returns the time when the session was stopped being used
	 * the last time.
	 * </p>
	 * <p>
	 * This timestamp is used
	 * </p>
	 *
	 * @return the timestamp of the last usage of this session.
	 */
	Date getLastUsageTimestamp();

	/**
	 * <p>
	 * Set the {@link #getLastUsageTimestamp() lastUsageTimestamp} to <i>now</i>, i.e. <code>new Date()</code>.
	 * </p>
	 * <p>
	 * This method should be called by {@link CryptoManager#getCryptoSession(String)}.
	 * </p>
	 *
	 * @see #getLastUsageTimestamp()
	 */
	void updateLastUsageTimestamp();

	/**
	 * <p>
	 * Encrypt the given <a href="http://en.wikipedia.org/wiki/Plaintext">plaintext</a>.
	 * </p>
	 * <p>
	 * This method is thread-safe. Thus, implementors should keep in mind that {@link Cipher} is not thread-safe!
	 * </p>
	 *
	 * @param plaintext the unencrypted information (aka <a href="http://en.wikipedia.org/wiki/Plaintext">plaintext</a>) to be encrypted.
	 * @return the encrypted information (aka <a href="http://en.wikipedia.org/wiki/Ciphertext">ciphertext</a>).
	 */
	Ciphertext encrypt(Plaintext plaintext);

	/**
	 * <p>
	 * Decrypt the given <a href="http://en.wikipedia.org/wiki/Ciphertext">ciphertext</a>.
	 * </p>
	 * <p>
	 * This method is thread-safe. Thus, implementors should keep in mind that {@link Cipher} is not thread-safe!
	 * </p>
	 *
	 * @param ciphertext the encrypted information (aka <a href="http://en.wikipedia.org/wiki/Ciphertext">ciphertext</a>) to be decrypted.
	 * @return the unencrypted information (aka <a href="http://en.wikipedia.org/wiki/Plaintext">plaintext</a>).
	 */
	Plaintext decrypt(Ciphertext ciphertext);

	/**
	 * <p>Close the session.</p>
	 * <p>
	 * After closing, the <code>CryptoSession</code> cannot be used for encryption/decryption anymore, i.e.
	 * {@link #encrypt(Plaintext)} and {@link #decrypt(Ciphertext)} very likely throw an exception. The other
	 * methods might still work.
	 * </p>
	 * <p>
	 * This method can be called multiple times - every following call will be silently ignored.
	 * </p>
	 */
	void close();

	/**
	 * Indicate, whether the session was already {@link #close() closed}.
	 * @return <code>true</code>, if {@link #close()} was already called; <code>false</code> otherwise.
	 */
	boolean isClosed();
}
