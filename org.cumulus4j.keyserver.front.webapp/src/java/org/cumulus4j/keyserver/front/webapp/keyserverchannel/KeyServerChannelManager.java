package org.cumulus4j.keyserver.front.webapp.keyserverchannel;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.cumulus4j.keyserver.back.shared.GetKeyRequest;
import org.cumulus4j.keyserver.back.shared.Request;
import org.cumulus4j.keyserver.front.webapp.SessionManager;

public class KeyServerChannelManager
{
	private SessionManager sessionManager;
	private URL appServerBaseURL;
	private URL keyServerChannelURL;
	private int desiredThreadCount;
	private String keyServerID;

	private Set<KeyServerChannelListenerThread> listenerThreads = Collections.synchronizedSet(new HashSet<KeyServerChannelListenerThread>());

	private static final Map<Class<? extends Request>, Class<? extends RequestHandler<?>>> requestClass2handlerClass;
	static {
		Map<Class<? extends Request>, Class<? extends RequestHandler<?>>> m = new HashMap<Class<? extends Request>, Class<? extends RequestHandler<?>>>();
		m.put(GetKeyRequest.class, GetKeyRequestHandler.class);
		requestClass2handlerClass = Collections.unmodifiableMap(m);
	}

	/**
	 *
	 * @param appServerBaseURL the base-URL before the "/KeyServerChannel" - e.g. if the REST URL of the KeyServerChannel-service is
	 * "https://serverUsingCumulus4j.mydomain.org/org.cumulus4j.keyserver.back.webapp/KeyServerChannel", then this must be
	 * "https://serverUsingCumulus4j.mydomain.org/org.cumulus4j.keyserver.back.webapp".
	 */
	public KeyServerChannelManager(SessionManager sessionManager, URL appServerBaseURL)
	{
		if (sessionManager == null)
			throw new IllegalArgumentException("sessionManager == null");

		if (appServerBaseURL == null)
			throw new IllegalArgumentException("appServerBaseURL == null");

		this.sessionManager = sessionManager;

		this.appServerBaseURL = appServerBaseURL;
		try {
			this.keyServerChannelURL = new URL(appServerBaseURL, "KeyServerChannel");
		} catch (MalformedURLException e) {
			throw new RuntimeException(e);
		}

		this.keyServerID = sessionManager.getKeyServerID();

		setDesiredThreadCount(10); // TODO make this manage itself automatically according to load statistics
	}

	public SessionManager getSessionManager() {
		return sessionManager;
	}

	public URL getAppServerBaseURL() {
		return appServerBaseURL;
	}

	public URL getKeyServerChannelURL() {
		return keyServerChannelURL;
	}

	public String getKeyServerID() {
		return keyServerID;
	}

	public void setDesiredThreadCount(int desiredThreadCount) {
		this.desiredThreadCount = desiredThreadCount;
		while (listenerThreads.size() < desiredThreadCount) {
			KeyServerChannelListenerThread thread = new KeyServerChannelListenerThread(this);
			listenerThreads.add(thread);
			thread.start();
		}
	}

	public int getDesiredThreadCount() {
		return desiredThreadCount;
	}

	protected boolean unregisterThreadIfMoreThanDesiredThreadCount(KeyServerChannelListenerThread thread)
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
		requestHandler.setKeyServerChannelManager(this);
		return requestHandler;
	}
}
