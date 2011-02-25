package org.cumulus4j.test.model;

import javax.jdo.annotations.Column;
import javax.jdo.annotations.IdGeneratorStrategy;
import javax.jdo.annotations.IdentityType;
import javax.jdo.annotations.NullValue;
import javax.jdo.annotations.PersistenceCapable;
import javax.jdo.annotations.Persistent;
import javax.jdo.annotations.PrimaryKey;

@PersistenceCapable(identityType=IdentityType.APPLICATION)
public class ClassMeta
{
	@PrimaryKey
	@Persistent(valueStrategy=IdGeneratorStrategy.NATIVE)
	private long classID = -1;

	@Persistent(nullValue=NullValue.EXCEPTION)
	@Column(length=255)
	private String packageName;

	@Persistent(nullValue=NullValue.EXCEPTION)
	@Column(length=255)
	private String simpleClassName;

	protected ClassMeta() { }

	public ClassMeta(Class<?> clazz) {
		this.packageName = clazz.getPackage() == null ? "" : clazz.getPackage().getName();
		this.simpleClassName = clazz.getSimpleName();
	}

	public long getClassID() {
		return classID;
	}

	/**
	 * Get the package name or an empty <code>String</code> for the default package.
	 * @return the package name (maybe empty, but never <code>null</code>).
	 */
	public String getPackageName() {
		return packageName;
	}

	public String getClassName() {
		return simpleClassName;
	}
}
