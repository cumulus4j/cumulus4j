package org.cumulus4j.store.model;

import java.util.Collection;

import javax.jdo.JDOObjectNotFoundException;
import javax.jdo.PersistenceManager;
import javax.jdo.identity.LongIdentity;

import org.cumulus4j.store.model.FieldMeta.NamedQueries;

public class FieldMetaDAO extends AbstractDAO {

	public FieldMetaDAO() { }

	public FieldMetaDAO(PersistenceManager pm) {
		super(pm);
	}

	public Collection<FieldMeta> getFieldMetasForClassMeta(ClassMeta classMeta) {
		if (classMeta == null)
			throw new IllegalArgumentException("classMeta == null");

		javax.jdo.Query query = pm.newNamedQuery(FieldMeta.class, NamedQueries.getFieldMetasForClassMeta_classID);
		@SuppressWarnings("unchecked")
		Collection<FieldMeta> result = (Collection<FieldMeta>) query.execute(classMeta.getClassID());
		return result;
	}

	public Collection<FieldMeta> getSubFieldMetasForFieldMeta(FieldMeta fieldMeta) {
		if (fieldMeta == null)
			throw new IllegalArgumentException("fieldMeta == null");

		javax.jdo.Query query = pm.newNamedQuery(FieldMeta.class, NamedQueries.getSubFieldMetasForFieldMeta_fieldID);
		@SuppressWarnings("unchecked")
		Collection<FieldMeta> result = (Collection<FieldMeta>) query.execute(fieldMeta.getFieldID());
		return result;
	}

	public FieldMeta getFieldMeta(long fieldID, boolean throwExceptionIfNotFound)
	{
		LongIdentity identity = new LongIdentity(FieldMeta.class, fieldID);
		try {
			FieldMeta fieldMeta = (FieldMeta) pm.getObjectById(identity);
			return fieldMeta;
		} catch (JDOObjectNotFoundException x) {
			if (throwExceptionIfNotFound)
				throw x;
			else
				return null;
		}
	}

}
