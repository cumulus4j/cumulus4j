package org.cumulus4j.store.model;

import javax.jdo.JDOObjectNotFoundException;
import javax.jdo.PersistenceManager;
import javax.jdo.Query;
import javax.jdo.identity.LongIdentity;

public class ClassMetaDAO extends AbstractDAO {

	public ClassMetaDAO() { }

	public ClassMetaDAO(PersistenceManager pm) {
		super(pm);
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
		LongIdentity identity = new LongIdentity(ClassMeta.class, classID);
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
		Query q = pm.newNamedQuery(EmbeddedClassMeta.class, EmbeddedClassMeta.NamedQueries.getEmbeddedClassMetaByEmbeddingFieldMeta);
		EmbeddedClassMeta result = (EmbeddedClassMeta) q.execute(embeddingFieldMeta);

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
