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
 * Key store managing keys safely in the local file system.
 * </p>
 * <p>
 * The {@link org.cumulus4j.keystore.KeyStore} is the most important class in this package. It manages a file
 * in the local file system in which it saves the keys
 * used by the Cumulus4j-DataNucleus-plug-in in an encrypted form. It is similar to a
 * <a href="http://download.oracle.com/javase/6/docs/api/java/security/KeyStore.html">Java-KeyStore</a>,
 * but specifically serves the needs of Cumulus4j.
 * </p>
 */
package org.cumulus4j.keystore;
