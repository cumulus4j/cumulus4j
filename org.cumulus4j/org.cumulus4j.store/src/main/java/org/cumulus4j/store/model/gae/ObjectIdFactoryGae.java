package org.cumulus4j.store.model.gae;

import java.util.Locale;

import javax.jdo.identity.StringIdentity;

import org.cumulus4j.store.model.ObjectIdFactory;
import org.cumulus4j.store.reflectionwrapper.gae.KeyFactory;

public class ObjectIdFactoryGae extends ObjectIdFactory {

	/**
	 * @deprecated Do not use this constructor directly! It exists only for the ServiceLoader!
	 * Use {@link ObjectIdFactory#getInstance()} instead!
	 */
	@Deprecated
	public ObjectIdFactoryGae() { }

	@Override
	public Object createObjectId(Class<?> entityClass, long id) {
		String kind = entityClass.getSimpleName().toLowerCase(Locale.UK);
//		Object key = KeyFactory.getInstance().createKey(kind, id).getWrappedObject();
//		return key;
		String keyString = KeyFactory.getInstance().createKeyString(kind, id);
		return new StringIdentity(entityClass, keyString);
	}
}
