package org.cumulus4j.store.crypto.keymanager.messagebroker;

import java.util.concurrent.TimeoutException;

import org.cumulus4j.keymanager.back.shared.ErrorResponse;
import org.cumulus4j.keymanager.back.shared.NullResponse;
import org.cumulus4j.keymanager.back.shared.Request;
import org.cumulus4j.keymanager.back.shared.Response;
import org.cumulus4j.store.crypto.keymanager.rest.ErrorResponseException;

/**
 * <p>
 * Broker transmitting messages between application-server and key-manager.
 * </p>
 * <p>
 * As documented in <a href="http://cumulus4j.org/documentation/deployment-scenarios.html">Deployment scenarios</a>,
 * TCP connections are always established from the key-manager (i.e. client or key-server) to the application server.
 * Since this means that the key-exchange-request-response-cycle works opposite the HTTP-request-response-cycle,
 * we need this <code>MessageBroker</code>.
 * </p>
 * <p>
 * Within every JVM, there is one single {@link MessageBrokerRegistry#getActiveMessageBroker() active MessageBroker}.
 * This instance must make sure that messages can be exchanged from every cluster-node to every key-manager; i.e. if
 * the key-manager connects to a different cluster-node than the primary connection (established by the application logic),
 * the {@link Request}s must be proxied over the right cluster-node to the key-manager. The {@link Response} must
 * of course be routed appropriately back to the correct cluster-node.
 * </p>
 * <p>
 * <img src="http://cumulus4j.org/images/deployment-scenario/deployment-scenario-without-keyserver-with-cluster.png" />
 * </p>
 *
 * @author Marco หงุ่ยตระกูล-Schulze - marco at nightlabs dot de
 */
public interface MessageBroker
{
//	ActiveKeyManagerChannelRegistration registerActiveKeyManagerChannel(String cryptoSessionIDPrefix, String internalKeyManagerChannelURL);
//
//	void unregisterActiveKeyManagerChannel(ActiveKeyManagerChannelRegistration registration);

	/**
	 * Send <code>request</code> to the key-manager (embedded in client or separate in key-server) and return its response.
	 * @param responseClass the type of the expected response.
	 * @param request the request to be sent to the key-manager.
	 * @return the response from the key-manager. Will be <code>null</code>, if the key-manager replied with a {@link NullResponse}
	 * (if valid for the given request).
	 * @throws TimeoutException if the request was not replied within the query-timeout.
	 * @throws ErrorResponseException if the key-manager (either running embedded on the remote client or
	 * in a separate key-server) sent an {@link ErrorResponse}.
	 */
	<R extends Response> R query(Class<R> responseClass, Request request)
	throws TimeoutException, ErrorResponseException;

	Request pollRequestForProcessing(String cryptoSessionIDPrefix);

	void pushResponse(Response response);
}
