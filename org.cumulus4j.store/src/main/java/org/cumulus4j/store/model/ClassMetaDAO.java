package org.cumulus4j.store.model;

import javax.jdo.JDOObjectNotFoundException;
import javax.jdo.PersistenceManager;

public class ClassMetaDAO extends AbstractDAO {

	public ClassMetaDAO() { }

	public ClassMetaDAO(PersistenceManager pm) {
		super(pm);
	}

	public ClassMeta getClassMeta(String packageName, String simpleClassName, boolean throwExceptionIfNotFound)
	{
		javax.jdo.Query q = pm.newNamedQuery(ClassMeta.class, ClassMeta.NamedQueries.getClassMetaByPackageNameAndSimpleClassName);
		ClassMeta result = (ClassMeta) q.execute(packageName, simpleClassName);

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
}
