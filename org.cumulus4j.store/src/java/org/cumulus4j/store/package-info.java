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
 * Cumulus4j is a plug-in for <a href="http://www.datanucleus.org">DataNucleus</a> providing encrypted data-storage.
 * </p>
 * <p>
 * Most classes in this package are extensions using DataNucleus' extension-points in order to implement
 * reading and writing of objects. DataNucleus uses the Eclipse extension mechanism. If you are not
 * familiar with Eclipse extensions, start reading here:
 * <a href="http://wiki.eclipse.org/FAQ_What_are_extensions_and_extension_points%3F">Eclipse FAQ
 * What are extensions and extension points?</a>
 * </p>
 * <p>
 * On one side, Cumulus4j is accessed by the frontend (i.e. the API consumer) via the following APIs:
 * </p>
 * <ul>
 * 	<li>Object-oriented persistence via JDO or JPA.</li>
 * 	<li>Key management API.</li>
 * </ul>
 * <p>
 * On the other side, Cumulus4j accesses the backend (i.e. another data storage) via a 2nd object-oriented
 * persistence layer instance - more precisely an instance of {@link javax.jdo.PersistenceManagerFactory}.
 * </p>
 */
package org.cumulus4j.store;