package org.cumulus4j.store.model;

import java.util.Collection;

import javax.jdo.PersistenceManager;

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

}
