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
