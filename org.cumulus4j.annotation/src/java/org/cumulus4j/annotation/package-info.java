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
 * Annotations to enhance the specification capabilities of Cumulus4j. For example, they can be used
 * to define which fields are queryable (and hence indexed) and which aren't. You can typically avoid use of these
 * annotations by using the standard JDO &#64;Extension annotation, but they are here since could be considered
 * more user-friendly (with the downside of imposing an extra dependency).
 * </p>
 */
package org.cumulus4j.annotation;