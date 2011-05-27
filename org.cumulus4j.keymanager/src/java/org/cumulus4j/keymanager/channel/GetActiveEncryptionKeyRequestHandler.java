package org.cumulus4j.keymanager.channel;

import java.io.IOException;
import java.security.GeneralSecurityException;
import java.security.Key;
import java.util.Date;

import org.cumulus4j.keymanager.Session;
import org.cumulus4j.keymanager.SessionManager;
import org.cumulus4j.keymanager.back.shared.GetActiveEncryptionKeyRequest;
import org.cumulus4j.keymanager.back.shared.GetKeyResponse;
import org.cumulus4j.keymanager.back.shared.KeyEncryptionUtil;
import org.cumulus4j.keymanager.back.shared.Response;
import org.cumulus4j.keystore.AuthenticationException;
import org.cumulus4j.keystore.GeneratedKey;
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
	private static Date lastKeyChangeTimestamp = null;
	private static long keyID;

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

		if (lastKeyChangeTimestamp == null || lastKeyChangeTimestamp.before(new Date(System.currentTimeMillis() - 6L * 3600L * 1000L))) {
			// TODO Do NOT generate a key now, but instead keep a huge pool of unused keys. This makes it much easier to exchange keys between
			// multiple clients (e.g. on a USB thumb drive). Maybe make this configurable for something sort-of-perfect-forward-secrecy.
			// We might use a mixture, too, i.e. keep only enough fresh keys in the key store that we don't need to synchronize
			// key stores more often than once a week or so.
			// Marco :-)
			GeneratedKey generatedKey = sessionManager.getKeyStore().generateKey(session.getUserName(), session.getPassword());
			keyID = generatedKey.getKeyID();
			lastKeyChangeTimestamp = new Date();
		}

		Key key = sessionManager.getKeyStore().getKey(session.getUserName(), session.getPassword(), keyID);
		byte[] keyEncodedEncrypted = KeyEncryptionUtil.encryptKey(key, request.getKeyEncryptionAlgorithm(), request.getKeyEncryptionPublicKey());
		return new GetKeyResponse(request, keyID, key.getAlgorithm(), keyEncodedEncrypted);
	}

}
