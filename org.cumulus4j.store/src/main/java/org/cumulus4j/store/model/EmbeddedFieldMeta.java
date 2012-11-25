package org.cumulus4j.store.model;

import java.util.UUID;

import javax.jdo.JDOHelper;
import javax.jdo.PersistenceManager;
import javax.jdo.annotations.Discriminator;
import javax.jdo.annotations.DiscriminatorStrategy;
import javax.jdo.annotations.IdentityType;
import javax.jdo.annotations.Inheritance;
import javax.jdo.annotations.InheritanceStrategy;
import javax.jdo.annotations.NotPersistent;
import javax.jdo.annotations.PersistenceCapable;

import org.datanucleus.metadata.AbstractMemberMetaData;
import org.datanucleus.store.ExecutionContext;

@PersistenceCapable(identityType=IdentityType.APPLICATION, detachable="true")
@Inheritance(strategy=InheritanceStrategy.NEW_TABLE)
@Discriminator(strategy=DiscriminatorStrategy.VALUE_MAP, value="EmbeddedFieldMeta")
public class EmbeddedFieldMeta extends FieldMeta {

	protected static final String UNIQUE_SCOPE_PREFIX_EMBEDDED_FIELD_META = EmbeddedFieldMeta.class.getSimpleName() + '.';

//	@Persistent(nullValue=NullValue.EXCEPTION)
	@NotPersistent
	private FieldMeta nonEmbeddedFieldMeta;

	private long nonEmbeddedFieldMeta_fieldID;

	protected EmbeddedFieldMeta() { }

	public EmbeddedFieldMeta(
			EmbeddedClassMeta classMeta, EmbeddedFieldMeta ownerFieldMeta, FieldMeta nonEmbeddedFieldMeta
	)
	{
		super(classMeta, ownerFieldMeta, nonEmbeddedFieldMeta.getFieldName(), nonEmbeddedFieldMeta.getRole());
		this.nonEmbeddedFieldMeta = nonEmbeddedFieldMeta;
		this.nonEmbeddedFieldMeta_fieldID = nonEmbeddedFieldMeta.getFieldID();
		setUniqueScope(null); // is set in jdoPreStore
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

	public FieldMeta getNonEmbeddedFieldMeta() {
		if (nonEmbeddedFieldMeta == null) {
			nonEmbeddedFieldMeta = new FieldMetaDAO(getPersistenceManager()).getFieldMeta(nonEmbeddedFieldMeta_fieldID, true);
		}
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
		if (getUniqueScope() == null || !getUniqueScope().startsWith(UNIQUE_SCOPE_PREFIX_EMBEDDED_FIELD_META)) {
			setUniqueScope("TEMPORARY_" + UUID.randomUUID());

			PostStoreRunnableManager.getInstance().addRunnable(new Runnable() {
				@Override
				public void run() {
					PersistenceManager pm = JDOHelper.getPersistenceManager(EmbeddedFieldMeta.this);

					if (nonEmbeddedFieldMeta_fieldID < 0 && nonEmbeddedFieldMeta != null) {
						nonEmbeddedFieldMeta = pm.makePersistent(nonEmbeddedFieldMeta);
						nonEmbeddedFieldMeta_fieldID = nonEmbeddedFieldMeta.getFieldID();
					}

					if (nonEmbeddedFieldMeta_fieldID < 0)
						throw new IllegalStateException("nonEmbeddedFieldMeta_fieldID < 0");

					EmbeddedClassMeta classMeta = pm.makePersistent(getClassMeta());
					FieldMeta embeddingFieldMeta = pm.makePersistent(classMeta.getEmbeddingFieldMeta());
					if (embeddingFieldMeta == null)
						setUniqueScopePostponedInPostStore(pm, 1);
					else
						setUniqueScope(UNIQUE_SCOPE_PREFIX_EMBEDDED_FIELD_META + embeddingFieldMeta.getFieldID());

					pm.flush();
				}
			});
		}
	}

	protected void setUniqueScopePostponedInPostStore(final PersistenceManager pm, final int postponeCounter) {
		PostStoreRunnableManager.getInstance().addRunnable(new Runnable() {
			@Override
			public void run() {
				FieldMeta embeddingFieldMeta = pm.makePersistent(getEmbeddingFieldMeta());
				if (embeddingFieldMeta == null) {
					final int maxPostponeCounter = 30;
					if (postponeCounter > maxPostponeCounter)
						throw new IllegalStateException("postponeCounter > maxPostponeCounter :: " + postponeCounter + " > " + maxPostponeCounter);

					setUniqueScopePostponedInPostStore(pm, postponeCounter + 1);
				}
				else
					setUniqueScope(UNIQUE_SCOPE_PREFIX_EMBEDDED_FIELD_META + embeddingFieldMeta.getFieldID());
			}
		});
	}

	@Override
	public void jdoPostDetach(Object o) {
		final PostDetachRunnableManager postDetachRunnableManager = PostDetachRunnableManager.getInstance();
		postDetachRunnableManager.enterScope();
		try {
			super.jdoPostDetach(o);
			final EmbeddedFieldMeta detached = this;
			final EmbeddedFieldMeta attached = (EmbeddedFieldMeta) o;

			postDetachRunnableManager.addRunnable(new Runnable() {
				@Override
				public void run() {
					DetachedClassMetaModel detachedClassMetaModel = DetachedClassMetaModel.getInstance();
					if (detachedClassMetaModel != null) {
						FieldMeta nonEmbeddedFieldMeta = attached.getNonEmbeddedFieldMeta();
						ClassMeta detachedClassMeta = detachedClassMetaModel.getClassMeta(nonEmbeddedFieldMeta.getClassMeta().getClassID(), true);

						FieldMeta nefm = detachedClassMeta.getFieldMeta(nonEmbeddedFieldMeta_fieldID);
						if (nefm == null)
							throw new IllegalStateException("detachedClassMeta.getFieldMeta(...) returned null for " + nonEmbeddedFieldMeta);

						detached.nonEmbeddedFieldMeta = nefm;
					}
				}
			});
		} finally {
			postDetachRunnableManager.exitScope();
		}
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
