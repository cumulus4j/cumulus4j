package org.cumulus4j.keymanager.channel;

import java.io.IOException;
import java.security.GeneralSecurityException;
import java.security.Key;
import java.util.Date;

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
	private static DateDependentKeyStrategy.ActiveKey activeKey;

	@Override
	public Response handle(GetActiveEncryptionKeyRequest request)
	throws AuthenticationException, KeyNotFoundException, IOException, GeneralSecurityException
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

		DateDependentKeyStrategy.ActiveKey currentActiveKey = activeKey;

		if (currentActiveKey == null || currentActiveKey.getActiveToExcl().compareTo(new Date()) <= 0) {
			DateDependentKeyStrategy keyStrategy = new DateDependentKeyStrategy(sessionManager.getKeyStore());
			DateDependentKeyStrategy.ActiveKey newActiveKey = keyStrategy.getActiveKey(session.getUserName(), session.getPassword(), null);
			activeKey = newActiveKey;
			currentActiveKey = newActiveKey;
		}

		Key key = sessionManager.getKeyStore().getKey(session.getUserName(), session.getPassword(), currentActiveKey.getKeyID());
		byte[] keyEncodedEncrypted = KeyEncryptionUtil.encryptKey(key, request.getKeyEncryptionAlgorithm(), request.getKeyEncryptionPublicKey());
		return new GetActiveEncryptionKeyResponse(
				request,
				currentActiveKey.getKeyID(), key.getAlgorithm(), keyEncodedEncrypted, currentActiveKey.getActiveToExcl()
		);
	}

}
