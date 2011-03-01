package org.cumulus4j.test.model;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import javax.jdo.JDOObjectNotFoundException;
import javax.jdo.PersistenceManager;
import javax.jdo.annotations.Column;
import javax.jdo.annotations.IdGeneratorStrategy;
import javax.jdo.annotations.IdentityType;
import javax.jdo.annotations.Key;
import javax.jdo.annotations.NotPersistent;
import javax.jdo.annotations.NullValue;
import javax.jdo.annotations.PersistenceCapable;
import javax.jdo.annotations.Persistent;
import javax.jdo.annotations.PrimaryKey;
import javax.jdo.annotations.Queries;
import javax.jdo.annotations.Query;
import javax.jdo.annotations.Unique;

/**
 * @author Marco หงุ่ยตระกูล-Schulze - marco at nightlabs dot de
 */
@PersistenceCapable(identityType=IdentityType.APPLICATION, detachable="true")
@Unique(name="ClassMeta_fullyQualifiedClassName", members={"packageName", "simpleClassName"})
@Queries({
		@Query(
				name="getClassMetaByPackageNameAndSimpleClassName",
				value="SELECT UNIQUE WHERE this.packageName == :packageName && this.simpleClassName == :simpleClassName"
		)
})
public class ClassMeta
{
	public static ClassMeta getClassMeta(PersistenceManager pm, String packageName, String simpleClassName, boolean throwExceptionIfNotFound)
	{
		javax.jdo.Query q = pm.newNamedQuery(ClassMeta.class, "getClassMetaByPackageNameAndSimpleClassName");
		ClassMeta result = (ClassMeta) q.execute(packageName, simpleClassName);

		if (result == null && throwExceptionIfNotFound)
			throw new JDOObjectNotFoundException(
					"No ClassMeta found for packageName=\"" + packageName + "\" and simpleClassName=\"" + simpleClassName + "\"!"
			);

		return result;
	}

	public static ClassMeta getClassMeta(PersistenceManager pm, Class<?> clazz, boolean throwExceptionIfNotFound)
	{
		String packageName = clazz.getPackage() == null ? "" : clazz.getPackage().getName();
		String simpleClassName = clazz.getSimpleName();
		return getClassMeta(pm, packageName, simpleClassName, throwExceptionIfNotFound);
	}


	@PrimaryKey
	@Persistent(valueStrategy=IdGeneratorStrategy.NATIVE)
	private long classID = -1;

	@Persistent(nullValue=NullValue.EXCEPTION)
	@Column(length=255)
	private String packageName;

	@Persistent(nullValue=NullValue.EXCEPTION)
	@Column(length=255)
	private String simpleClassName;

	private ClassMeta superClassMeta;

	@Persistent(mappedBy="classMeta")
	@Key(mappedBy="fieldName")
	private Map<String, FieldMeta> fieldName2fieldMeta;

	@NotPersistent
	private Map<Long, FieldMeta> fieldID2fieldMeta;

	protected ClassMeta() { }

	public ClassMeta(Class<?> clazz) {
		this.packageName = clazz.getPackage() == null ? "" : clazz.getPackage().getName();
		this.simpleClassName = clazz.getSimpleName();
		this.fieldName2fieldMeta = new HashMap<String, FieldMeta>();
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

	public String getSimpleClassName() {
		return simpleClassName;
	}

	public String getClassName() {
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

	public Collection<FieldMeta> getFieldMetas() {
		return fieldName2fieldMeta.values();
	}

	public FieldMeta getFieldMeta(String fieldName) {
		return fieldName2fieldMeta.get(fieldName);
	}

	public FieldMeta getFieldMeta(String className, String fieldName) {
		if (getClassName().equals(className))
			return getFieldMeta(fieldName);
		else if (superClassMeta != null)
			return superClassMeta.getFieldMeta(className, fieldName);
		else
			return null;
	}

	public FieldMeta getFieldMeta(long fieldID)
	{
		Map<Long, FieldMeta> m = fieldID2fieldMeta;

		if (m == null) {
			m = new HashMap<Long, FieldMeta>(fieldName2fieldMeta.size());
			for (FieldMeta fieldMeta : fieldName2fieldMeta.values())
				m.put(fieldMeta.getFieldID(), fieldMeta);

			fieldID2fieldMeta = m;
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

		fieldName2fieldMeta.put(fieldMeta.getFieldName(), fieldMeta);
		fieldID2fieldMeta = null;
	}

	public void removeFieldMeta(FieldMeta fieldMeta) {
		if (!this.equals(fieldMeta.getClassMeta()))
			throw new IllegalArgumentException("fieldMeta.classMeta != this");

		fieldName2fieldMeta.remove(fieldMeta.getFieldName());
		fieldID2fieldMeta = null;
	}

	@Override
	public int hashCode() {
		return (int) (classID ^ (classID >>> 32));
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) return true;
		if (obj == null) return false;
		if (getClass() != obj.getClass()) return false;
		ClassMeta other = (ClassMeta) obj;
		return this.classID == other.classID;
	}
}
