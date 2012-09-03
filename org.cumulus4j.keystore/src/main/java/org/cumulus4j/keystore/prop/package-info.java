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
/**
 * <p>
 * Property classes for the <code>KeyStore</code>'s property management.
 * </p>
 * <p>
 * The <code>KeyStore</code> supports managing arbitrary properties in the form of
 * name-value-pairs. The names are plain-text, but the values are encrypted.
 * A property-value can be of any type for which a subclass of
 * {@link org.cumulus4j.keystore.prop.Property} exists.
 * </p>
 * <p>
 * See {@link org.cumulus4j.keystore.KeyStore#getProperty(String, char[], Class, String)}
 * and {@link org.cumulus4j.keystore.prop.Property} for further infos.
 * </p>
 */
package org.cumulus4j.keystore.prop;
