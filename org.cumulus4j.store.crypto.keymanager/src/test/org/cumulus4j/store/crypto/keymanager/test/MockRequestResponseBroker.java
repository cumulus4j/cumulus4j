package org.cumulus4j.store.crypto.keymanager.test;

import java.security.SecureRandom;
import java.util.concurrent.TimeoutException;

import javax.crypto.spec.SecretKeySpec;

import org.cumulus4j.keymanager.back.shared.GetActiveEncryptionKeyRequest;
import org.cumulus4j.keymanager.back.shared.GetKeyRequest;
import org.cumulus4j.keymanager.back.shared.GetKeyResponse;
import org.cumulus4j.keymanager.back.shared.Request;
import org.cumulus4j.keymanager.back.shared.Response;
import org.cumulus4j.store.crypto.keymanager.rest.ErrorResponseException;
import org.cumulus4j.store.crypto.keymanager.rest.MessageBroker;

public class MockRequestResponseBroker extends MessageBroker
{
	public static void setMockSharedInstance() {
		setSharedInstance(new MockRequestResponseBroker());
	}

	private static byte[] keyData = new byte[128 / 8]; // testing with 128 bits is sufficient
	static {
		new SecureRandom().nextBytes(keyData);
	}

	@Override
	public <R extends Response> R query(Class<R> responseClass, Request request)
	throws TimeoutException, ErrorResponseException
	{
		if (request instanceof GetActiveEncryptionKeyRequest) {
			SecretKeySpec key = new SecretKeySpec(keyData, "AES");
			return responseClass.cast(new GetKeyResponse(request, 123, key));
		}
		if (request instanceof GetKeyRequest) {
			GetKeyRequest r = (GetKeyRequest) request;
			SecretKeySpec key = new SecretKeySpec(keyData, "AES");
			return responseClass.cast(new GetKeyResponse(request, r.getKeyID(), key));
		}
		throw new UnsupportedOperationException("NYI");
	}

	@Override
	protected Request pollRequestForProcessing(String cryptoSessionIDPrefix) {
		throw new UnsupportedOperationException("Mock does not implement this!");
	}

	@Override
	protected void pushResponse(Response response) {
		throw new UnsupportedOperationException("Mock does not implement this!");
	}
}
