package org.cumulus4j.store.model;

import java.util.Collection;
import java.util.Map;

import javax.jdo.JDOObjectNotFoundException;
import javax.jdo.PersistenceManager;
import javax.jdo.Query;
import javax.jdo.identity.LongIdentity;

public class ClassMetaDAO extends AbstractDAO {

	public ClassMetaDAO() { }

	public ClassMetaDAO(PersistenceManager pm) {
		super(pm);
	}

	public static String getMultiClassMetaOrFilterPart(Map<String, Object> queryParams, Collection<ClassMeta> classMetas) {
		StringBuilder result = new StringBuilder();

		if (classMetas.size() > 1)
			result.append('(');

		int idx = -1;
		for (ClassMeta classMeta : classMetas) {
			if (++idx > 0)
				result.append(" || ");

			String classMetaClassIDParamName = "classMeta_classID" + idx;
			result.append("this.classMeta_classID == :").append(classMetaClassIDParamName);
			queryParams.put(classMetaClassIDParamName, classMeta.getClassID());
		}

		if (classMetas.size() > 1)
			result.append(')');

		return result.toString();
	}

	public ClassMeta getClassMeta(String packageName, String simpleClassName, boolean throwExceptionIfNotFound)
	{
		Query q = pm.newNamedQuery(ClassMeta.class, ClassMeta.NamedQueries.getClassMetaByPackageNameAndSimpleClassName);
		ClassMeta result = (ClassMeta) q.execute(ClassMeta.UNIQUE_SCOPE_CLASS_META, packageName, simpleClassName);

		if (result == null && throwExceptionIfNotFound)
			throw new JDOObjectNotFoundException(
					"No ClassMeta found for packageName=\"" + packageName + "\" and simpleClassName=\"" + simpleClassName + "\"!"
			);

		return result;
	}

	public ClassMeta getClassMeta(Class<?> clazz, boolean throwExceptionIfNotFound)
	{
		String packageName = clazz.getPackage() == null ? "" : clazz.getPackage().getName();
		String simpleClassName = clazz.getSimpleName();
		return getClassMeta(packageName, simpleClassName, throwExceptionIfNotFound);
	}

	public ClassMeta getClassMeta(long classID, boolean throwExceptionIfNotFound)
	{
		Object identity = new LongIdentity(ClassMeta.class, classID);
		try {
			ClassMeta classMeta = (ClassMeta) pm.getObjectById(identity);
			return classMeta;
		} catch (JDOObjectNotFoundException x) {
			if (throwExceptionIfNotFound)
				throw x;
			else
				return null;
		}
	}

	public EmbeddedClassMeta getEmbeddedClassMeta(FieldMeta embeddingFieldMeta, boolean throwExceptionIfNotFound) {
		Query q = pm.newNamedQuery(EmbeddedClassMeta.class, EmbeddedClassMeta.NamedQueries.getEmbeddedClassMetaByEmbeddingFieldMeta_fieldID);
		EmbeddedClassMeta result = (EmbeddedClassMeta) q.execute(embeddingFieldMeta.getFieldID());

//		EmbeddedClassMeta result;
//		@SuppressWarnings("unchecked")
//		Collection<EmbeddedClassMeta> c = (Collection<EmbeddedClassMeta>) q.execute(embeddingFieldMeta);
//		if (c.size() == 1)
//			result = c.iterator().next();
//		else if (c.isEmpty())
//			result = null;
//		else
//			throw new IllegalStateException("Found multiple EmbeddeClassMeta instances: " + c);

		if (result == null && throwExceptionIfNotFound)
			throw new JDOObjectNotFoundException(
					"No EmbeddedClassMeta found with embeddingFieldMeta=\"" + embeddingFieldMeta + "\"!"
			);

		return result;
	}
}
