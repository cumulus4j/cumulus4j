/*
 * Cumulus4j - Securing your data in the cloud - http://cumulus4j.org
 * Copyright (C) 2011 NightLabs Consulting GmbH
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.cumulus4j.store.model;

import java.util.Collection;
import java.util.List;
import java.util.Map;
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
