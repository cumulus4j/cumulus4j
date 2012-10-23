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
 * REST API for the communication between key-manager and application-server.
 * </p>
 * <p>
 * As documented in <a target="_blank" href="http://cumulus4j.org/${project.version}/documentation/deployment-scenarios.html">Deployment scenarios</a>,
 * TCP connections are always established from the key-manager (i.e. client or key-server) to the application server.
 * The REST API provided by this package is the yellow + red arrows in the graphics of the 'Deployment scenarios'.
 * </p>
 */
package org.cumulus4j.store.crypto.keymanager.rest;
