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
package org.cumulus4j.keymanager.back.shared;

import javax.xml.bind.annotation.XmlRootElement;

/**
 * {@link Response} implementation not containing any data and symbolising Java's <code>null</code> value.
 *
 * @author Marco หงุ่ยตระกูล-Schulze - marco at nightlabs dot de
 */
@XmlRootElement
public class NullResponse
extends Response
{
	private static final long serialVersionUID = 1L;

	/**
	 * Create a <code>NullResponse</code> without a prior request. This is just used as filler
	 * without being forwarded to any requester. It circumvents the problem that Jersey has a problem
	 * when a REST service (aka resource) method expecting an entity should be called without one.
	 */
	public NullResponse() { }

	/**
	 * Create a <code>NullResponse</code> as answer to a prior request. It is
	 * processed like any other response, i.e. forwarded to the requester, but finally
	 * translated to <code>null</code>
	 * (<code>org.cumulus4j.store.crypto.keymanager.messagebroker.MessageBroker.query(Class<R>, Request)</code>
	 * never returns a <code>NullResponse</code> instance; instead it returns <code>null</code>).
	 * @param request the request that is answered by this new <code>NullResponse</code> instance.
	 */
	public NullResponse(Request request)
	{
		super(request);
	}
}
