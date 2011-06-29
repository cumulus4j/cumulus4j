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
package org.cumulus4j.keymanager.channel;

import org.cumulus4j.keymanager.back.shared.ErrorResponse;
import org.cumulus4j.keymanager.back.shared.NullResponse;
import org.cumulus4j.keymanager.back.shared.Request;
import org.cumulus4j.keymanager.back.shared.Response;

/**
 * <p>
 * Handler processing and replying requests coming from the application server.
 * </p>
 * <p>
 * The so-called "key manager channel" is - as shown in the document
 * <a href="http://www.cumulus4j.org/1.0.0/documentation/deployment-scenarios.html">Deployment scenarios</a> - an
 * HTTP(S) connection from the key-manager to the application server with an inverse request-response-cycle.
 * This means, the application server sends a {@link Request},
 * the key manager handles it and then sends a {@link Response} back.
 * </p>
 * <p>
 * For every {@link Request} type (i.e. subclass), there's one implementation of <code>RequestHandler</code>
 * registered in the {@link KeyManagerChannelManager}. For every incoming <code>Request</code>, the
 * <code>KeyManagerChannelManager</code> instantiates an appropriate <code>RequestHandler</code> implementation,
 * initialises it (i.e. calls {@link #setKeyManagerChannelManager(KeyManagerChannelManager)}) and then calls
 * {@link #handle(Request)}.
 * </p>
 * <p>
 * If handling a request fails, an {@link ErrorResponse} is sent to the server.
 * </p>
 * <p>
 * <b>Important:</b> You should not directly implement this interface but instead subclass {@link AbstractRequestHandler}!
 * </p>
 *
 * @author Marco หงุ่ยตระกูล-Schulze - marco at nightlabs dot de
 *
 * @param <R> the request type for which this request handler is responsible.
 */
public interface RequestHandler<R extends Request>
{
	/**
	 * Get the {@link KeyManagerChannelManager}.
	 * @return the {@link KeyManagerChannelManager} or <code>null</code>, if it has not yet been set.
	 */
	KeyManagerChannelManager getKeyManagerChannelManager();

	/**
	 * Set the {@link KeyManagerChannelManager}. This method is called by the <code>KeyManagerChannelManager</code>
	 * after instantiating a new <code>RequestHandler</code> instance and before invoking {@link #handle(Request)}.
	 * @param keyManagerChannelManager the {@link KeyManagerChannelManager}
	 */
	void setKeyManagerChannelManager(KeyManagerChannelManager keyManagerChannelManager);

	/**
	 * Handle the given request.
	 * @param request the request to be handled; never <code>null</code>.
	 * @return the response for the given request; can be <code>null</code>, which is sent
	 * as {@link NullResponse} to the server.
	 */
	Response handle(R request) throws Throwable;
}
