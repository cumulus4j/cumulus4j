package org.cumulus4j.keymanager.keyserverchannel;

import java.security.Key;
import java.util.Date;

import org.cumulus4j.keymanager.Session;
import org.cumulus4j.keymanager.SessionManager;
import org.cumulus4j.keymanager.back.shared.GetKeyRequest;
import org.cumulus4j.keymanager.back.shared.GetKeyResponse;
import org.cumulus4j.keymanager.back.shared.Response;
import org.cumulus4j.keystore.AuthenticationException;
import org.cumulus4j.keystore.KeyNotFoundException;

public class GetKeyRequestHandler extends AbstractRequestHandler<GetKeyRequest>
{

	@Override
	public Response handle(GetKeyRequest request) throws AuthenticationException, KeyNotFoundException
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

		Key key = sessionManager.getKeyStore().getKey(session.getUserName(), session.getPassword(), request.getKeyID());
		return new GetKeyResponse(request, request.getKeyID(), key);
	}

}
