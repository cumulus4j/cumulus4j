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
	 *
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

	public SessionManager getSessionManager() {
		return sessionManager;
	}

	public URL getAppServerBaseURL() {
		return appServerBaseURL;
	}

	public URL getKeyManagerChannelURL() {
		return keyManagerChannelURL;
	}

	public void setDesiredThreadCount(int desiredThreadCount) {
		this.desiredThreadCount = desiredThreadCount;
		while (listenerThreads.size() < desiredThreadCount) {
			KeyManagerChannelListenerThread thread = new KeyManagerChannelListenerThread(this);
			listenerThreads.add(thread);
			thread.start();
		}
	}

	public int getDesiredThreadCount() {
		return desiredThreadCount;
	}

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
