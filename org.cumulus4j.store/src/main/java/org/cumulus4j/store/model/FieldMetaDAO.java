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
		javax.jdo.Query query = pm.newNamedQuery(FieldMeta.class, NamedQueries.getFieldMetasForClassMeta);
		@SuppressWarnings("unchecked")
		Collection<FieldMeta> result = (Collection<FieldMeta>) query.execute(classMeta);
		return result;
	}

	public Collection<FieldMeta> getSubFieldMetasForFieldMeta(FieldMeta fieldMeta) {
		javax.jdo.Query query = pm.newNamedQuery(FieldMeta.class, NamedQueries.getSubFieldMetasForFieldMeta);
		@SuppressWarnings("unchecked")
		Collection<FieldMeta> result = (Collection<FieldMeta>) query.execute(fieldMeta);
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
