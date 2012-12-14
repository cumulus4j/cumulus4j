package org.cumulus4j.store.reflectionwrapper.gae;

import org.cumulus4j.store.reflectionwrapper.ReflectionWrapper;
import org.cumulus4j.store.reflectionwrapper.ReflectionWrapperException;

public class Key extends ReflectionWrapper {

	protected Key(KeyFactory keyFactory, Object wrappedObject) {
		super(keyFactory, wrappedObject);
	}

	public long getId() {
		Object result = invoke(1, "getId");

		if (!(result instanceof Long))
			throw new ReflectionWrapperException("getId() did not return an instance of Long, but: " + result);

		return (Long) result;
	}

}
