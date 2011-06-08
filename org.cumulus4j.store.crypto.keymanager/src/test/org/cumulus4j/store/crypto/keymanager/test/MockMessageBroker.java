package org.cumulus4j.store.crypto.keymanager.test;

import java.security.SecureRandom;
import java.util.Date;
import java.util.concurrent.TimeoutException;

import org.cumulus4j.keymanager.back.shared.GetActiveEncryptionKeyRequest;
import org.cumulus4j.keymanager.back.shared.GetActiveEncryptionKeyResponse;
import org.cumulus4j.keymanager.back.shared.GetKeyRequest;
import org.cumulus4j.keymanager.back.shared.GetKeyResponse;
import org.cumulus4j.keymanager.back.shared.KeyEncryptionUtil;
import org.cumulus4j.keymanager.back.shared.Request;
import org.cumulus4j.keymanager.back.shared.Response;
import org.cumulus4j.store.crypto.keymanager.messagebroker.AbstractMessageBroker;
import org.cumulus4j.store.crypto.keymanager.messagebroker.MessageBrokerRegistry;
import org.cumulus4j.store.crypto.keymanager.rest.ErrorResponseException;

/**
 * @author Marco หงุ่ยตระกูล-Schulze - marco at nightlabs dot de
 */
public class MockMessageBroker extends AbstractMessageBroker
{
	public static void setMockSharedInstance() {
		MessageBrokerRegistry.sharedInstance().setActiveMessageBroker(new MockMessageBroker());
	}

	private static byte[] key = new byte[128 / 8]; // testing with 128 bits is sufficient
	static {
		new SecureRandom().nextBytes(key);
	}

	@Override
	protected Response _query(Class<? extends Response> responseClass, Request request)
	throws TimeoutException, ErrorResponseException
	{
		try {
			if (request instanceof GetActiveEncryptionKeyRequest) {
				GetActiveEncryptionKeyRequest r = (GetActiveEncryptionKeyRequest) request;
				byte[] keyEncodedEncrypted = KeyEncryptionUtil.encryptKey(key, r.getKeyEncryptionTransformation(), r.getKeyEncryptionPublicKey());
				return responseClass.cast(new GetActiveEncryptionKeyResponse(request, 123, keyEncodedEncrypted, new Date(System.currentTimeMillis() + 3600L * 1000L)));
			}
			if (request instanceof GetKeyRequest) {
				GetKeyRequest r = (GetKeyRequest) request;
				byte[] keyEncodedEncrypted = KeyEncryptionUtil.encryptKey(key, r.getKeyEncryptionTransformation(), r.getKeyEncryptionPublicKey());
				return responseClass.cast(new GetKeyResponse(request, r.getKeyID(), keyEncodedEncrypted));
			}
		} catch (RuntimeException x) {
			throw x;
		} catch (Exception x) {
			throw new RuntimeException(x);
		}
		throw new UnsupportedOperationException("NYI");
	}

	@Override
	protected Request _pollRequest(String cryptoSessionIDPrefix) {
		throw new UnsupportedOperationException("Mock does not implement this!");
	}

	@Override
	protected void _pushResponse(Response response) {
		throw new UnsupportedOperationException("Mock does not implement this!");
	}
}
