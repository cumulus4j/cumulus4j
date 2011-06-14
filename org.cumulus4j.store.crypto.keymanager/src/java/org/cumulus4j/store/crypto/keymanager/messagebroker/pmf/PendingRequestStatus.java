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
package org.cumulus4j.store.crypto.keymanager.messagebroker.pmf;

/**
 * A <code>PendingRequest</code>'s {@link PendingRequest#getStatus() status}.
 * <p>
 * The {@link MessageBrokerPMF} first assigns the status {@link #waitingForProcessing}
 * when a new request comes in when a thread enters the
 * {@link MessageBrokerPMF#query(Class, org.cumulus4j.keymanager.back.shared.Request)
 * query(...)} method.
 * </p>
 * <p>
 * When a key-manager obtains the request by calling {@link MessageBrokerPMF#pollRequest(String) pollRequest(...)},
 * the status changes to {@link #currentlyBeingProcessed}.
 * </p>
 * <p>
 * Finally, when the {@link MessageBrokerPMF#pushResponse(org.cumulus4j.keymanager.back.shared.Response) pushResponse(...)}
 * method is called, the response is associated with the request
 * (via {@link PendingRequest#setResponse(org.cumulus4j.keymanager.back.shared.Response)}) and the status advances to {@link #completed}.
 * </p>
 * <p>
 * When the {@link MessageBrokerPMF#query(Class, org.cumulus4j.keymanager.back.shared.Request)
 * query(...)} method picks up the response, it finally deletes the {@link PendingRequest} from the datastore.
 * </p>
 *
 * @author Marco หงุ่ยตระกูล-Schulze - marco at nightlabs dot de
 */
public enum PendingRequestStatus
{
	waitingForProcessing,
	currentlyBeingProcessed,
	completed
}
