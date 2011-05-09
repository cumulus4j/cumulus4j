package org.cumulus4j.store.crypto.keymanager.rest;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import javax.ws.rs.ApplicationPath;
import javax.ws.rs.core.Application;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Marco หงุ่ยตระกูล-Schulze - marco at nightlabs dot de
 */
@ApplicationPath("/org.cumulus4j.keyserver.back.webapp")
public class App
extends Application
{
	private static final Logger logger = LoggerFactory.getLogger(App.class);

	private static final Class<?>[] serviceClassesArray = {
		KeyServerChannelService.class
	};

	private static final Set<Class<?>> serviceClassesSet;
	static {
		Set<Class<?>> s = new HashSet<Class<?>>(serviceClassesArray.length);
		for (Class<?> c : serviceClassesArray)
			s.add(c);

		serviceClassesSet = Collections.unmodifiableSet(s);
	}

	@Override
	public Set<Class<?>> getClasses() {
		return serviceClassesSet;
	}

	private Set<Object> singletons;

	@Override
	public Set<Object> getSingletons()
	{
		if (singletons == null) {
			Set<Object> s = new HashSet<Object>();
//			s.add(new KeyStoreProvider(keyStore));
//			s.add(new SessionManagerProvider(new SessionManager(keyStore)));
			singletons = Collections.unmodifiableSet(s);
		}

		return singletons;
	}
}
