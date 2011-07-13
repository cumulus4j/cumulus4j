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
 * {@link org.cumulus4j.keymanager.channel.RequestHandler Handlers} for the communication channel between
 * key manager and application server.
 * </p>
 * <p>
 * The so-called "key manager channel" is - as shown in the document
 * <a target="_blank" href="http://cumulus4j.org/${project.version}/documentation/deployment-scenarios.html">Deployment scenarios</a> - an
 * HTTP(S) connection from the key-manager to the application server with an inverse request-response-cycle.
 * This means, the application server sends a {@link org.cumulus4j.keymanager.back.shared.Request},
 * the key manager handles it and then sends a {@link org.cumulus4j.keymanager.back.shared.Response} back.
 * </p>
 * <p>
 * The classes in this package are the handlers responsible for processing these requests.
 * </p>
 */
package org.cumulus4j.keymanager.channel;
