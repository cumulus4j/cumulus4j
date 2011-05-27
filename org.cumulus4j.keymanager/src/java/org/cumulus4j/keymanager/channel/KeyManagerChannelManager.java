package org.cumulus4j.keymanager.channel;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.cumulus4j.keymanager.SessionManager;
import org.cumulus4j.keymanager.back.shared.GetActiveEncryptionKeyRequest;
import org.cumulus4j.keymanager.back.shared.GetKeyRequest;
import org.cumulus4j.keymanager.back.shared.Request;

/**
 * <p>
 * Manager for the communication channel between key manager and application server.
 * </p>
 * <p>
 * The so-called "key manager channel" is - as shown in the document
 * <a href="http://www.cumulus4j.org/documentation/deployment-scenarios.html">Deployment scenarios</a> - an
 * HTTP(S) connection from the key-manager to the application server with an inverse request-response-cycle.
 * This means, the application server sends a {@link org.cumulus4j.keymanager.back.shared.Request},
 * the key manager handles it and then sends a {@link org.cumulus4j.keymanager.back.shared.Response} back.
 * </p>
 *
 * @author Marco หงุ่ยตระกูล-Schulze - marco at nightlabs dot de
 */
public class KeyManagerChannelManager
{
	private SessionManager sessionManager;
	private URL appServerBaseURL;
	private URL keyManagerChannelURL;
	private int desiredThreadCount;

	private Set<KeyManagerChannelListenerThread> listenerThreads = Collections.synchronizedSet(new HashSet<KeyManagerChannelListenerThread>());

	private static final Map<Class<? extends Request>, Class<? extends RequestHandler<?>>> requestClass2handlerClass;
	static {
		Map<Class<? extends Request>, Class<? extends RequestHandler<?>>> m = new HashMap<Class<? extends Request>, Class<? extends RequestHandler<?>>>();
		m.put(GetKeyRequest.class, GetKeyRequestHandler.class);
		m.put(GetActiveEncryptionKeyRequest.class, GetActiveEncryptionKeyRequestHandler.class);
		requestClass2handlerClass = Collections.unmodifiableMap(m);
	}

	/**
	 * Instantiate a <code>KeyManagerChannelManager</code>.
	 *
	 * @param sessionManager the {@link SessionManager} which
	 * @param appServerBaseURL the base-URL before the "/KeyManagerChannel" - e.g. if the REST URL of the KeyManagerChannel-service is
	 * "https://serverUsingCumulus4j.mydomain.org/org.cumulus4j.keymanager.back.webapp/KeyManagerChannel", then this must be
	 * "https://serverUsingCumulus4j.mydomain.org/org.cumulus4j.keymanager.back.webapp".
	 */
	public KeyManagerChannelManager(SessionManager sessionManager, URL appServerBaseURL)
	{
		if (sessionManager == null)
			throw new IllegalArgumentException("sessionManager == null");

		if (appServerBaseURL == null)
			throw new IllegalArgumentException("appServerBaseURL == null");

		this.sessionManager = sessionManager;

		this.appServerBaseURL = appServerBaseURL;

		try {
			String s = appServerBaseURL.toString();
			if (!s.endsWith("/"))
				s += '/';

			this.keyManagerChannelURL = new URL(s + "KeyManagerChannel");
		} catch (MalformedURLException e) {
			throw new RuntimeException(e);
		}

		setDesiredThreadCount(5); // TODO make this manage itself automatically according to load statistics
	}

	/**
	 * Get the {@link SessionManager} that was passed in the constructor.
	 * @return the {@link SessionManager}.
	 */
	public SessionManager getSessionManager() {
		return sessionManager;
	}

	/**
	 * Get the base-URL before the "/KeyManagerChannel" - e.g. if the REST URL of the KeyManagerChannel-service is
	 * "https://serverUsingCumulus4j.mydomain.org/org.cumulus4j.keymanager.back.webapp/KeyManagerChannel", then this must be
	 * "https://serverUsingCumulus4j.mydomain.org/org.cumulus4j.keymanager.back.webapp".
	 * @return the base-URL before the "/KeyManagerChannel".
	 */
	public URL getAppServerBaseURL() {
		return appServerBaseURL;
	}

	/**
	 * Get the complete URL to the <code>KeyManagerChannel</code>.
	 *
	 * @return the complete URL to the <code>KeyManagerChannel</code>.
	 */
	public URL getKeyManagerChannelURL() {
		return keyManagerChannelURL;
	}

	/**
	 * <p>
	 * Set the quantity of {@link KeyManagerChannelListenerThread}s that should be running for this
	 * {@link KeyManagerChannelManager}.
	 * </p>
	 * <p>
	 * If the given <code>desiredThreadCount</code> is greater than
	 * the number of currently running threads, new threads are created. If the <code>desiredThreadCount</code>
	 * is less than the number of currently running threads, some of the threads will terminate themselves
	 * until the number of currently running threads matches the desired quantity.
	 * </p>
	 * @param desiredThreadCount the new quantity of {@link KeyManagerChannelListenerThread}s which should be
	 * active for this {@link KeyManagerChannelManager}.
	 * @see #getDesiredThreadCount()
	 */
	public void setDesiredThreadCount(int desiredThreadCount) {
		this.desiredThreadCount = desiredThreadCount;
		while (listenerThreads.size() < desiredThreadCount) {
			KeyManagerChannelListenerThread thread = new KeyManagerChannelListenerThread(this);
			listenerThreads.add(thread);
			thread.start();
		}
	}

	/**
	 * Get the quantity of {@link KeyManagerChannelListenerThread}s that should be running for this
	 * {@link KeyManagerChannelManager}.
	 * @return the quantity of {@link KeyManagerChannelListenerThread}s that should be active for this
	 * {@link KeyManagerChannelManager}.
	 * @see #setDesiredThreadCount(int)
	 */
	public int getDesiredThreadCount() {
		return desiredThreadCount;
	}

	/**
	 * <p>
	 * Unregister the given <code>thread</code>, if there are currently more threads running than desired.
	 * </p>
	 * <p>
	 * This method is called by a {@link KeyManagerChannelListenerThread} in its run-loop to determine, if the thread
	 * should terminate itself (see {@link #setDesiredThreadCount(int)}). If the method returns <code>true</code>,
	 * the thread will exit its {@link Thread#run() run()} method.
	 * </p>
	 * @param thread the thread.
	 * @return <code>true</code> if the thread was unregistered and thus must exit its <code>run()</code> method;
	 * <code>false</code> if the thread was not unregistered and should thus continue.
	 */
	protected boolean unregisterThreadIfMoreThanDesiredThreadCount(KeyManagerChannelListenerThread thread)
	{
		synchronized (listenerThreads) {
			if (listenerThreads.size() > desiredThreadCount) {
				listenerThreads.remove(thread);
				return true;
			}
			else
				return false;
		}
	}

	/**
	 * Get the appropriate {@link RequestHandler handler} for the given <code>request</code>.
	 *
	 * @param <R> the type of the <code>request</code>.
	 * @param request the request.
	 * @return the {@link RequestHandler} for the request.
	 * @throws InstantiationException if {@link Class#newInstance()} failed to create the handler instance.
	 * @throws IllegalAccessException if {@link Class#newInstance()} failed to create the handler instance.
	 */
	protected <R extends Request> RequestHandler<R> getRequestHandler(R request)
	throws InstantiationException, IllegalAccessException
	{
		if (request == null)
			throw new IllegalArgumentException("request == null");

		Class<? extends Request> requestClass = request.getClass();
		Class<? extends RequestHandler<?>> handlerClass = requestClass2handlerClass.get(requestClass);
		if (handlerClass == null)
			throw new IllegalStateException("There is no RequestHandler class registered for this requestClass: " + requestClass);

		@SuppressWarnings("unchecked")
		RequestHandler<R> requestHandler = (RequestHandler<R>) handlerClass.newInstance();
		requestHandler.setKeyManagerChannelManager(this);
		return requestHandler;
	}
}
