package org.cumulus4j.store.crypto.keymanager.test;

import java.security.GeneralSecurityException;
import java.security.SecureRandom;
import java.util.concurrent.TimeoutException;

import javax.crypto.spec.SecretKeySpec;

import org.cumulus4j.keymanager.back.shared.GetActiveEncryptionKeyRequest;
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

	private static byte[] keyData = new byte[128 / 8]; // testing with 128 bits is sufficient
	static {
		new SecureRandom().nextBytes(keyData);
	}

	@Override
	public <R extends Response> R query(Class<R> responseClass, Request request)
	throws TimeoutException, ErrorResponseException
	{
		try {
			if (request instanceof GetActiveEncryptionKeyRequest) {
				GetActiveEncryptionKeyRequest r = (GetActiveEncryptionKeyRequest) request;
				SecretKeySpec key = new SecretKeySpec(keyData, "AES");
				byte[] keyEncodedEncrypted = KeyEncryptionUtil.encryptKey(key, r.getKeyEncryptionAlgorithm(), r.getKeyEncryptionPublicKey());
				return responseClass.cast(new GetKeyResponse(request, 123, key.getAlgorithm(), keyEncodedEncrypted));
			}
			if (request instanceof GetKeyRequest) {
				GetKeyRequest r = (GetKeyRequest) request;
				SecretKeySpec key = new SecretKeySpec(keyData, "AES");
				byte[] keyEncodedEncrypted = KeyEncryptionUtil.encryptKey(key, r.getKeyEncryptionAlgorithm(), r.getKeyEncryptionPublicKey());
				return responseClass.cast(new GetKeyResponse(request, r.getKeyID(), key.getAlgorithm(), keyEncodedEncrypted));
			}
		} catch (GeneralSecurityException x) {
			throw new RuntimeException(x);
		}
		throw new UnsupportedOperationException("NYI");
	}

	@Override
	public Request pollRequestForProcessing(String cryptoSessionIDPrefix) {
		throw new UnsupportedOperationException("Mock does not implement this!");
	}

	@Override
	public void pushResponse(Response response) {
		throw new UnsupportedOperationException("Mock does not implement this!");
	}
}
