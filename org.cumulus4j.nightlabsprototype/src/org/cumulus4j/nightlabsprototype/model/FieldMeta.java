package org.cumulus4j.nightlabsprototype.model;

import javax.jdo.annotations.Column;
import javax.jdo.annotations.IdGeneratorStrategy;
import javax.jdo.annotations.IdentityType;
import javax.jdo.annotations.NotPersistent;
import javax.jdo.annotations.NullValue;
import javax.jdo.annotations.PersistenceCapable;
import javax.jdo.annotations.Persistent;
import javax.jdo.annotations.PrimaryKey;
import javax.jdo.annotations.Unique;
import javax.jdo.listener.DetachCallback;

import org.cumulus4j.nightlabsprototype.Cumulus4jStoreManager;

/**
 * Persistent meta-data for a field of a persistence-capable class. Since class- and field-names are very
 * long we reference them indirectly via the long-identifiers of {@link ClassMeta} and {@link FieldMeta},
 * e.g. in the relation {@link IndexEntry#getFieldMeta() IndexEntry.fieldMeta}.
 *
 * @author Marco หงุ่ยตระกูล-Schulze - marco at nightlabs dot de
 */
@PersistenceCapable(identityType=IdentityType.APPLICATION, detachable="true")
@Unique(name="FieldMeta_classMeta_fieldName", members={"classMeta", "fieldName"})
public class FieldMeta
implements DetachCallback
{
	@PrimaryKey
	@Persistent(valueStrategy=IdGeneratorStrategy.NATIVE)
	private long fieldID = -1;

	@Persistent(nullValue=NullValue.EXCEPTION)
	private ClassMeta classMeta;

	@Persistent(nullValue=NullValue.EXCEPTION)
	@Column(length=255)
	private String fieldTypePackageName;

	@Persistent(nullValue=NullValue.EXCEPTION)
	@Column(length=255)
	private String fieldTypeSimpleClassName;

	@Persistent(nullValue=NullValue.EXCEPTION)
	@Column(length=255)
	private String fieldName;

	@NotPersistent
	private int dataNucleusAbsoluteFieldNumber = -1;

	protected FieldMeta() { }

	public FieldMeta(ClassMeta classMeta, Class<?> fieldType, String fieldName)
	{
		if (classMeta == null)
			throw new IllegalArgumentException("classMeta == null");

		if (fieldType == null)
			throw new IllegalArgumentException("fieldType == null");

		if (fieldName == null)
			throw new IllegalArgumentException("fieldName == null");

		this.classMeta = classMeta;
		this.fieldTypePackageName = fieldType.getPackage() == null ? "" : fieldType.getPackage().getName();
		this.fieldTypeSimpleClassName = fieldType.getSimpleName();
		this.fieldName = fieldName;
	}

	public long getFieldID() {
		return fieldID;
	}

	/**
	 * Get the {@link ClassMeta} to which this <code>FieldMeta</code> belongs. Every FieldMeta
	 * belongs to exactly one {@link ClassMeta} just like a field is declared in exactly one Java class.
	 * @return the {@link ClassMeta} to which this instance of <code>FieldMeta</code> belongs.
	 */
	public ClassMeta getClassMeta() {
		return classMeta;
	}

	/**
	 * Get the package name or an empty <code>String</code> for the default package.
	 * @return the package name (maybe empty, but never <code>null</code>).
	 */
	public String getFieldTypePackageName() {
		return fieldTypePackageName;
	}
	public String getFieldTypeSimpleClassName() {
		return fieldTypeSimpleClassName;
	}

	/**
	 * Get the fully qualified class-name of the field's type.
	 * @return the fully qualified class-name of the field's type.
	 */
	public String getFieldTypeClassName() {
		if (fieldTypePackageName.isEmpty())
			return fieldTypeSimpleClassName;
		else
			return fieldTypePackageName + '.' + fieldTypeSimpleClassName;
	}

	/**
	 * Get the simple field name (no class prefix) of the field referenced by this meta-data-instance.
	 * @return the simple field name.
	 */
	public String getFieldName() {
		return fieldName;
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
}
