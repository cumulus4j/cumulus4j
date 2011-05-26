/**
 * <p>
 * Broker transmitting messages between application-server and key-manager.
 * </p>
 * <p>
 * As documented in <a href="http://cumulus4j.org/documentation/deployment-scenarios.html">Deployment scenarios</a>,
 * TCP connections are always established from the key-manager (i.e. client or key-server) to the application server.
 * Since this means that the key-exchange-request-response-cycle works opposite the HTTP-request-response-cycle,
 * we need the {@link org.cumulus4j.store.crypto.keymanager.messagebroker.MessageBroker}.
 * </p>
 */
package org.cumulus4j.store.crypto.keymanager.messagebroker;
