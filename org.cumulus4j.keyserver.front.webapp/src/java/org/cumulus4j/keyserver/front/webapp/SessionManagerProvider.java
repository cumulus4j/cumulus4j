package org.cumulus4j.keyserver.front.webapp;

import javax.ws.rs.core.Context;
import javax.ws.rs.ext.Provider;

import com.sun.jersey.spi.inject.SingletonTypeInjectableProvider;

@Provider
public class SessionManagerProvider
extends SingletonTypeInjectableProvider<Context, SessionManager>
{
	public SessionManagerProvider(SessionManager sessionManager) {
		super(SessionManager.class, sessionManager);
	}
}
