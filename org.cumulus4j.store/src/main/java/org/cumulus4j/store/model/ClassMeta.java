/*
 * Cumulus4j - Securing your data in the cloud - http://cumulus4j.org
 * Copyright (C) 2011 NightLabs Consulting GmbH
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.cumulus4j.store.model;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import javax.jdo.FetchPlan;
import javax.jdo.JDOHelper;
import javax.jdo.PersistenceManager;
import javax.jdo.annotations.Column;
import javax.jdo.annotations.FetchGroup;
import javax.jdo.annotations.FetchGroups;
import javax.jdo.annotations.IdentityType;
import javax.jdo.annotations.NotPersistent;
import javax.jdo.annotations.NullValue;
import javax.jdo.annotations.PersistenceCapable;
import javax.jdo.annotations.Persistent;
import javax.jdo.annotations.Queries;
import javax.jdo.annotations.Query;
import javax.jdo.annotations.Unique;
import javax.jdo.annotations.Version;
import javax.jdo.annotations.VersionStrategy;
import javax.jdo.listener.DetachCallback;

import org.datanucleus.metadata.AbstractClassMetaData;
import org.datanucleus.store.ExecutionContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.appengine.api.datastore.KeyFactory;



/**
 * Persistent meta-data for a persistence-capable {@link Class}. Since class names are very long,
 * we use the {@link #getClassID() classID} instead in our index and data entities (e.g. in the relation
 * {@link DataEntry#getClassMeta() DataEntry.classMeta}).
 *
 * @author Marco หงุ่ยตระกูล-Schulze - marco at nightlabs dot de
 */
@PersistenceCapable(identityType=IdentityType.APPLICATION, detachable="true")
@Version(strategy=VersionStrategy.VERSION_NUMBER)
@Unique(name="ClassMeta_fullyQualifiedClassName", members={"packageName", "simpleClassName"})
@FetchGroups({
	@FetchGroup(name=FetchPlan.ALL, members={
			@Persistent(name="superClassMeta", recursionDepth=-1)
	})
})
@Queries({
	@Query(
			name="getClassMetaByPackageNameAndSimpleClassName",
			value="SELECT UNIQUE WHERE this.packageName == :packageName && this.simpleClassName == :simpleClassName"
	)
})
public class ClassMeta
implements DetachCallback
{
	private static final Logger logger = LoggerFactory.getLogger(ClassMeta.class);

	protected static class NamedQueries {
		public static final String getClassMetaByPackageNameAndSimpleClassName = "getClassMetaByPackageNameAndSimpleClassName";
	}

	private Long classID;
	
	/** This is needed due to GAE compatibility. package-gae.orm is responsible
	 *  for the correct usage if this class
	**/
	private String classIDString;

	@NotPersistent
	private transient volatile String className;

	@Persistent(nullValue=NullValue.EXCEPTION)
	@Column(length=255)
	private String packageName;

	@Persistent(nullValue=NullValue.EXCEPTION)
	@Column(length=255)
	private String simpleClassName;

	private ClassMeta superClassMeta;

	/**
	 * Meta data for all persistent fields of the class referenced by this <code>ClassMeta</code>.
	 * <p>
	 * This map is manually managed (e.g. lazy-loaded by {@link #getFieldName2FieldMeta()} or manually detached
	 * in {@link #jdoPostDetach(Object)}) because of constraints in GAE. We simulate the behaviour of:
	 * <p>
	 * <pre>
	 * &#64;Persistent(mappedBy="classMeta", dependentValue="true")
	 * &#64;Key(mappedBy="fieldName")
	 * </pre>
	 */
	@NotPersistent
	private Map<String, FieldMeta> fieldName2FieldMeta;

	@NotPersistent
	private Map<Long, FieldMeta> fieldID2FieldMeta;

	protected ClassMeta() { }

	public ClassMeta(Class<?> clazz) {
		this.packageName = clazz.getPackage() == null ? "" : clazz.getPackage().getName();
		this.simpleClassName = clazz.getSimpleName();
	}

	public long getClassID() {
		if(classIDString != null && classID == -1){
			classID = KeyFactory.stringToKey(classIDString).getId();
		}
		return classID;
	}

	/**
	 * Get the package name or an empty <code>String</code> for the default package.
	 * @return the package name (maybe empty, but never <code>null</code>).
	 */
	public String getPackageName() {
		return packageName;
	}

	public String getSimpleClassName() {
		return simpleClassName;
	}

	/**
	 * Get the fully qualified class name (composed of {@link #getPackageName() packageName} and {@link #getSimpleClassName() simpleClassName}).
	 * @return the fully qualified class name.
	 */
	public String getClassName()
	{
		String cn = className;
		if (cn == null)
			className = cn = getClassName(packageName, simpleClassName);

		return cn;
	}

	public static String getClassName(String packageName, String simpleClassName) {
		if (packageName == null)
			throw new IllegalArgumentException("packageName == null");
		if (simpleClassName == null)
			throw new IllegalArgumentException("simpleClassName == null");

		if (packageName.isEmpty())
			return simpleClassName;
		else
			return packageName + '.' + simpleClassName;
	}

	/**
	 * The super-class' meta-data or <code>null</code>, if there is no <b>persistence-capable</b> super-class.
	 * @return the super-class' meta-data or <code>null</code>.
	 */
	public ClassMeta getSuperClassMeta() {
		return superClassMeta;
	}

	public void setSuperClassMeta(ClassMeta superClassMeta) {
		this.superClassMeta = superClassMeta;
	}

	/**
	 * Get the {@link PersistenceManager} assigned to <code>this</code>. If there is none, this method checks, if
	 * <code>this</code> is new. If <code>this</code> was persisted before, it must have one or an {@link IllegalStateException}
	 * is thrown.
	 * @return the {@link PersistenceManager} assigned to this or <code>null</code>.
	 */
	protected PersistenceManager getPersistenceManager() {
		PersistenceManager pm = JDOHelper.getPersistenceManager(this);
		if (pm == null) {
			if (JDOHelper.getObjectId(this) != null)
				throw new IllegalStateException("This ClassMeta instance is not new, but JDOHelper.getPersistenceManager(this) returned null! " + this);
		}
		return pm;
	}

	public Map<String, FieldMeta> getFieldName2FieldMeta() {
		Map<String, FieldMeta> result = this.fieldName2FieldMeta;

		if (result == null) {
			logger.debug("getFieldName2FieldMeta: this.fieldName2FieldMeta == null => populating. this={}", this);
			result = new HashMap<String, FieldMeta>();
			PersistenceManager pm = getPersistenceManager();
			if (pm != null) {
				Collection<FieldMeta> fieldMetas = new FieldMetaDAO(pm).getFieldMetasForClassMeta(this);
				for (FieldMeta fieldMeta : fieldMetas)
					result.put(fieldMeta.getFieldName(), fieldMeta);
			}
			this.fieldName2FieldMeta = result;
		}
		else
			logger.trace("getFieldName2FieldMeta: this.fieldName2FieldMeta != null (already populated). this={}", this);

		return result;
	}

	/**
	 * Get all {@link FieldMeta} instances known to this instance. This is the meta-data for all fields
	 * <b>directly declared</b> in the class referenced by this <code>ClassMeta</code> <b>not
	 * including super-classes</b>.
	 * @return Collection of FieldMeta objects for this class
	 */
	public Collection<FieldMeta> getFieldMetas() {
		return getFieldName2FieldMeta().values();
	}

	/**
	 * Get the {@link FieldMeta} for a field that is <b>directly declared</b> in the class referenced by
	 * this <code>ClassMeta</code>. This method thus does not take super-classes into account.
	 *
	 * @param fieldName the simple field name (no class prefix).
	 * @return the {@link FieldMeta} corresponding to the specified <code>fieldName</code> or <code>null</code>, if no such field
	 * exists.
	 * @see #getFieldMeta(long)
	 * @see #getFieldMeta(String, String)
	 */
	public FieldMeta getFieldMeta(String fieldName) {
		return getFieldName2FieldMeta().get(fieldName);
	}

	/**
	 * <p>
	 * Get the {@link FieldMeta} for a field that is either directly declared in the class referenced by this
	 * <code>ClassMeta</code> or in a super-class.
	 * </p>
	 * <p>
	 * If <code>className</code> is <code>null</code>, this method
	 * searches recursively in the inheritance hierarchy upwards (i.e. first this class then the super-class,
	 * then the next super-class etc.) until it finds a field matching the given <code>fieldName</code>.
	 * </p>
	 * <p>
	 * If <code>className</code> is not <code>null</code>, this method searches only in the specified class.
	 * If <code>className</code> is neither the current class nor any super-class, this method always returns
	 * <code>null</code>.
	 * </p>
	 *
	 * @param className the fully qualified class-name of the class referenced by this <code>ClassMeta</code>
	 * or any super-class. <code>null</code> to search the entire class hierarchy upwards (through all super-classes
	 * until the field is found or the last super-class was investigated).
	 * @param fieldName the simple field name (no class prefix).
	 * @return the {@link FieldMeta} matching the given criteria or <code>null</code> if no such field could be found.
	 */
	public FieldMeta getFieldMeta(String className, String fieldName) {
		if (className == null) {
			FieldMeta fieldMeta = getFieldMeta(fieldName);
			if (fieldMeta != null)
				return fieldMeta;

			if (superClassMeta != null)
				return superClassMeta.getFieldMeta(className, fieldName);
			else
				return null;
		}
		else {
			if (getClassName().equals(className))
				return getFieldMeta(fieldName);
			else if (superClassMeta != null)
				return superClassMeta.getFieldMeta(className, fieldName);
			else
				return null;
		}
	}

	/**
	 * Get the {@link FieldMeta} with the specified {@link FieldMeta#getFieldID() fieldID}. It does not matter, if
	 * this field is directly in the class referenced by this <code>ClassMeta</code> or in a super-class.
	 * @param fieldID the {@link FieldMeta#getFieldID() fieldID} of the <code>FieldMeta</code> to be found.
	 * @return the {@link FieldMeta} referenced by the given <code>fieldID</code> or <code>null</code>, if no such
	 * field exists in the class or any super-class.
	 */
	public FieldMeta getFieldMeta(long fieldID)
	{
		Map<Long, FieldMeta> m = fieldID2FieldMeta;

		if (m == null) {
			m = new HashMap<Long, FieldMeta>(getFieldName2FieldMeta().size());
			for (FieldMeta fieldMeta : getFieldName2FieldMeta().values())
				m.put(fieldMeta.getFieldID(), fieldMeta);

			fieldID2FieldMeta = m;
		}

		FieldMeta fieldMeta = m.get(fieldID);
		if (fieldMeta != null)
			return fieldMeta;

		if (superClassMeta != null)
			return superClassMeta.getFieldMeta(fieldID);
		else
			return null;
	}

	public void addFieldMeta(FieldMeta fieldMeta) {
		if (!this.equals(fieldMeta.getClassMeta()))
			throw new IllegalArgumentException("fieldMeta.classMeta != this");

		PersistenceManager pm = getPersistenceManager();
		if (pm != null)
			fieldMeta = pm.makePersistent(fieldMeta);

		getFieldName2FieldMeta().put(fieldMeta.getFieldName(), fieldMeta);
		fieldID2FieldMeta = null;
	}

	public void removeFieldMeta(FieldMeta fieldMeta) {
		if (!this.equals(fieldMeta.getClassMeta()))
			throw new IllegalArgumentException("fieldMeta.classMeta != this");

		getFieldName2FieldMeta().remove(fieldMeta.getFieldName());
		fieldID2FieldMeta = null;

		PersistenceManager pm = getPersistenceManager();
		if (pm != null)
			pm.deletePersistent(fieldMeta);
	}

	@Override
	public int hashCode() {
		Long id = classID;
		if(id == null){
			id = -1L;
		}
		return (int) (id ^ (id >>> 32));
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) return true;
		if (obj == null) return false;
		if (getClass() != obj.getClass()) return false;
		ClassMeta other = (ClassMeta) obj;
		return this.classID == other.classID;
	}

	@Override
	public String toString() {
		return (
				this.getClass().getName()
				+ '@'
				+ Integer.toHexString(System.identityHashCode(this))
				+ '[' + classID + ',' + getClassName() + ']'
		);
	}

	@NotPersistent
	private AbstractClassMetaData dataNucleusClassMetaData;

	public AbstractClassMetaData getDataNucleusClassMetaData(ExecutionContext executionContext)
	{
		if (dataNucleusClassMetaData != null)
			return dataNucleusClassMetaData;

		AbstractClassMetaData dnClassMetaData = executionContext.getMetaDataManager().getMetaDataForClass(getClassName(), executionContext.getClassLoaderResolver());
		if (dnClassMetaData == null)
			throw new IllegalStateException("DataNucleus does not know any meta-data for this class: classID=" + getClassID() + " className=" + getClassName());

		dataNucleusClassMetaData = dnClassMetaData;
		return dnClassMetaData;
	}

	@Override
	public void jdoPreDetach() { }

	protected static final ThreadLocal<Set<ClassMeta>> attachedClassMetasInPostDetachThreadLocal = new ThreadLocal<Set<ClassMeta>>() {
		@Override
		protected Set<ClassMeta> initialValue() {
			return new HashSet<ClassMeta>();
		}
	};

	@Override
	public void jdoPostDetach(Object o) {
		final ClassMeta attached = (ClassMeta) o;
		final ClassMeta detached = this;
		logger.debug("jdoPostDetach: attached={}", attached);

		Set<ClassMeta> attachedClassMetasInPostDetach = attachedClassMetasInPostDetachThreadLocal.get();
		if (!attachedClassMetasInPostDetach.add(attached)) {
			logger.debug("jdoPostDetach: Already in detachment => Skipping detachment of this.fieldName2FieldMeta! attached={}", attached);
			return;
		}
		try {

			PersistenceManager pm = attached.getPersistenceManager();
			if (pm == null)
				throw new IllegalStateException("attached.getPersistenceManager() returned null!");

			// The following fields should already be null, but we better ensure that we never
			// contain *AT*tached objects inside a *DE*tached container.
			detached.fieldName2FieldMeta = null;
			detached.fieldID2FieldMeta = null;

			Set<?> fetchGroups = pm.getFetchPlan().getGroups();
			if (fetchGroups.contains(javax.jdo.FetchGroup.ALL)) {
				logger.debug("jdoPostDetach: Detaching this.fieldName2FieldMeta: attached={}", attached);

				// if the fetch-groups say we should detach the FieldMetas, we do it.
				HashMap<String, FieldMeta> map = new HashMap<String, FieldMeta>();
				
				Collection<FieldMeta> fieldMetas = new ArrayList<FieldMeta>();
				Collection<FieldMeta> attachedFieldMetas = attached.getFieldMetas();
				for(FieldMeta fieldMeta : attachedFieldMetas){
					pm.makePersistent(fieldMeta);
					fieldMetas.add(fieldMeta.clone());
				}
				
				for (FieldMeta fieldMeta : fieldMetas) {
					fieldMeta.setClassMeta(this); // ensure, it's the identical (not only equal) ClassMeta.
					map.put(fieldMeta.getFieldName(), fieldMeta);
				}
				detached.fieldName2FieldMeta = map;
			}

		} finally {
			attachedClassMetasInPostDetach.remove(attached);
		}
	}
}
