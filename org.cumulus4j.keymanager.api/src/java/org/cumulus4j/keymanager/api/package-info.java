/**
 * <p>
 * API for accessing the key manager.
 * </p><p>
 * The entry point into this API is the {@link org.cumulus4j.keymanager.api.KeyManagerAPI}. It provides a generic way
 * to manage a {@link org.cumulus4j.keystore.KeyStore} either in the local file system or on a remote key-server
 * (as shown in <a href="../documentation/deployment-scenarios.html">Deployment scenarios</a>).
 * </p><p>
 * An application server using Cumulus4j is only able to read or write data, when the key manager grants access to
 * keys. In order to control this access, crypto-sessions are used (not to be confused with a servlet's session):
 * An application server can only request a key from a key manager, when the crypto-session exists and is unlocked.
 * Usually, a client will first unlock the session, then send a request to the app server and when the app server responded,
 * lock the session, again. Thus most of the time, a key manager will reject access to keys, even while a connection
 * between app server and key manager exists.
 * </p>
 */
package org.cumulus4j.keymanager.api;
