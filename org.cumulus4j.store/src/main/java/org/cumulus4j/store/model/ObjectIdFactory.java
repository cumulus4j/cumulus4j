package org.cumulus4j.store.model;

import java.util.Iterator;
import java.util.ServiceLoader;

import javax.jdo.identity.LongIdentity;

public class ObjectIdFactory {

	private static ObjectIdFactory instance;
	static {
		ServiceLoader<ObjectIdFactory> serviceLoader = ServiceLoader.load(ObjectIdFactory.class);
		Iterator<ObjectIdFactory> it = serviceLoader.iterator();
		if (!it.hasNext())
			throw new IllegalStateException("No ObjectIdFactory service registered!");

		ObjectIdFactory objectIdFactory = it.next();

		if (it.hasNext())
			throw new IllegalStateException("Multiple ObjectIdFactory services registered!");

		instance = objectIdFactory;
	}

	public static ObjectIdFactory getInstance() {
		return instance;
	}

	/**
	 * @deprecated Do not use this constructor directly! It exists only for the ServiceLoader!
	 * Use {@link #getInstance()} instead!
	 */
	@Deprecated
	public ObjectIdFactory() { }

	public Object createObjectId(Class<?> entityClass, long id) {
		LongIdentity identity = new LongIdentity(entityClass, id);
		return identity;
	}

}
