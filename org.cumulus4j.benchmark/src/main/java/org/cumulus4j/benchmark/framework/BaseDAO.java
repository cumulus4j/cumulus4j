package org.cumulus4j.benchmark.framework;

import java.util.Map;

public class BaseDAO<T> {

	private static Map<Class<?>, BaseDAO<?>> instanceMap = new java.util.HashMap<Class<?>, BaseDAO<?>>();

	protected static <T> void registerInstance(BaseDAO<T> dao, Class<?> clazz){
		instanceMap.put(clazz, dao);
	}

	protected static synchronized <T> BaseDAO<?> getInstance(Class<T> clazz){

		BaseDAO<?> instance = instanceMap.get(clazz);

		return instance;

	}

	public static synchronized <T> BaseDAO<?> sharedInstance(Class<T> clazz) {

		BaseDAO<?> instance = instanceMap.get(clazz);

		if (instance != null) {
			return instance;
		}

		try {
			instance = new BaseDAO<T>();
		}
		catch (ClassCastException exc) {
			throw new IllegalArgumentException(exc);
		}

		instanceMap.put(clazz, instance);

		return instance;
	}

	protected BaseDAO(){}

}