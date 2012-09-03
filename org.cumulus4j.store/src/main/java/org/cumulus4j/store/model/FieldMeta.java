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

import javax.jdo.JDOHelper;
import javax.jdo.PersistenceManager;
import javax.jdo.annotations.Column;
import javax.jdo.annotations.IdentityType;
import javax.jdo.annotations.NotPersistent;
import javax.jdo.annotations.NullValue;
import javax.jdo.annotations.PersistenceCapable;
import javax.jdo.annotations.Persistent;
import javax.jdo.annotations.Queries;
import javax.jdo.annotations.Query;
import javax.jdo.annotations.Unique;
import javax.jdo.annotations.Uniques;
import javax.jdo.annotations.Version;
import javax.jdo.annotations.VersionStrategy;
import javax.jdo.listener.DetachCallback;

import org.cumulus4j.store.Cumulus4jStoreManager;
import org.datanucleus.metadata.AbstractClassMetaData;
import org.datanucleus.metadata.AbstractMemberMetaData;
import org.datanucleus.store.ExecutionContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Persistent meta-data for a field of a persistence-capable class. Since class- and field-names are very
 * long we reference them indirectly via the long-identifiers of {@link ClassMeta} and {@link FieldMeta},
 * e.g. in the relation {@link IndexEntry#getFieldMeta() IndexEntry.fieldMeta}.
 *
 * @author Marco หงุ่ยตระกูล-Schulze - marco at nightlabs dot de
 */
@PersistenceCapable(identityType=IdentityType.APPLICATION, detachable="true")
@Version(strategy=VersionStrategy.VERSION_NUMBER)
@Uniques({
	@Unique(name="FieldMeta_classMeta_ownerFieldMeta_fieldName_role", members={"classMeta", "ownerFieldMeta", "fieldName", "role"})
})
@Queries({
	@Query(name=FieldMeta.NamedQueries.getFieldMetasForClassMeta, value="SELECT WHERE this.classMeta == :classMeta"),
	@Query(name=FieldMeta.NamedQueries.getSubFieldMetasForFieldMeta, value="SELECT WHERE this.ownerFieldMeta == :ownerFieldMeta")
})
public class FieldMeta
implements DetachCallback
{
	private static final Logger logger = LoggerFactory.getLogger(FieldMeta.class);

	protected static class NamedQueries {
		public static final String getFieldMetasForClassMeta = "getFieldMetasForClassMeta";
		public static final String getSubFieldMetasForFieldMeta = "getSubFieldMetasForFieldMeta";
	}

	private long fieldID = -1;
	
	/** This is needed due to GAE compatibility. package-gae.orm is responsible
	 *  for the correct usage if this class
	 * 	
	 * @since 18.05.2012
	 * @author Alexander.Gauss
	**/
	private String fieldIDString;

	private ClassMeta classMeta;

	private FieldMeta ownerFieldMeta;

	@Persistent(nullValue=NullValue.EXCEPTION)
	@Column(length=255)
	private String fieldName;

	@Persistent(nullValue=NullValue.EXCEPTION)
	private FieldMetaRole role;

	@NotPersistent
	private int dataNucleusAbsoluteFieldNumber = -1;

	/**
	 * Meta data for all sub-fields of this <code>FieldMeta</code>.
	 * <p>
	 * This map is manually managed (e.g. lazy-loaded by {@link #getRole2SubFieldMeta()} or manually detached
	 * in {@link #jdoPostDetach(Object)}) because of constraints in GAE. We simulate the behaviour of:
	 * <p>
	 * <pre>
	 * &#64;Persistent(mappedBy="ownerFieldMeta", dependentValue="true")
	 * &#64;Key(mappedBy="role")
	 * </pre>
	 */
	@NotPersistent
	private Map<FieldMetaRole, FieldMeta> role2SubFieldMeta;

	/**
	 * Internal constructor. This exists only for JDO and should not be used by application code!
	 */
	protected FieldMeta() { }

	/**
	 * Create a <code>FieldMeta</code> referencing a real field.
	 * @param classMeta the class to which this field belongs.
	 * @param fieldName the field's name.
	 * @see #FieldMeta(FieldMeta, FieldMetaRole)
	 */
	public FieldMeta(ClassMeta classMeta, String fieldName)
	{
		this(classMeta, null, fieldName, FieldMetaRole.primary);
	}
	/**
	 * Create a <code>FieldMeta</code> referencing a part of a field. This is necessary to index keys and values of a
	 * <code>Map</code> field (i.e. 2 separate indexes for one field) as well as <code>Collection</code>-elements and similar.
	 * @param ownerFieldMeta the <code>FieldMeta</code> of the real field (to which the part belongs).
	 * @param role the role (aka type) of the sub-field (aka part).
	 * @see #FieldMeta(ClassMeta, String)
	 */
	public FieldMeta(FieldMeta ownerFieldMeta, FieldMetaRole role)
	{
		this(null, ownerFieldMeta, ownerFieldMeta.getFieldName(), role);
	}

	/**
	 * Internal constructor. This exists only for easier implementation of the other constructors and
	 * should not be used by application code!
	 */
	protected FieldMeta(ClassMeta classMeta, FieldMeta ownerFieldMeta, String fieldName, FieldMetaRole role)
	{
		if (classMeta == null && ownerFieldMeta == null)
			throw new IllegalArgumentException("classMeta == null && ownerFieldMeta == null");

		if (classMeta != null && ownerFieldMeta != null)
			throw new IllegalArgumentException("classMeta != null && ownerFieldMeta != null");

		if (fieldName == null)
			throw new IllegalArgumentException("fieldName == null");

		if (role == null)
			throw new IllegalArgumentException("role == null");

		this.classMeta = classMeta;
		this.ownerFieldMeta = ownerFieldMeta;
		this.fieldName = fieldName;
		this.role = role;
	}
	
	/**
	 * Copy C´tor
	 */
	protected FieldMeta(ClassMeta classMeta, FieldMeta ownerFieldMeta, String fieldName, 
							FieldMetaRole role, long fieldID, String fieldIDString, int dnAbsFieldNumber, 
							Map<FieldMetaRole, FieldMeta> role2SubFieldMeta){
		this.classMeta = classMeta;
		this.ownerFieldMeta = ownerFieldMeta;
		this.fieldName = fieldName;
		this.role = role;
		this.fieldID = fieldID;
		this.fieldIDString = fieldIDString;
		this.role2SubFieldMeta = role2SubFieldMeta;
		this.dataNucleusAbsoluteFieldNumber = dnAbsFieldNumber;
	}
	
	@Override
	public FieldMeta clone(){
		return new FieldMeta(this.getClassMeta(), this.getOwnerFieldMeta(), this.getFieldName(), 
								this.getRole(), this.getFieldID(), this.fieldIDString, 
								this.getDataNucleusAbsoluteFieldNumber(), this.getRole2SubFieldMeta());
	}

	public long getFieldID() {
		if(fieldID == -1){
			return Long.valueOf(fieldIDString);
		}
		return fieldID;
	}

	/**
	 * Get the {@link ClassMeta} to which this <code>FieldMeta</code> belongs. Every FieldMeta
	 * belongs to exactly one {@link ClassMeta} just like a field is declared in exactly one Java class.
	 * Note, that a {@link FieldMeta} might belong to another FieldMeta in order to reference sub-field-properties,
	 * e.g. a {@link Map}'s key. In this case, the direct property <code>classMeta</code> is <code>null</code>, but this method
	 * still returns the correct {@link ClassMeta} by resolving it indirectly via the {@link #getOwnerFieldMeta() ownerFieldMeta}.
	 * @return the {@link ClassMeta} to which this instance of <code>FieldMeta</code> belongs.
	 */
	public ClassMeta getClassMeta() {
		if (ownerFieldMeta != null)
			return ownerFieldMeta.getClassMeta();

		return classMeta;
	}

	protected void setClassMeta(ClassMeta classMeta) {
		// We allow only assignment of equal arguments (e.g. during detachment).
		if (this.classMeta != null && !this.classMeta.equals(classMeta))
			throw new IllegalStateException("Cannot modify this this.classMeta!");

		this.classMeta = classMeta;
	}

	/**
	 * Get the {@link FieldMetaRole#primary primary} {@link FieldMeta}, to which this sub-<code>FieldMeta</code> belongs
	 * or <code>null</code>, if this <code>FieldMeta</code> is primary.
	 * @return the owning primary field-meta or <code>null</code>.
	 */
	public FieldMeta getOwnerFieldMeta() {
		return ownerFieldMeta;
	}

	protected void setOwnerFieldMeta(FieldMeta ownerFieldMeta) {
		// We allow only assignment of equal arguments (e.g. during detachment).
		if (this.ownerFieldMeta != null && !this.ownerFieldMeta.equals(ownerFieldMeta))
			throw new IllegalStateException("Cannot modify this this.ownerFieldMeta!");

		this.ownerFieldMeta = ownerFieldMeta;
	}

	/**
	 * Get the simple field name (no class prefix) of the field referenced by this meta-data-instance.
	 * @return the simple field name.
	 */
	public String getFieldName() {
		return fieldName;
	}

	/**
	 * Get the role of the (sub-)field. If this is not a sub-field, but a primary field
	 * (i.e. directly meaning a real field of the class referenced by {@link #getClassMeta() classMeta})
	 * it will be {@link FieldMetaRole#primary}, hence this method never returns <code>null</code>.
	 * @return the role of this <code>FieldMeta</code>; never <code>null</code>.
	 */
	public FieldMetaRole getRole() {
		return role;
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
				throw new IllegalStateException("This FieldMeta instance is not new, but JDOHelper.getPersistenceManager(this) returned null! " + this);
		}
		return pm;
	}

	protected Map<FieldMetaRole, FieldMeta> getRole2SubFieldMeta() {
		Map<FieldMetaRole, FieldMeta> result = this.role2SubFieldMeta;

		if (result == null) {
			logger.debug("getRole2SubFieldMeta: this.role2SubFieldMeta == null => populating. this={}", this);
			result = new HashMap<FieldMetaRole, FieldMeta>();
			PersistenceManager pm = getPersistenceManager();
			if (pm != null) {
				Collection<FieldMeta> fieldMetas = new FieldMetaDAO(pm).getSubFieldMetasForFieldMeta(this);
				for (FieldMeta fieldMeta : fieldMetas)
					result.put(fieldMeta.getRole(), fieldMeta);
			}

			this.role2SubFieldMeta = result;
		}
		else
			logger.trace("getRole2SubFieldMeta: this.role2SubFieldMeta != null (already populated). this={}", this);

		return result;
	}

	/**
	 * Get the non-persistent field-number in DataNucleus' meta-data. This is only a usable value,
	 * if this <code>FieldMeta</code> was obtained via
	 * {@link Cumulus4jStoreManager#getClassMeta(org.datanucleus.store.ExecutionContext, Class)}; otherwise
	 * it is -1.
	 * @return the non-persistent field-number in DataNucleus' meta-data or -1.
	 */
	public int getDataNucleusAbsoluteFieldNumber() {
		return dataNucleusAbsoluteFieldNumber;
	}
	public void setDataNucleusAbsoluteFieldNumber(int dataNucleusAbsoluteFieldNumber) {
		this.dataNucleusAbsoluteFieldNumber = dataNucleusAbsoluteFieldNumber;
		this.dataNucleusMemberMetaData = null;

		for (FieldMeta subFM : getRole2SubFieldMeta().values())
			subFM.setDataNucleusAbsoluteFieldNumber(dataNucleusAbsoluteFieldNumber);
	}

	/**
	 * Get a sub-field of this field or <code>null</code>, if no such sub-field exists.
	 * @param role the role of the sub-field. Must not be <code>null</code>.
	 * @return the sub-<code>FieldMeta</code> or <code>null</code>.
	 */
	public FieldMeta getSubFieldMeta(FieldMetaRole role)
	{
		if (role == null)
			throw new IllegalArgumentException("role == null");

		return getRole2SubFieldMeta().get(role);
	}

	/**
	 * Get all sub-fields' meta-data of this field. If there are no sub-fields, this is an
	 * empty collection.
	 * @return all sub-<code>FieldMeta</code>s of this field; never <code>null</code>.
	 */
	public Collection<FieldMeta> getSubFieldMetas()
	{
		return getRole2SubFieldMeta().values();
	}

	public void addSubFieldMeta(FieldMeta subFieldMeta)
	{
		if (!this.equals(subFieldMeta.getOwnerFieldMeta()))
			throw new IllegalArgumentException("this != subFieldMeta.ownerFieldMeta");

		if (!this.fieldName.equals(subFieldMeta.getFieldName()))
			throw new IllegalArgumentException("this.fieldName != subFieldMeta.fieldName");

		if (getSubFieldMeta(subFieldMeta.getRole()) != null)
			throw new IllegalArgumentException("There is already a subFieldMeta with role \"" + subFieldMeta.getRole() + "\"!");

		subFieldMeta.setDataNucleusAbsoluteFieldNumber(dataNucleusAbsoluteFieldNumber);
		PersistenceManager pm = getPersistenceManager();
		if (pm != null)
			subFieldMeta = pm.makePersistent(subFieldMeta);

		getRole2SubFieldMeta().put(subFieldMeta.getRole(), subFieldMeta);
	}

	public void removeSubFieldMeta(FieldMeta subFieldMeta)
	{
		if (!this.equals(subFieldMeta.getOwnerFieldMeta()))
			throw new IllegalArgumentException("subFieldMeta.ownerFieldMeta != this");

		getRole2SubFieldMeta().remove(subFieldMeta.getRole());
		PersistenceManager pm = getPersistenceManager();
		if (pm != null)
			pm.deletePersistent(subFieldMeta);
	}

	public void removeAllSubFieldMetasExcept(FieldMetaRole ... roles)
	{
		if (roles == null)
			roles = new FieldMetaRole[0];

		Set<FieldMetaRole> rolesToKeep = new HashSet<FieldMetaRole>(roles.length);
		for (FieldMetaRole role : roles)
			rolesToKeep.add(role);
		PersistenceManager pm = getPersistenceManager();
		Collection<FieldMetaRole> oldRoles = new ArrayList<FieldMetaRole>(getRole2SubFieldMeta().keySet());
		for (FieldMetaRole role : oldRoles) {
			if (!rolesToKeep.contains(role)) {
				FieldMeta subFieldMeta = getRole2SubFieldMeta().remove(role);

				if (pm != null && subFieldMeta != null)
					pm.deletePersistent(subFieldMeta);
			}
		}
	}

	@NotPersistent
	private transient FieldMeta mappedByFieldMeta;

	/**
	 * Used by {@link #getMappedByFieldMeta(ExecutionContext)} to mask <code>null</code> and thus
	 * prevent a second unnecessary resolve process if the first already resolved to <code>null</code>.
	 */
	private static final FieldMeta NULL_MAPPED_BY_FIELD_META = new FieldMeta();

	/**
	 * <p>
	 * Get the {@link FieldMeta} of the opposite end of the mapped-by-relation. If
	 * this is not a mapped-by field, this method returns <code>null</code>.
	 * </p>
	 * <p>
	 * Though, it returns always the mapped-by opposite side, the semantics of
	 * this method still depend on the {@link #getRole() role} of this <code>FieldMeta</code>:
	 * </p>
	 * <ul>
	 * <li>{@link FieldMetaRole#primary}: Returns the owner-field on the opposite side which is referenced by
	 * &#64;Persistent(mappedBy="owner")</li>
	 * <li>{@link FieldMetaRole#mapKey}: Returns the key-field on the opposite side which is referenced by
	 * &#64;Key(mappedBy="key")</li>
	 * <li>{@link FieldMetaRole#mapValue}: Returns the value-field on the opposite side which is referenced by
	 * &#64;Value(mappedBy="value")</li>
	 * </ul>
	 *
	 * @return the {@link FieldMeta} of the other end of the mapped-by-relation.
	 */
	public FieldMeta getMappedByFieldMeta(ExecutionContext executionContext)
	{
		FieldMeta mbfm = mappedByFieldMeta;

		if (NULL_MAPPED_BY_FIELD_META == mbfm)
			return null;

		if (mbfm != null)
			return mbfm;

		AbstractMemberMetaData mmd = getDataNucleusMemberMetaData(executionContext);

		if (mmd.getMappedBy() != null)
		{
			Class<?> typeOppositeSide;
			if (mmd.hasCollection())
				typeOppositeSide = executionContext.getClassLoaderResolver().classForName(mmd.getCollection().getElementType());
			else if (mmd.hasArray())
				typeOppositeSide = executionContext.getClassLoaderResolver().classForName(mmd.getArray().getElementType());
			else if (mmd.hasMap()) {
				if (mmd.getMap().keyIsPersistent())
					typeOppositeSide = executionContext.getClassLoaderResolver().classForName(mmd.getMap().getKeyType());
				else if (mmd.getMap().valueIsPersistent())
					typeOppositeSide = executionContext.getClassLoaderResolver().classForName(mmd.getMap().getValueType());
				else
					throw new IllegalStateException("How can a Map be mapped-by without key and value being persistent?! Exactly one of them should be persistent!");
			}
			else
				typeOppositeSide = mmd.getType();

			Cumulus4jStoreManager storeManager = (Cumulus4jStoreManager) executionContext.getStoreManager();
			ClassMeta classMetaOppositeSide = storeManager.getClassMeta(executionContext, typeOppositeSide);
			String mappedBy = null;

			switch (role) {
				case primary:
					mbfm = classMetaOppositeSide.getFieldMeta(mmd.getMappedBy());
					break;

				case mapKey:
					mappedBy = mmd.getKeyMetaData() == null ? null : mmd.getKeyMetaData().getMappedBy();
					if (mmd.getMap().valueIsPersistent() && mappedBy == null)
						throw new IllegalStateException("The map's value is persistent via mappedBy (without @Join), but there is no @Key(mappedBy=\"...\")! This is invalid! " + mmd);
					break;

				case mapValue:
					mappedBy = mmd.getValueMetaData() == null ? null : mmd.getValueMetaData().getMappedBy();
					if (mmd.getMap().keyIsPersistent() && mappedBy == null)
						throw new IllegalStateException("The map's key is persistent via mappedBy (without @Join), but there is no @Value(mappedBy=\"...\")! This is invalid! " + mmd);
					break;

				case arrayElement:
				case collectionElement:
					// TODO doesn't this need implementation?
					// Seems to work this way, but why? Marco :-)
					break;

				default:
					throw new IllegalStateException("Unexpected role: " + role);
			}

			if (mappedBy != null) {
				mbfm = classMetaOppositeSide.getFieldMeta(mappedBy);
				if (mbfm == null)
					throw new IllegalStateException("Field \"" + mappedBy + "\" referenced in 'mappedBy' of " + this + " does not exist!");
			}
		}

		if (mbfm == null)
			mappedByFieldMeta = NULL_MAPPED_BY_FIELD_META;
		else
			mappedByFieldMeta = mbfm;

		return mbfm;
	}

	protected static final ThreadLocal<Set<FieldMeta>> attachedFieldMetasInPostDetachThreadLocal = new ThreadLocal<Set<FieldMeta>>() {
		@Override
		protected Set<FieldMeta> initialValue() {
			return new HashSet<FieldMeta>();
		}
	};

	@Override
	public void jdoPreDetach() { }

	@Override
	public void jdoPostDetach(Object o) {
		final FieldMeta attached = (FieldMeta) o;
		final FieldMeta detached = this;
		logger.debug("jdoPostDetach: attached={}", attached);
		detached.dataNucleusAbsoluteFieldNumber = attached.dataNucleusAbsoluteFieldNumber;

		Set<FieldMeta> attachedFieldMetasInPostDetach = attachedFieldMetasInPostDetachThreadLocal.get();
		if (!attachedFieldMetasInPostDetach.add(attached)) {
			logger.debug("jdoPostDetach: Already in detachment => Skipping detachment of this.role2SubFieldMeta! attached={}", attached);
			return;
		}
		try {

			PersistenceManager pm = attached.getPersistenceManager();
			if (pm == null)
				throw new IllegalStateException("attached.getPersistenceManager() returned null!");

			// The following field should already be null, but we better ensure that we never
			// contain *AT*tached objects inside a *DE*tached container.
			detached.role2SubFieldMeta = null;

			Set<?> fetchGroups = pm.getFetchPlan().getGroups();
			if (fetchGroups.contains(javax.jdo.FetchGroup.ALL)) {
				logger.debug("jdoPostDetach: Detaching this.role2SubFieldMeta: attached={}", attached);

				// if the fetch-groups say we should detach the FieldMetas, we do it.
				HashMap<FieldMetaRole, FieldMeta> map = new HashMap<FieldMetaRole, FieldMeta>();
				Collection<FieldMeta> detachedSubFieldMetas = pm.detachCopyAll(attached.getRole2SubFieldMeta().values());
				for (FieldMeta detachedSubFieldMeta : detachedSubFieldMetas) {
					detachedSubFieldMeta.setOwnerFieldMeta(this); // ensure, it's the identical (not only equal) FieldMeta.
					map.put(detachedSubFieldMeta.getRole(), detachedSubFieldMeta);
				}
				detached.role2SubFieldMeta = map;
			}

		} finally {
			attachedFieldMetasInPostDetach.remove(attached);
		}
	}

	@Override
	public int hashCode()
	{
		return (int) (fieldID ^ (fieldID >>> 32));
	}

	@Override
	public boolean equals(Object obj)
	{
		if (this == obj) return true;
		if (obj == null) return false;
		if (getClass() != obj.getClass()) return false;
		FieldMeta other = (FieldMeta) obj;
		return this.fieldID == other.fieldID;
	}

	@Override
	public String toString()
	{
		ClassMeta cm = getClassMeta();
		return (
				this.getClass().getName()
				+ '@'
				+ Integer.toHexString(System.identityHashCode(this))
				+ '['
				+ fieldID + ',' + (cm == null ? null : cm.getClassName()) + '#' + getFieldName() + '[' + role + ']'
				+ ']'
		);
	}

	@NotPersistent
	private AbstractMemberMetaData dataNucleusMemberMetaData;

	public AbstractMemberMetaData getDataNucleusMemberMetaData(ExecutionContext executionContext)
	{
		if (dataNucleusMemberMetaData != null)
			return dataNucleusMemberMetaData;

		AbstractClassMetaData dnClassMetaData = getClassMeta().getDataNucleusClassMetaData(executionContext);

		int dnFieldNumber = getDataNucleusAbsoluteFieldNumber();
		if (dnFieldNumber < 0)
			throw new IllegalStateException("The method getDataNucleusMemberMetaData(...) can only be called on FieldMeta instances that were obtained via Cumulus4jStoreManager#getClassMeta(org.datanucleus.store.ExecutionContext, Class)!!!");

		AbstractMemberMetaData dnMemberMetaData = dnClassMetaData.getMetaDataForManagedMemberAtAbsolutePosition(dnFieldNumber);
		if (dnMemberMetaData == null)
			throw new IllegalStateException("DataNucleus has no meta-data for this field: fieldID=" + getFieldID() + " className=" + classMeta.getClassName() + " fieldName=" + getFieldName());

		dataNucleusMemberMetaData = dnMemberMetaData;
		return dnMemberMetaData;
	}
}
