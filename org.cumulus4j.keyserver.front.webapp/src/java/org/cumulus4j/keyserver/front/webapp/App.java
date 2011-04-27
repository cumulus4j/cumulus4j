package org.cumulus4j.keyserver.front.webapp;

import java.io.IOException;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import javax.ws.rs.ApplicationPath;
import javax.ws.rs.core.Application;

@ApplicationPath("/")
public class App
extends Application
{
	private static final Class<?>[] serviceClassesArray = {
		SessionService.class
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
		try {
			if (singletons == null) {
				Set<Object> s = new HashSet<Object>();
				s.add(new SessionSingleton(null)); // TODO make configurable?!
				singletons = Collections.unmodifiableSet(s);
			}

			return singletons;
		} catch (IOException x) {
			throw new RuntimeException(x);
		}
	}
}
