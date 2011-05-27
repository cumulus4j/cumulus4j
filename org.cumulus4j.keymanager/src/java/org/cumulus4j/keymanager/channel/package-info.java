/**
 * <p>
 * {@link org.cumulus4j.keymanager.channel.RequestHandler Handlers} for the communication channel between
 * key manager and application server.
 * </p>
 * <p>
 * The so-called "key manager channel" is - as shown in the document
 * <a href="http://www.cumulus4j.org/documentation/deployment-scenarios.html">Deployment scenarios</a> - an
 * HTTP(S) connection from the key-manager to the application server with an inverse request-response-cycle.
 * This means, the application server sends a {@link org.cumulus4j.keymanager.back.shared.Request},
 * the key manager handles it and then sends a {@link org.cumulus4j.keymanager.back.shared.Response} back.
 * </p>
 * <p>
 * The classes in this package are the handlers responsible for processing these requests.
 * </p>
 */
package org.cumulus4j.keymanager.channel;
