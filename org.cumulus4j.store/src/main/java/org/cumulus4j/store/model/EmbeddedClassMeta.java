package org.cumulus4j.store.model;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicLong;

import javax.jdo.JDOHelper;
import javax.jdo.PersistenceManager;
import javax.jdo.annotations.Column;
import javax.jdo.annotations.Discriminator;
import javax.jdo.annotations.DiscriminatorStrategy;
import javax.jdo.annotations.IdentityType;
import javax.jdo.annotations.Inheritance;
import javax.jdo.annotations.InheritanceStrategy;
import javax.jdo.annotations.NotPersistent;
import javax.jdo.annotations.PersistenceCapable;
import javax.jdo.annotations.Queries;
import javax.jdo.annotations.Query;
import javax.jdo.annotations.Unique;
import javax.jdo.annotations.Uniques;

import org.datanucleus.store.ExecutionContext;

@PersistenceCapable(identityType=IdentityType.APPLICATION, detachable="true")
@Inheritance(strategy=InheritanceStrategy.NEW_TABLE)
@Discriminator(strategy=DiscriminatorStrategy.VALUE_MAP, value="EmbeddedClassMeta")
@Uniques({
	@Unique(members={"embeddingFieldMeta_fieldID"}, columns=@Column(name="discriminator"))
})
@Queries({
	@Query(
			name=EmbeddedClassMeta.NamedQueries.getEmbeddedClassMetaByEmbeddingFieldMeta_fieldID,
			value="SELECT UNIQUE FROM org.cumulus4j.store.model.EmbeddedClassMeta EXCLUDE SUBCLASSES WHERE this.embeddingFieldMeta_fieldID == :embeddingFieldMeta_fieldID"
	)
})
public class EmbeddedClassMeta extends ClassMeta {

	protected static final String UNIQUE_SCOPE_PREFIX_EMBEDDED_CLASS_META = EmbeddedClassMeta.class.getSimpleName() + '.';

	protected static class NamedQueries {
		public static final String getEmbeddedClassMetaByEmbeddingFieldMeta_fieldID = "getEmbeddedClassMetaByEmbeddingFieldMeta_fieldID";
	}

//	@Persistent(nullValue=NullValue.EXCEPTION)
	@NotPersistent
	private ClassMeta nonEmbeddedClassMeta;

	private long nonEmbeddedClassMeta_classID;

//	@Persistent(nullValue=NullValue.EXCEPTION)
	@NotPersistent
	private FieldMeta embeddingFieldMeta;

	private long embeddingFieldMeta_fieldID;

	@NotPersistent
	private Map<FieldMeta, EmbeddedFieldMeta> nonEmbeddedFieldMeta2EmbeddedFieldMeta;

	protected EmbeddedClassMeta() { }

	public EmbeddedClassMeta(ExecutionContext executionContext, ClassMeta nonEmbeddedClassMeta, FieldMeta embeddingFieldMeta) {
		super(executionContext.getClassLoaderResolver().classForName(nonEmbeddedClassMeta.getClassName()));
		if (embeddingFieldMeta == null)
			throw new IllegalArgumentException("embeddingFieldMeta == null");

		if (nonEmbeddedClassMeta instanceof EmbeddedClassMeta)
//			nonEmbeddedClassMeta = ((EmbeddedClassMeta) nonEmbeddedClassMeta).getNonEmbeddedClassMeta();
			throw new IllegalArgumentException("nonEmbeddedClassMeta is an instance of EmbeddedClassMeta: " + nonEmbeddedClassMeta);

		this.nonEmbeddedClassMeta = nonEmbeddedClassMeta;
		this.nonEmbeddedClassMeta_classID = nonEmbeddedClassMeta.getClassID();
		this.embeddingFieldMeta = embeddingFieldMeta;
		this.embeddingFieldMeta_fieldID = embeddingFieldMeta.getFieldID();
		setUniqueScope(null); // set in jdoPreStore, because id not assigned, yet
	}

	@Override
	public void addFieldMeta(FieldMeta fieldMeta) {
		if (!(fieldMeta instanceof EmbeddedFieldMeta)) {
			throw new IllegalArgumentException("fieldMeta is NOT an instance of EmbeddedFieldMeta: " + fieldMeta);
		}
		super.addFieldMeta(fieldMeta);
		Map<FieldMeta, EmbeddedFieldMeta> nefm2efmMap = nonEmbeddedFieldMeta2EmbeddedFieldMeta;
		if (nefm2efmMap != null) {
			EmbeddedFieldMeta embeddedFieldMeta = (EmbeddedFieldMeta) fieldMeta;
			nefm2efmMap.put(embeddedFieldMeta.getNonEmbeddedFieldMeta(), embeddedFieldMeta);
		}
	}

	@Override
	public void removeFieldMeta(FieldMeta fieldMeta) {
		super.removeFieldMeta(fieldMeta);
		nonEmbeddedFieldMeta2EmbeddedFieldMeta = null;
	}

	/**
	 * Get the non-embedded {@link ClassMeta} of which this instance is a reference wihtin the scope of
	 * the {@link #getEmbeddingFieldMeta()}.
	 * @return the non-embedded {@link ClassMeta} (the one representing FCOs). Never <code>null</code>.
	 */
	public ClassMeta getNonEmbeddedClassMeta() {
		if (nonEmbeddedClassMeta == null) {
			nonEmbeddedClassMeta = new ClassMetaDAO(getPersistenceManager()).getClassMeta(nonEmbeddedClassMeta_classID, true);
		}
		return nonEmbeddedClassMeta;
	}

	/**
	 * Get the field embedding this pseudo-class.
	 * <p>
	 * This may be an {@link EmbeddedFieldMeta}, if this is a nested-embedded-field-situation.
	 * @return the field embedding this pseudo-class. Never <code>null</code>.
	 */
	public FieldMeta getEmbeddingFieldMeta() {
		if (embeddingFieldMeta == null) {
			embeddingFieldMeta = new FieldMetaDAO(getPersistenceManager()).getFieldMeta(embeddingFieldMeta_fieldID, true);
		}
		return embeddingFieldMeta;
	}

	@Override
	public void jdoPreStore() {
		super.jdoPreStore();
		if (getUniqueScope() == null || !getUniqueScope().startsWith(UNIQUE_SCOPE_PREFIX_EMBEDDED_CLASS_META)) {
			setUniqueScope("TEMPORARY_" + UUID.randomUUID());
			if (nonEmbeddedClassMeta_classID < 0)
				nonEmbeddedClassMeta_classID = nextTemp_nonEmbeddedClassMeta_classID.decrementAndGet();

			if (embeddingFieldMeta_fieldID < 0)
				embeddingFieldMeta_fieldID = nextTemp_embeddingFieldMeta_fieldID.decrementAndGet();

			PostStoreRunnableManager.getInstance().addRunnable(new Runnable() {
				@Override
				public void run() {
					PersistenceManager pm = JDOHelper.getPersistenceManager(EmbeddedClassMeta.this);

					if (nonEmbeddedClassMeta_classID < 0 && nonEmbeddedClassMeta != null) {
						nonEmbeddedClassMeta = pm.makePersistent(nonEmbeddedClassMeta);
						nonEmbeddedClassMeta_classID = nonEmbeddedClassMeta.getClassID();
					}

					if (nonEmbeddedClassMeta_classID < 0)
						throw new IllegalStateException("nonEmbeddedClassMeta_classID < 0");

					if (embeddingFieldMeta_fieldID < 0 && embeddingFieldMeta != null) {
						embeddingFieldMeta = pm.makePersistent(embeddingFieldMeta);
						embeddingFieldMeta_fieldID = embeddingFieldMeta.getFieldID();
					}

					if (embeddingFieldMeta_fieldID < 0)
						throw new IllegalStateException("embeddingFieldMeta_fieldID < 0");

					embeddingFieldMeta = pm.makePersistent(embeddingFieldMeta);
					setUniqueScope(UNIQUE_SCOPE_PREFIX_EMBEDDED_CLASS_META + embeddingFieldMeta.getFieldID());

					pm.flush();
				}
			});
		}
	}

	private static AtomicLong nextTemp_nonEmbeddedClassMeta_classID = new AtomicLong();
	private static AtomicLong nextTemp_embeddingFieldMeta_fieldID = new AtomicLong();

	/**
	 * Get the {@link FieldMeta} managed by this instances corresponding to the given <code>fieldMeta</code>.
	 * <p>
	 * The given <code>fieldMeta</code> can be a sub-FieldMeta (not directly assigned to the corresponding ClassMeta,
	 * but assigned to one of its FieldMetas).
	 * @param fieldMeta a non-embedded {@link FieldMeta} (i.e. <b>not</b> an instance of {@link EmbeddedFieldMeta}).
	 * @return
	 */
	public EmbeddedFieldMeta getEmbeddedFieldMetaForNonEmbeddedFieldMeta(FieldMeta fieldMeta) {
		if (fieldMeta == null)
			throw new IllegalArgumentException("fieldMeta == null");

		if (fieldMeta instanceof EmbeddedFieldMeta)
			throw new IllegalArgumentException("fieldMeta is an instance of EmbeddedFieldMeta, but it should be a non-embedded FieldMeta!");

		if (!this.getNonEmbeddedClassMeta().equals(fieldMeta.getClassMeta()))
			throw new IllegalArgumentException("fieldMeta.classMeta != this.nonEmbeddedClassMeta");

		if (nonEmbeddedFieldMeta2EmbeddedFieldMeta == null) {
			Map<FieldMeta, EmbeddedFieldMeta> m = new HashMap<FieldMeta, EmbeddedFieldMeta>();
			for (FieldMeta efm : getFieldMetasWithSubFieldMetas()) {
				EmbeddedFieldMeta embeddedFieldMeta = (EmbeddedFieldMeta) efm;
				m.put(embeddedFieldMeta.getNonEmbeddedFieldMeta(), embeddedFieldMeta);
			}
			nonEmbeddedFieldMeta2EmbeddedFieldMeta = m;
		}
		return nonEmbeddedFieldMeta2EmbeddedFieldMeta.get(fieldMeta);
	}

	protected Collection<FieldMeta> getFieldMetasWithSubFieldMetas() {
		Collection<FieldMeta> result = new ArrayList<FieldMeta>();
		for (FieldMeta fieldMeta : getFieldMetas()) {
			populateFieldMetasWithSubFieldMetas(result, fieldMeta);
		}
		return result;
	}
	protected void populateFieldMetasWithSubFieldMetas(Collection<FieldMeta> result, FieldMeta fieldMeta) {
		result.add(fieldMeta);
		for (FieldMeta subFieldMeta : fieldMeta.getSubFieldMetas()) {
			populateFieldMetasWithSubFieldMetas(result, subFieldMeta);
		}
	}

	@Override
	public void jdoPostDetach(Object o) {
		super.jdoPostDetach(o);
		PostDetachRunnableManager.getInstance().addRunnable(new Runnable() {
			@Override
			public void run() {
				DetachedClassMetaModel detachedClassMetaModel = DetachedClassMetaModel.getInstance();
				if (detachedClassMetaModel != null)
					nonEmbeddedClassMeta = detachedClassMetaModel.getClassMeta(nonEmbeddedClassMeta_classID, true);
			}
		});
	}
}
