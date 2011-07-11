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
 * REST API for the communication between client and key-server.
 * </p>
 * <p>
 * As documented in <a href="http://cumulus4j.org/1.0.0/documentation/deployment-scenarios.html#3-computer-scenario">Deployment scenarios / 3-computer-scenario</a>,
 * the client controls via a crypto-session when the app-server is granted access to the key-manager.
 * The REST API provided by this package is the green arrow in the graphics of the 'Deployment scenarios', section
 * '3-computer-scenario (with key-server)'.
 * </p>
 */
package org.cumulus4j.keymanager.front.webapp;
