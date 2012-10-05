package org.cumulus4j.howto;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import javax.ws.rs.ApplicationPath;
import javax.ws.rs.core.Application;

/**
 * @author Marco หงุ่ยตระกูล-Schulze - marco at nightlabs dot de
 */
@ApplicationPath("/App")
public class App extends Application {
	private static final Class<?>[] serviceClassesArray = {
			org.cumulus4j.howto.services.DatanucleusService.class,
			org.cumulus4j.howto.services.DummyKeyManagerService.class,
			org.cumulus4j.howto.services.Cumulus4jKeystoreService.class};

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
	public Set<Object> getSingletons() {
		if (singletons == null) {
			Set<Object> s = new HashSet<Object>();
			singletons = Collections.unmodifiableSet(s);
		}

		return singletons;
	}
}