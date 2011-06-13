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

import org.cumulus4j.keymanager.back.shared.Request;

/**
 * Abstract base class for {@link RequestHandler} implementations.
 * Implementors should subclass this class instead of directly implementing the
 * <code>RequestHandler</code> interface.
 *
 * @author Marco หงุ่ยตระกูล-Schulze - marco at nightlabs dot de
 *
 * @param <R> the request type for which this request handler is responsible.
 */
public abstract class AbstractRequestHandler<R extends Request> implements RequestHandler<R>
{
	private KeyManagerChannelManager keyManagerChannelManager;

	@Override
	public KeyManagerChannelManager getKeyManagerChannelManager() {
		return keyManagerChannelManager;
	}

	@Override
	public void setKeyManagerChannelManager(KeyManagerChannelManager keyManagerChannelManager) {
		this.keyManagerChannelManager = keyManagerChannelManager;
	}

}
