package org.cumulus4j.store.model;

public enum FieldMetaRole
{
	/**
	 * Only those {@link FieldMeta} instances with this role are registered in {@link ClassMeta} directly.
	 * All others are registered as sub-FieldMeta.
	 */
	primary,
	collectionElement,
	arrayElement,
	mapKey,
	mapValue
}
