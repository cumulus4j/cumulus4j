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
 * Package providing handlers for various JDOQL/JPQL methods, utilising the index information
 * and querying the backend datastore as necessary to access the DataEntry objects matching the
 * particular method clause. Note that each method handler will typically evaluate a Boolean
 * clause. This is simple for things like "Collection.isEmpty" which returns boolean, but for
 * methods like String.substring() it has to be part of a comparison with some other String, and
 * consequently caters for a most-used subset of the possible combinations.
 * </p>
 */
package org.cumulus4j.store.query.method;