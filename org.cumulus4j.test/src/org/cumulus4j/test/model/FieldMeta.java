package org.cumulus4j.test.model;

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

@PersistenceCapable(identityType=IdentityType.APPLICATION, detachable="true")
@Unique(name="FieldMeta_classMeta_fieldName", members={"classMeta", "fieldName"})
//@Queries({
//		@Query(
//				name="getFieldMetaByClassMetaAndFieldName",
//				value="SELECT UNIQUE WHERE this.classMeta == :classMeta && this.fieldName == :fieldName"
//		)
//})
public class FieldMeta
implements DetachCallback
{
//	public static FieldMeta getFieldMeta(PersistenceManager pm, ClassMeta classMeta, String fieldName)
//	{
//		javax.jdo.Query q = pm.newNamedQuery(FieldMeta.class, "getFieldMetaByClassMetaAndFieldName");
//		return (FieldMeta) q.execute(classMeta, fieldName);
//	}

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

	public String getFieldName() {
		return fieldName;
	}

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
