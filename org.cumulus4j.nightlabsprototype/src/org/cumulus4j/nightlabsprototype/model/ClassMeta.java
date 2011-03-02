package org.cumulus4j.nightlabsprototype.model;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import javax.jdo.FetchPlan;
import javax.jdo.JDOObjectNotFoundException;
import javax.jdo.PersistenceManager;
import javax.jdo.annotations.Column;
import javax.jdo.annotations.FetchGroup;
import javax.jdo.annotations.FetchGroups;
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
 * Persistent meta-data for a persistence-capable {@link Class}. Since class names are very long,
 * we use the {@link #getClassID() classID} instead in our index and data entities (e.g. in the relation
 * {@link DataEntry#getClassMeta() DataEntry.classMeta}).
 *
 * @author Marco หงุ่ยตระกูล-Schulze - marco at nightlabs dot de
 */
@PersistenceCapable(identityType=IdentityType.APPLICATION, detachable="true")
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

	/**
	 * Get all {@link FieldMeta} instances known to this instance. This is the meta-data for all fields
	 * <b>directly declared</b> in the class referenced by this <code>ClassMeta</code> <b>not
	 * including super-classes</b>.
	 * @return
	 */
	public Collection<FieldMeta> getFieldMetas() {
		return fieldName2fieldMeta.values();
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
		return fieldName2fieldMeta.get(fieldName);
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
