package org.cumulus4j.store.reflectionwrapper.gae;

import org.cumulus4j.store.reflectionwrapper.ReflectionWrapper;

public class KeyFactory extends ReflectionWrapper {

	private static KeyFactory instance;

	protected KeyFactory(ClassLoader classLoader) {
		super(classLoader);
	}

	public static KeyFactory getInstance() {
		if (instance == null)
			instance = new KeyFactory(KeyFactory.class.getClassLoader());

		return instance;
	}

	@Override
	protected String getWrappedClassName() {
		return "com.google.appengine.api.datastore.KeyFactory";
	}

	@Override
	protected Object createWrappedObject(Class<?> wrappedClass) {
		throw new UnsupportedOperationException(String.format("The class %s cannot be instantiated! It is solely used in a static way.", getWrappedClassName()));
	}

	public Key stringToKey(String encoded) {
		Object wrappedKey = invokeStatic(
				1, "stringToKey",
				String.class,
				encoded
		);

		if (wrappedKey == null)
			throw new IllegalStateException("stringToKey(String) returned null! invocationTargetClass=" + getWrappedClassName());

		return new Key(this, wrappedKey);
	}

	public Key createKey(String kind, long id) {
		Object wrappedKey = invokeStatic(
				2, "createKey",
				String.class,
				long.class,
				kind,
				id
		);

		if (wrappedKey == null)
			throw new IllegalStateException("createKey(String, long) returned null! invocationTargetClass=" + getWrappedClassName());

		return new Key(this, wrappedKey);
	}

	public String createKeyString(String kind, long id) {
		String keyString = (String) invokeStatic(
				3, "createKeyString",
				String.class,
				long.class,
				kind,
				id
		);

		if (keyString == null)
			throw new IllegalStateException("createKeyString(String, long) returned null! invocationTargetClass=" + getWrappedClassName());

		return keyString;
	}
}
