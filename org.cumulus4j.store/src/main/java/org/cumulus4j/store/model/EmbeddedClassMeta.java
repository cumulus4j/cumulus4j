package org.cumulus4j.store.model;

import javax.jdo.annotations.Discriminator;
import javax.jdo.annotations.DiscriminatorStrategy;
import javax.jdo.annotations.IdentityType;
import javax.jdo.annotations.NullValue;
import javax.jdo.annotations.PersistenceCapable;
import javax.jdo.annotations.Persistent;

import org.datanucleus.store.ExecutionContext;

@PersistenceCapable(identityType=IdentityType.APPLICATION, detachable="true")
@Discriminator(strategy=DiscriminatorStrategy.VALUE_MAP, value="EmbeddedClassMeta")
public class EmbeddedClassMeta extends ClassMeta {

	@Persistent(nullValue=NullValue.EXCEPTION)
	private ClassMeta nonEmbeddedClassMeta;

	@Persistent(nullValue=NullValue.EXCEPTION)
	private FieldMeta embeddingField;

	protected EmbeddedClassMeta() { }

	public EmbeddedClassMeta(ExecutionContext executionContext, ClassMeta nonEmbeddedClassMeta, FieldMeta embeddingField) {
		super(executionContext.getClassLoaderResolver().classForName(nonEmbeddedClassMeta.getClassName()));
		if (embeddingField == null)
			throw new IllegalArgumentException("embeddingField == null");

		if (nonEmbeddedClassMeta instanceof EmbeddedClassMeta)
//			nonEmbeddedClassMeta = ((EmbeddedClassMeta) nonEmbeddedClassMeta).getNonEmbeddedClassMeta();
			throw new IllegalArgumentException("nonEmbeddedClassMeta is an instance of EmbeddedClassMeta: " + nonEmbeddedClassMeta);

		this.nonEmbeddedClassMeta = nonEmbeddedClassMeta;
		this.embeddingField = embeddingField;
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
	 * the {@link #getEmbeddingField()}.
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
	public FieldMeta getEmbeddingField() {
		return embeddingField;
	}
}
