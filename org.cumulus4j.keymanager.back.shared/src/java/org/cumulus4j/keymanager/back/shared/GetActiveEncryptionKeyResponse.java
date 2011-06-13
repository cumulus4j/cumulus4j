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

import java.util.Date;

import javax.xml.bind.annotation.XmlRootElement;

/**
 * @author Marco หงุ่ยตระกูล-Schulze - marco at nightlabs dot de
 */
@XmlRootElement
public class GetActiveEncryptionKeyResponse extends GetKeyResponse
{
	private static final long serialVersionUID = 1L;

	private Date activeUntilExcl;

	public GetActiveEncryptionKeyResponse() { }

	public GetActiveEncryptionKeyResponse(Request request, long keyID, byte[] keyEncodedEncrypted, Date activeUntilExcl)
	{
		super(request, keyID, keyEncodedEncrypted);
		this.activeUntilExcl = activeUntilExcl;
	}

	public Date getActiveUntilExcl() {
		return activeUntilExcl;
	}
	public void setActiveUntilExcl(Date activeUntilExcl) {
		this.activeUntilExcl = activeUntilExcl;
	}
}
