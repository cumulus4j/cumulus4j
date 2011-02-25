package org.cumulus4j.test.model;

import javax.jdo.annotations.Column;
import javax.jdo.annotations.IdGeneratorStrategy;
import javax.jdo.annotations.IdentityType;
import javax.jdo.annotations.NullValue;
import javax.jdo.annotations.PersistenceCapable;
import javax.jdo.annotations.Persistent;
import javax.jdo.annotations.PrimaryKey;

@PersistenceCapable(identityType=IdentityType.APPLICATION)
public class FieldMeta
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
}
