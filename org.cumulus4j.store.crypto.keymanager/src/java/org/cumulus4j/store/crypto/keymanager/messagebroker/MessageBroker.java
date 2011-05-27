package org.cumulus4j.store.crypto.keymanager.messagebroker;

import java.util.concurrent.TimeoutException;

import org.cumulus4j.keymanager.back.shared.ErrorResponse;
import org.cumulus4j.keymanager.back.shared.GetKeyRequest;
import org.cumulus4j.keymanager.back.shared.GetKeyResponse;
import org.cumulus4j.keymanager.back.shared.Message;
import org.cumulus4j.keymanager.back.shared.NullResponse;
import org.cumulus4j.keymanager.back.shared.Request;
import org.cumulus4j.keymanager.back.shared.Response;
import org.cumulus4j.store.crypto.CryptoSession;
import org.cumulus4j.store.crypto.keymanager.rest.ErrorResponseException;

/**
 * <p>
 * Broker transmitting {@link Message messages} between application-server and key-manager.
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
 * of course be routed appropriately back to the correct cluster-node:
 * </p>
 * <p>
 * <img src="http://www.cumulus4j.org/images/deployment-scenario/deployment-scenario-without-keyserver-with-cluster.png" />
 * </p>
 * <p>
 * <b>Important:</b> You should not directly implement this interface but instead subclass {@link AbstractMessageBroker}!
 * </p>
 *
 * @author Marco หงุ่ยตระกูล-Schulze - marco at nightlabs dot de
 */
public interface MessageBroker
{
	/**
	 * <p>
	 * System property to control the timeout (in milliseconds) for the {@link #query(Class, Request)} method.
	 * </p>
	 * <p>
	 * The <code>query(...)</code> method will throw a {@link TimeoutException}, if no {@link Response}
	 * to a given {@link Request} arrived within this timeout.
	 * </p>
	 * <p>
	 * If the system property is not present or not a valid number, it is up to the <code>MessageBroker</code>
	 * implementation, what default value should be used. See {@link AbstractMessageBroker#getQueryTimeout()}
	 * for the default implemented there.
	 * </p>
	 */
	static final String SYSTEM_PROPERTY_QUERY_TIMEOUT = "cumulus4j.MessageBroker.queryTimeout";

	/**
	 * <p>
	 * System property to control the timeout (in milliseconds) for the {@link #pollRequest(String)} method.
	 * </p>
	 * <p>
	 * The <code>pollRequest(...)</code> method returns <code>null</code>, if no {@link Request} popped up
	 * in the to-do-queue within the timeout.
	 * </p>
	 * <p>
	 * If the system property is not present or not a valid number, it is up to the <code>MessageBroker</code>
	 * implementation, what default value should be used. See {@link AbstractMessageBroker#getPollRequestTimeout()}
	 * for the default implemented there.
	 * </p>
	 */
	static final String SYSTEM_PROPERTY_POLL_REQUEST_TIMEOUT = "cumulus4j.MessageBroker.pollRequestTimeout";

//	ActiveKeyManagerChannelRegistration registerActiveKeyManagerChannel(String cryptoSessionIDPrefix, String internalKeyManagerChannelURL);
//
//	void unregisterActiveKeyManagerChannel(ActiveKeyManagerChannelRegistration registration);

	/**
	 * <p>
	 * Send <code>request</code> to the key-manager (embedded in client or separate in key-server) and return its response.
	 * </p>
	 * <p>
	 * This method is used for example by a {@link CryptoSession} to request keys via a {@link GetKeyRequest}. As soon as
	 * this method entered with the <code>request</code>, it is expected that the {@link #pollRequest(String)} returns
	 * this <code>request</code> to the appropriate key-manager. The <code>query(...)</code> method blocks then until
	 * the key-manager handled the <code>request</code> and sent a {@link GetKeyResponse} back. As soon as the <code>response</code>
	 * was {@link #pushResponse(Response) pushed} into the <code>MessageBroker</code>, <code>query(...)</code> should return it.
	 * </p>
	 * <p>
	 * If the expected {@link Response} does not arrive within the query-timeout (configurable via
	 * system property {@value #SYSTEM_PROPERTY_QUERY_TIMEOUT}), this method should throw
	 * a {@link TimeoutException}.
	 * </p>
	 *
	 * @param responseClass the type of the expected response.
	 * @param request the request to be sent to the key-manager.
	 * @return the response from the key-manager. Will be <code>null</code>, if the key-manager replied with a {@link NullResponse}
	 * (if valid for the given request).
	 * @throws TimeoutException if the request was not replied within the {@link #SYSTEM_PROPERTY_QUERY_TIMEOUT query-timeout}.
	 * @throws ErrorResponseException if the key-manager (either running embedded on the remote client or
	 * in a separate key-server) sent an {@link ErrorResponse}.
	 */
	<R extends Response> R query(Class<R> responseClass, Request request)
	throws TimeoutException, ErrorResponseException;

	/**
	 * <p>
	 * Poll the next {@link Request} that is waiting to be processed.
	 * </p>
	 * <p>
	 * This method is - indirectly via a REST web-service - called by the key-manager periodically
	 * in order to receive requests. If there is a request waiting, this method should immediately
	 * return it. If there is no request in the queue, this method should wait for an incoming
	 * request for a short time. If there is still no request available after a short blocking time,
	 * this method should return <code>null</code> (before the remote client would timeout).
	 * </p>
	 * <p>
	 * Usually, blocking about 1 minute is recommended in most situations. However, when
	 * using certain runtimes, it must be much shorter  (e.g. the Google App Engine allows
	 * requests not to take longer than 30 sec, thus 20 sec are an appropriate time to stay safe).
	 * </p>
	 * <p>
	 * Additionally, since the remote key-manager must wait at maximum this time, its HTTP-client's
	 * timeout must be longer than this timeout.
	 * </p>
	 * <p>
	 * It should be possible to configure this timeout via the system property
	 * {@value #SYSTEM_PROPERTY_POLL_REQUEST_TIMEOUT}. Implementors should use
	 * {@link AbstractMessageBroker#getPollRequestTimeout()} for this purpose.
	 * </p>
	 * @param cryptoSessionIDPrefix usually, every key-manager uses the same prefix for
	 * all crypto-sessions. Thus, this prefix is used to efficiently route requests to
	 * the right key-manager.
	 * @return
	 */
	Request pollRequest(String cryptoSessionIDPrefix);

	/**
	 * <p>
	 * Push a {@link Response} in order to reply a previous request.
	 * </p>
	 * <p>
	 * This method is - indirectly via a REST web-service - called by the key-manager after
	 * it successfully handled a {@link Request}.
	 * </p>
	 * @param response the response answering a previous {@link Request} enqueued by {@link #query(Class, Request)}.
	 */
	void pushResponse(Response response);
}
