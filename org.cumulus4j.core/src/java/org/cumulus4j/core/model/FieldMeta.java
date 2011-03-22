package org.cumulus4j.core.model;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import javax.jdo.annotations.Column;
import javax.jdo.annotations.IdGeneratorStrategy;
import javax.jdo.annotations.IdentityType;
import javax.jdo.annotations.Key;
import javax.jdo.annotations.NotPersistent;
import javax.jdo.annotations.NullValue;
import javax.jdo.annotations.PersistenceCapable;
import javax.jdo.annotations.Persistent;
import javax.jdo.annotations.PrimaryKey;
import javax.jdo.annotations.Unique;
import javax.jdo.annotations.Uniques;
import javax.jdo.listener.DetachCallback;

import org.cumulus4j.core.Cumulus4jStoreManager;
import org.datanucleus.metadata.AbstractClassMetaData;
import org.datanucleus.metadata.AbstractMemberMetaData;
import org.datanucleus.store.ExecutionContext;

/**
 * Persistent meta-data for a field of a persistence-capable class. Since class- and field-names are very
 * long we reference them indirectly via the long-identifiers of {@link ClassMeta} and {@link FieldMeta},
 * e.g. in the relation {@link IndexEntry#getFieldMeta() IndexEntry.fieldMeta}.
 *
 * @author Marco หงุ่ยตระกูล-Schulze - marco at nightlabs dot de
 */
@PersistenceCapable(identityType=IdentityType.APPLICATION, detachable="true")
@Uniques({
	@Unique(name="FieldMeta_classMeta_ownerFieldMeta_fieldName_role", members={"classMeta", "ownerFieldMeta", "fieldName", "role"})
})
public class FieldMeta
implements DetachCallback
{
	@PrimaryKey
	@Persistent(valueStrategy=IdGeneratorStrategy.NATIVE)
	private long fieldID = -1;

	private ClassMeta classMeta;

	private FieldMeta ownerFieldMeta;

	@Persistent(nullValue=NullValue.EXCEPTION)
	@Column(length=255)
	private String fieldName;

	@Persistent(nullValue=NullValue.EXCEPTION)
	private FieldMetaRole role;

	@NotPersistent
	private int dataNucleusAbsoluteFieldNumber = -1;

	@Persistent(mappedBy="ownerFieldMeta", dependentValue="true")
	@Key(mappedBy="role")
	private Map<FieldMetaRole, FieldMeta> role2subFieldMeta = new HashMap<FieldMetaRole, FieldMeta>();

	protected FieldMeta() { }

	public FieldMeta(ClassMeta classMeta, String fieldName)
	{
		this(classMeta, null, fieldName, FieldMetaRole.primary);
	}
	public FieldMeta(FieldMeta ownerFieldMeta, String fieldName, FieldMetaRole role)
	{
		this(null, ownerFieldMeta, fieldName, role);
	}

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

	public long getFieldID() {
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

	/**
	 * Get the {@link FieldMetaRole#primary primary} {@link FieldMeta}, to which this sub-<code>FieldMeta</code> belongs
	 * or <code>null</code>, if this <code>FieldMeta</code> is primary.
	 * @return the owning primary field-meta or <code>null</code>.
	 */
	public FieldMeta getOwnerFieldMeta() {
		return ownerFieldMeta;
	}

	/**
	 * Get the simple field name (no class prefix) of the field referenced by this meta-data-instance.
	 * @return the simple field name.
	 */
	public String getFieldName() {
		return fieldName;
	}

	public FieldMetaRole getRole() {
		return role;
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

		for (FieldMeta subFM : role2subFieldMeta.values())
			subFM.setDataNucleusAbsoluteFieldNumber(dataNucleusAbsoluteFieldNumber);
	}

	public FieldMeta getSubFieldMeta(FieldMetaRole role)
	{
		return role2subFieldMeta.get(role);
	}

	public Collection<FieldMeta> getSubFieldMetas()
	{
		return role2subFieldMeta.values();
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
		role2subFieldMeta.put(subFieldMeta.getRole(), subFieldMeta);
	}

	public void removeSubFieldMeta(FieldMeta fieldMeta)
	{
		role2subFieldMeta.remove(fieldMeta.getRole());
	}

	public void removeAllSubFieldMetasExcept(FieldMetaRole ... roles)
	{
		if (roles == null)
			roles = new FieldMetaRole[0];

		Set<FieldMetaRole> rolesToKeep = new HashSet<FieldMetaRole>(roles.length);
		for (FieldMetaRole role : roles)
			rolesToKeep.add(role);

		Collection<FieldMetaRole> oldRoles = new ArrayList<FieldMetaRole>(role2subFieldMeta.keySet());
		for (FieldMetaRole role : oldRoles) {
			if (!rolesToKeep.contains(role))
				role2subFieldMeta.remove(role);
		}
	}

	@Override
	public void jdoPreDetach() { }

	@Override
	public void jdoPostDetach(Object o) {
		FieldMeta attached = (FieldMeta) o;
		FieldMeta detached = this;
		detached.dataNucleusAbsoluteFieldNumber = attached.dataNucleusAbsoluteFieldNumber;
	}

	@Override
	public int hashCode() {
		return (int) (fieldID ^ (fieldID >>> 32));
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) return true;
		if (obj == null) return false;
		if (getClass() != obj.getClass()) return false;
		FieldMeta other = (FieldMeta) obj;
		return this.fieldID == other.fieldID;
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
