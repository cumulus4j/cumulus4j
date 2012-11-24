package org.cumulus4j.store.model;

import javax.jdo.JDOHelper;
import javax.jdo.PersistenceManager;
import javax.jdo.annotations.Discriminator;
import javax.jdo.annotations.DiscriminatorStrategy;
import javax.jdo.annotations.IdentityType;
import javax.jdo.annotations.Inheritance;
import javax.jdo.annotations.InheritanceStrategy;
import javax.jdo.annotations.NullValue;
import javax.jdo.annotations.PersistenceCapable;
import javax.jdo.annotations.Persistent;

import org.datanucleus.metadata.AbstractMemberMetaData;
import org.datanucleus.store.ExecutionContext;

@PersistenceCapable(identityType=IdentityType.APPLICATION, detachable="true")
@Inheritance(strategy=InheritanceStrategy.NEW_TABLE)
@Discriminator(strategy=DiscriminatorStrategy.VALUE_MAP, value="EmbeddedFieldMeta")
public class EmbeddedFieldMeta extends FieldMeta {

	protected static final String UNIQUE_SCOPE_PREFIX_EMBEDDED_FIELD_META = EmbeddedFieldMeta.class.getSimpleName() + '.';

	@Persistent(nullValue=NullValue.EXCEPTION)
	private FieldMeta nonEmbeddedFieldMeta;

	protected EmbeddedFieldMeta() { }

	public EmbeddedFieldMeta(
			EmbeddedClassMeta classMeta, EmbeddedFieldMeta ownerFieldMeta, FieldMeta nonEmbeddedFieldMeta
	)
	{
		super(classMeta, ownerFieldMeta, nonEmbeddedFieldMeta.getFieldName(), nonEmbeddedFieldMeta.getRole());
		this.nonEmbeddedFieldMeta = nonEmbeddedFieldMeta;
		setUniqueScope(null); // is set in jdoPreStore
	}
	//other
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

	public FieldMeta getNonEmbeddedFieldMeta() {
		return nonEmbeddedFieldMeta;
	}

	/**
	 * Get the field which is embedding the object having this field.
	 * <p>
	 * This is a convenience method for
	 * {@link #getClassMeta()}.{@link EmbeddedClassMeta#getEmbeddingFieldMeta() getEmbeddingFieldMeta()}.
	 * @return the field which is embedding the object having this field. Never <code>null</code>.
	 */
	public FieldMeta getEmbeddingFieldMeta() {
		return getClassMeta().getEmbeddingFieldMeta();
	}

	@Override
	public void jdoPreStore() {
		super.jdoPreStore();
		PersistenceManager pm = JDOHelper.getPersistenceManager(this);
		FieldMeta embeddingFieldMeta = pm.makePersistent(getEmbeddingFieldMeta());
		setUniqueScope(UNIQUE_SCOPE_PREFIX_EMBEDDED_FIELD_META + embeddingFieldMeta.getFieldID());
	}

	@Override
	public void jdoPostDetach(Object o) {
		super.jdoPostDetach(o);
		PostDetachRunnableManager.getInstance().addRunnable(new Runnable() {
			@Override
			public void run() {
				DetachedClassMetaModel detachedClassMetaModel = DetachedClassMetaModel.getInstance();
				if (detachedClassMetaModel != null) {
					ClassMeta detachedClassMeta = detachedClassMetaModel.getClassMeta(nonEmbeddedFieldMeta.getClassMeta().getClassID(), true);

					FieldMeta nefm = detachedClassMeta.getFieldMeta(nonEmbeddedFieldMeta.getFieldID());
					if (nefm == null)
						throw new IllegalStateException("detachedClassMeta.getFieldMeta(...) returned null for " + nonEmbeddedFieldMeta);

					nonEmbeddedFieldMeta = nefm;
				}
			}
		});
	}

	@Override
	public int getDataNucleusAbsoluteFieldNumber(ExecutionContext executionContext) {
		return getNonEmbeddedFieldMeta().getDataNucleusAbsoluteFieldNumber(executionContext);
	}

	@Override
	public int getDataNucleusAbsoluteFieldNumber() {
		return getNonEmbeddedFieldMeta().getDataNucleusAbsoluteFieldNumber();
	}

	@Override
	public void setDataNucleusAbsoluteFieldNumber(int dataNucleusAbsoluteFieldNumber) {
		throw new UnsupportedOperationException("This delegate property cannot be set!");
	}

	@Override
	public AbstractMemberMetaData getDataNucleusMemberMetaData(ExecutionContext executionContext) {
		return getNonEmbeddedFieldMeta().getDataNucleusMemberMetaData(executionContext);
	}

	@Override
	public String toString() {
		return super.toString() + "\nembedded in\n" + getEmbeddingFieldMeta();
	}
}
