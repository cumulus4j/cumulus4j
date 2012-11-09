package org.cumulus4j.store.model;

import javax.jdo.annotations.Discriminator;
import javax.jdo.annotations.DiscriminatorStrategy;
import javax.jdo.annotations.IdentityType;
import javax.jdo.annotations.PersistenceCapable;

@PersistenceCapable(identityType=IdentityType.APPLICATION, detachable="true")
@Discriminator(strategy=DiscriminatorStrategy.VALUE_MAP, value="EmbeddedFieldMeta")
public class EmbeddedFieldMeta extends FieldMeta {

	protected EmbeddedFieldMeta() { }

	public EmbeddedFieldMeta(EmbeddedClassMeta classMeta, String fieldName) {
		super(classMeta, fieldName);
	}

	public EmbeddedFieldMeta(EmbeddedFieldMeta ownerFieldMeta, FieldMetaRole role) {
		super(ownerFieldMeta, role);
	}

	public EmbeddedFieldMeta(EmbeddedClassMeta classMeta, FieldMeta ownerFieldMeta, String fieldName, FieldMetaRole role) {
		super(classMeta, ownerFieldMeta, fieldName, role);
	}

	@Override
	public EmbeddedClassMeta getClassMeta() {
		return (EmbeddedClassMeta) super.getClassMeta();
	}

	@Override
	public void addSubFieldMeta(FieldMeta subFieldMeta) {
		if (!(subFieldMeta instanceof EmbeddedFieldMeta)) // must not be null anyway!
			throw new IllegalArgumentException("subFieldMeta is NOT an instance of EmbeddedFieldMeta: " + subFieldMeta);

		super.addSubFieldMeta(subFieldMeta);
	}

	@Override
	protected void setClassMeta(ClassMeta classMeta) {
		if (classMeta != null && !(classMeta instanceof EmbeddedClassMeta))
			throw new IllegalArgumentException("classMeta is NOT an instance of EmbeddedClassMeta: " + classMeta);

		super.setClassMeta(classMeta);
	}

	@Override
	protected void setOwnerFieldMeta(FieldMeta ownerFieldMeta) {
		if (ownerFieldMeta != null && !(ownerFieldMeta instanceof EmbeddedFieldMeta))
			throw new IllegalArgumentException("ownerFieldMeta is NOT an instance of EmbeddedFieldMeta: " + ownerFieldMeta);

		super.setOwnerFieldMeta(ownerFieldMeta);
	}
}
