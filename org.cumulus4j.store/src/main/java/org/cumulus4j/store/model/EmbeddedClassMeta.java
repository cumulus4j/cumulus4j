package org.cumulus4j.store.model;

import javax.jdo.JDOHelper;
import javax.jdo.PersistenceManager;
import javax.jdo.annotations.Column;
import javax.jdo.annotations.Discriminator;
import javax.jdo.annotations.DiscriminatorStrategy;
import javax.jdo.annotations.IdentityType;
import javax.jdo.annotations.Inheritance;
import javax.jdo.annotations.InheritanceStrategy;
import javax.jdo.annotations.NullValue;
import javax.jdo.annotations.PersistenceCapable;
import javax.jdo.annotations.Persistent;
import javax.jdo.annotations.Queries;
import javax.jdo.annotations.Query;
import javax.jdo.annotations.Unique;
import javax.jdo.annotations.Uniques;

import org.datanucleus.store.ExecutionContext;

@PersistenceCapable(identityType=IdentityType.APPLICATION, detachable="true")
@Inheritance(strategy=InheritanceStrategy.NEW_TABLE)
@Discriminator(strategy=DiscriminatorStrategy.VALUE_MAP, value="EmbeddedClassMeta")
@Uniques({
	@Unique(members={"embeddingFieldMeta"}, columns=@Column(name="discriminator"))
})
@Queries({
	@Query(
			name=EmbeddedClassMeta.NamedQueries.getEmbeddedClassMetaByEmbeddingFieldMeta,
			value="SELECT UNIQUE FROM org.cumulus4j.store.model.EmbeddedClassMeta EXCLUDE SUBCLASSES WHERE this.embeddingFieldMeta == :embeddingFieldMeta"
	)
})
public class EmbeddedClassMeta extends ClassMeta {

	protected static final String UNIQUE_SCOPE_PREFIX_EMBEDDED_CLASS_META = EmbeddedClassMeta.class.getSimpleName() + '.';

	protected static class NamedQueries {
		public static final String getEmbeddedClassMetaByEmbeddingFieldMeta = "getEmbeddedClassMetaByEmbeddingFieldMeta";
	}

	@Persistent(nullValue=NullValue.EXCEPTION)
	private ClassMeta nonEmbeddedClassMeta;

	@Persistent(nullValue=NullValue.EXCEPTION)
	private FieldMeta embeddingFieldMeta;

	protected EmbeddedClassMeta() { }

	public EmbeddedClassMeta(ExecutionContext executionContext, ClassMeta nonEmbeddedClassMeta, FieldMeta embeddingFieldMeta) {
		super(executionContext.getClassLoaderResolver().classForName(nonEmbeddedClassMeta.getClassName()));
		if (embeddingFieldMeta == null)
			throw new IllegalArgumentException("embeddingFieldMeta == null");

		if (nonEmbeddedClassMeta instanceof EmbeddedClassMeta)
//			nonEmbeddedClassMeta = ((EmbeddedClassMeta) nonEmbeddedClassMeta).getNonEmbeddedClassMeta();
			throw new IllegalArgumentException("nonEmbeddedClassMeta is an instance of EmbeddedClassMeta: " + nonEmbeddedClassMeta);

		this.nonEmbeddedClassMeta = nonEmbeddedClassMeta;
		this.embeddingFieldMeta = embeddingFieldMeta;
		setUniqueScope(null); // set in jdoPreStore, because id not assigned, yet
	}

	@Override
	public void addFieldMeta(FieldMeta fieldMeta) {
		if (!(fieldMeta instanceof EmbeddedFieldMeta)) {
			throw new IllegalArgumentException("fieldMeta is NOT an instance of EmbeddedFieldMeta: " + fieldMeta);
		}
		super.addFieldMeta(fieldMeta);
	}

	/**
	 * Get the non-embedded {@link ClassMeta} of which this instance is a reference wihtin the scope of
	 * the {@link #getEmbeddingFieldMeta()}.
	 * @return the non-embedded {@link ClassMeta} (the one representing FCOs). Never <code>null</code>.
	 */
	public ClassMeta getNonEmbeddedClassMeta() {
		return nonEmbeddedClassMeta;
	}

	/**
	 * Get the field embedding this pseudo-class.
	 * <p>
	 * This may be an {@link EmbeddedFieldMeta}, if this is a nested-embedded-field-situation.
	 * @return the field embedding this pseudo-class. Never <code>null</code>.
	 */
	public FieldMeta getEmbeddingFieldMeta() {
		return embeddingFieldMeta;
	}

	@Override
	public void jdoPreStore() {
		super.jdoPreStore();
		PersistenceManager pm = JDOHelper.getPersistenceManager(this);
		embeddingFieldMeta = pm.makePersistent(embeddingFieldMeta);
		setUniqueScope(UNIQUE_SCOPE_PREFIX_EMBEDDED_CLASS_META + embeddingFieldMeta.getFieldID());
	}
}
