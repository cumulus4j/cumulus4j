package org.cumulus4j.store.model;

import java.util.HashMap;
import java.util.Map;

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

	private Map<Long, ClassMeta> classID2ClassMeta = new HashMap<Long, ClassMeta>();
	private Map<Long, FieldMeta> fieldID2FieldMeta = new HashMap<Long, FieldMeta>();

	public DetachedClassMetaModel() { }

	public final ClassMeta getClassMeta(long classID, boolean throwExceptionIfNotFound) {
		ClassMeta result = classID2ClassMeta.get(classID);
		if (result != null)
			return result;

		return getClassMetaImpl(classID, throwExceptionIfNotFound);
	}

	public final FieldMeta getFieldMeta(long fieldID, boolean throwExceptionIfNotFound) {
		FieldMeta result = fieldID2FieldMeta.get(fieldID);
		if (result != null)
			return result;

		return getFieldMetaImpl(fieldID, throwExceptionIfNotFound);
	}

	protected abstract ClassMeta getClassMetaImpl(long classID, boolean throwExceptionIfNotFound);

	protected abstract FieldMeta getFieldMetaImpl(long fieldID, boolean throwExceptionIfNotFound);

	public void registerClassMetaCurrentlyDetaching(ClassMeta classMeta) {
		classID2ClassMeta.put(classMeta.getClassID(), classMeta);
	}

	public void registerFieldMetaCurrentlyDetaching(FieldMeta fieldMeta) {
		fieldID2FieldMeta.put(fieldMeta.getFieldID(), fieldMeta);
	}

}
