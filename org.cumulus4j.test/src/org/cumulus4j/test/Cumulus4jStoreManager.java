package org.cumulus4j.test;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import javax.jdo.FetchPlan;
import javax.jdo.PersistenceManager;

import org.cumulus4j.test.model.ClassMeta;
import org.cumulus4j.test.model.FieldMeta;
import org.datanucleus.ClassLoaderResolver;
import org.datanucleus.NucleusContext;
import org.datanucleus.metadata.AbstractClassMetaData;
import org.datanucleus.metadata.AbstractMemberMetaData;
import org.datanucleus.store.AbstractStoreManager;
import org.datanucleus.store.ExecutionContext;
import org.datanucleus.store.connection.ManagedConnection;

public class Cumulus4jStoreManager
extends AbstractStoreManager
{
	private Map<Class<?>, ClassMeta> class2classMeta = Collections.synchronizedMap(new HashMap<Class<?>, ClassMeta>());

	public Cumulus4jStoreManager(ClassLoaderResolver clr, NucleusContext nucleusContext, Map<String, Object> props)
	{
		super("cumulus4j", clr, nucleusContext, props);

		persistenceHandler = new Cumulus4jPersistenceHandler(this);
	}

	public ClassMeta getClassMeta(ExecutionContext executionContext, Class<?> clazz)
	{
		ClassMeta result = class2classMeta.get(clazz);
		if (result != null)
			return result;

		ManagedConnection mconn = this.getConnection(executionContext);
		try {
			PersistenceManager pm = (PersistenceManager) mconn.getConnection();
			pm.getFetchPlan().setGroup(FetchPlan.ALL);
			result = registerClass(executionContext, pm, clazz);
			pm.getFetchPlan().setGroup(FetchPlan.ALL);
			pm.getFetchPlan().setMaxFetchDepth(-1);
			result = pm.detachCopy(result);

			class2classMeta.put(clazz, result);

			// TODO Later, we should register the super-class' infos here, too, but that's just optimization (not essentially necessary).

		} finally {
			mconn.release();
		}

		return result;
	}

	private ClassMeta registerClass(ExecutionContext executionContext, PersistenceManager pm, Class<?> clazz)
	{
		AbstractClassMetaData dnClassMetaData = getMetaDataManager().getMetaDataForClass(clazz, executionContext.getClassLoaderResolver());
		if (dnClassMetaData == null)
			throw new IllegalArgumentException("The class " + clazz.getName() + " does not have persistence-meta-data! Is it persistence-capable? Is it enhanced?");

		ClassMeta classMeta = ClassMeta.getClassMeta(pm, clazz, false);
		if (classMeta == null) {
			classMeta = new ClassMeta(clazz);
			classMeta = pm.makePersistent(classMeta);
			pm.flush(); // Get exceptions as soon as possible by forcing a flush already here.
		}

		Class<?> superclass = clazz.getSuperclass();
		if (superclass != null && getMetaDataManager().hasMetaDataForClass(superclass.getName())) {
			ClassMeta superClassMeta = registerClass(executionContext, pm, superclass);
			classMeta.setSuperClassMeta(superClassMeta);
		}

		Set<String> persistentMemberNames = new HashSet<String>();
		for (AbstractMemberMetaData memberMetaData : dnClassMetaData.getManagedMembers()) {
			if (!memberMetaData.isFieldToBePersisted())
				continue;

			persistentMemberNames.add(memberMetaData.getName());
			FieldMeta fieldMeta = classMeta.getFieldMeta(memberMetaData.getName());
			if (fieldMeta == null) {
				// adding field that's so far unknown
				fieldMeta = new FieldMeta(classMeta, memberMetaData.getType(), memberMetaData.getName());
				classMeta.addFieldMeta(fieldMeta);
			}
			fieldMeta.setDataNucleusAbsoluteFieldNumber(memberMetaData.getAbsoluteFieldNumber());
		}

		for (FieldMeta fieldMeta : new ArrayList<FieldMeta>(classMeta.getFieldMetas())) {
			if (persistentMemberNames.contains(fieldMeta.getFieldName()))
				continue;

			// The field is not in the class anymore => remove its persistent reference.
			classMeta.removeFieldMeta(fieldMeta);
		}

		pm.flush(); // Get exceptions as soon as possible by forcing a flush already here.
		return classMeta;
	}
}
