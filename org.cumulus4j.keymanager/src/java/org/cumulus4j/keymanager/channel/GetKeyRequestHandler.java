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

import java.io.IOException;
import java.security.GeneralSecurityException;
import java.util.Date;

import org.bouncycastle.crypto.CryptoException;
import org.cumulus4j.keymanager.Session;
import org.cumulus4j.keymanager.SessionManager;
import org.cumulus4j.keymanager.back.shared.GetKeyRequest;
import org.cumulus4j.keymanager.back.shared.GetKeyResponse;
import org.cumulus4j.keymanager.back.shared.KeyEncryptionUtil;
import org.cumulus4j.keymanager.back.shared.Response;
import org.cumulus4j.keystore.AuthenticationException;
import org.cumulus4j.keystore.KeyNotFoundException;

/**
 * <p>
 * Handler for {@link GetKeyRequest}.
 * </p>
 * <p>
 * If the {@link Session} is found for the given
 * {@link org.cumulus4j.keymanager.back.shared.Request#getCryptoSessionID() cryptoSessionID} and
 * it is not {@link Session#isLocked() locked}, this handler looks up the desired key and
 * sends it in a {@link GetKeyResponse} to the server.
 * </p>
 *
 * @author Marco หงุ่ยตระกูล-Schulze - marco at nightlabs dot de
 */
public class GetKeyRequestHandler extends AbstractRequestHandler<GetKeyRequest>
{

	@Override
	public Response handle(GetKeyRequest request)
	throws AuthenticationException, KeyNotFoundException, GeneralSecurityException, IOException, CryptoException
	{
		SessionManager sessionManager = getKeyManagerChannelManager().getSessionManager();
		Session session = sessionManager.getSessionForCryptoSessionID(request.getCryptoSessionID());

		// TODO typed exceptions/typed responses?!
		if (session == null)
			throw new IllegalStateException("There is no session for cryptoSessionID=" + request.getCryptoSessionID() + "!");

		if (session.isLocked())
			throw new IllegalStateException("The session for cryptoSessionID=" + request.getCryptoSessionID() + " is currently locked!");

		if (session.getExpiry().before(new Date()))
			throw new IllegalStateException("The session for cryptoSessionID=" + request.getCryptoSessionID() + " is already expired!");

		byte[] key = sessionManager.getKeyStore().getKey(session.getUserName(), session.getPassword(), request.getKeyID());
		byte[] keyEncodedEncrypted = KeyEncryptionUtil.encryptKey(key, request.getKeyEncryptionTransformation(), request.getKeyEncryptionPublicKey());
		return new GetKeyResponse(request, request.getKeyID(), keyEncodedEncrypted);
	}

}
