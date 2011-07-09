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
import java.util.Collections;
import java.util.Date;
import java.util.Map;
import java.util.WeakHashMap;

import org.bouncycastle.crypto.CryptoException;
import org.cumulus4j.keymanager.Session;
import org.cumulus4j.keymanager.SessionManager;
import org.cumulus4j.keymanager.back.shared.GetActiveEncryptionKeyRequest;
import org.cumulus4j.keymanager.back.shared.GetActiveEncryptionKeyResponse;
import org.cumulus4j.keymanager.back.shared.GetKeyResponse;
import org.cumulus4j.keymanager.back.shared.KeyEncryptionUtil;
import org.cumulus4j.keymanager.back.shared.Response;
import org.cumulus4j.keystore.AuthenticationException;
import org.cumulus4j.keystore.DateDependentKeyStrategy;
import org.cumulus4j.keystore.KeyNotFoundException;
import org.cumulus4j.keystore.KeyStore;

/**
 * <p>
 * Handler for {@link GetActiveEncryptionKeyRequest}.
 * </p>
 * <p>
 * If the {@link Session} is found for the given
 * {@link org.cumulus4j.keymanager.back.shared.Request#getCryptoSessionID() cryptoSessionID} and
 * it is not {@link Session#isLocked() locked}, this handler determines the currently active
 * encryption key and sends it in a {@link GetKeyResponse} to the server.
 * </p>
 *
 * @author Marco หงุ่ยตระกูล-Schulze - marco at nightlabs dot de
 */
public class GetActiveEncryptionKeyRequestHandler extends AbstractRequestHandler<GetActiveEncryptionKeyRequest>
{
	private static Map<KeyStore, DateDependentKeyStrategy.ActiveKey> keyStore2activeKey = Collections.synchronizedMap(
			new WeakHashMap<KeyStore, DateDependentKeyStrategy.ActiveKey>()
	);

	@Override
	public Response handle(GetActiveEncryptionKeyRequest request)
	throws AuthenticationException, KeyNotFoundException, IOException, GeneralSecurityException, CryptoException
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

		if (request.getTimestamp() == null)
			throw new IllegalArgumentException("request.getTimestamp() == null");

		KeyStore keyStore = sessionManager.getKeyStore();
		DateDependentKeyStrategy.ActiveKey currentActiveKey = keyStore2activeKey.get(keyStore);

		if (currentActiveKey == null || currentActiveKey.getActiveToExcl().compareTo(request.getTimestamp()) <= 0) {
			DateDependentKeyStrategy keyStrategy = new DateDependentKeyStrategy(keyStore);
			DateDependentKeyStrategy.ActiveKey newActiveKey = keyStrategy.getActiveKey(
					session.getUserName(), session.getPassword(), request.getTimestamp()
			);
			if (newActiveKey == null)
				throw new IllegalStateException("keyStrategy.getActiveKey(...) returned null!");

			keyStore2activeKey.put(keyStore, newActiveKey);
			currentActiveKey = newActiveKey;
		}

		byte[] key = keyStore.getKey(session.getUserName(), session.getPassword(), currentActiveKey.getKeyID());
		byte[] keyEncodedEncrypted = KeyEncryptionUtil.encryptKey(key, request.getKeyEncryptionTransformation(), request.getKeyEncryptionPublicKey());
		return new GetActiveEncryptionKeyResponse(
				request,
				currentActiveKey.getKeyID(), keyEncodedEncrypted, currentActiveKey.getActiveToExcl()
		);
	}

}
