package org.cumulus4j.store.model;

public abstract class DetachedClassMetaModel {

	private static final ThreadLocal<DetachedClassMetaModel> threadLocal = new ThreadLocal<DetachedClassMetaModel>();

	public static DetachedClassMetaModel getInstance() {
		return threadLocal.get();
	}

	public static void setInstance(DetachedClassMetaModel detachedClassMetaModel) {
		if (detachedClassMetaModel == null)
			threadLocal.remove();
		else
			threadLocal.set(detachedClassMetaModel);
	}

	public DetachedClassMetaModel() { }

	public abstract ClassMeta getClassMeta(long classID, boolean throwExceptionIfNotFound);

}
