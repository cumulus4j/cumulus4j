package org.cumulus4j.store.model;

import java.util.Collection;
import java.util.List;
import java.util.Set;

/**
 * Enum for the {@link FieldMeta#getRole() role} of a {@link FieldMeta}.
 * @author Marco หงุ่ยตระกูล-Schulze - marco at nightlabs dot de
 */
public enum FieldMetaRole
{
	/**
	 * Only those {@link FieldMeta} instances with this role are registered in {@link ClassMeta} directly.
	 * All others are registered as sub-FieldMeta.
	 */
	primary,

	/**
	 * Sub-field (aka part of a real field) used when the primary field is of type {@link Collection}
	 * (or a sub-type like {@link List}, {@link Set} etc.).
	 */
	collectionElement,

	/**
	 * Sub-field (aka part of a real field) used when the primary field is an array.
	 */
	arrayElement,

	/**
	 * Sub-field (aka part of a real field) used when the primary field is of type {@link Map}
	 * and this sub-field is the <code>Map</code>'s key.
	 */
	mapKey,

	/**
	 * Sub-field (aka part of a real field) used when the primary field is of type {@link Map}
	 * and this sub-field is the <code>Map</code>'s value.
	 */
	mapValue
}
