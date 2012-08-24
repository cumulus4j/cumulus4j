package org.cumulus4j.store.localkeystoremessagebroker;

import java.io.IOException;
import java.security.GeneralSecurityException;
import java.util.concurrent.TimeoutException;

import org.bouncycastle.crypto.CryptoException;
import org.cumulus4j.keymanager.back.shared.GetActiveEncryptionKeyRequest;
import org.cumulus4j.keymanager.back.shared.GetActiveEncryptionKeyResponse;
import org.cumulus4j.keymanager.back.shared.GetKeyRequest;
import org.cumulus4j.keymanager.back.shared.GetKeyResponse;
import org.cumulus4j.keymanager.back.shared.KeyEncryptionUtil;
import org.cumulus4j.keymanager.back.shared.Request;
import org.cumulus4j.keymanager.back.shared.Response;
import org.cumulus4j.keystore.DateDependentKeyStrategy;
import org.cumulus4j.keystore.KeyStore;
import org.cumulus4j.store.crypto.keymanager.messagebroker.AbstractMessageBroker;
import org.cumulus4j.store.crypto.keymanager.rest.ErrorResponseException;

public class LocalKeyStoreMessageBroker extends AbstractMessageBroker
{
	private KeyStore keyStore;
	private String userName;
	private char[] password;

	private DateDependentKeyStrategy.ActiveKey currentActiveKey;

	public synchronized KeyStore getKeyStore() {
		return keyStore;
	}
	public synchronized void setKeyStore(KeyStore keyStore) {
		this.keyStore = keyStore;
		currentActiveKey = null;
	}
	public synchronized String getUserName() {
		return userName;
	}
	public synchronized void setUserName(String authUserName) {
		this.userName = authUserName;
		currentActiveKey = null;
	}
	public synchronized char[] getPassword() {
		return password;
	}
	public synchronized void setPassword(char[] authPassword) {
		this.password = authPassword;
		currentActiveKey = null;
	}

	protected GetActiveEncryptionKeyResponse handle(GetActiveEncryptionKeyRequest request) throws GeneralSecurityException, IOException, CryptoException {
		KeyStore keyStore = getKeyStore();

		if (currentActiveKey == null || currentActiveKey.getActiveToExcl().compareTo(request.getTimestamp()) <= 0) {
			DateDependentKeyStrategy keyStrategy = new DateDependentKeyStrategy(keyStore);
			DateDependentKeyStrategy.ActiveKey newActiveKey = keyStrategy.getActiveKey(
					getUserName(), getPassword(), request.getTimestamp()
			);
			if (newActiveKey == null)
				throw new IllegalStateException("keyStrategy.getActiveKey(...) returned null!");

			currentActiveKey = newActiveKey;
		}

		byte[] key = keyStore.getKey(getUserName(), getPassword(), currentActiveKey.getKeyID());
		byte[] keyEncodedEncrypted = KeyEncryptionUtil.encryptKey(key, request.getKeyEncryptionTransformation(), request.getKeyEncryptionPublicKey());
		return new GetActiveEncryptionKeyResponse(
				request,
				currentActiveKey.getKeyID(), keyEncodedEncrypted, currentActiveKey.getActiveToExcl()
		);
	}

	protected GetKeyResponse handle(GetKeyRequest request) throws GeneralSecurityException, IOException, CryptoException {
		byte[] key = getKeyStore().getKey(getUserName(), getPassword(), request.getKeyID());
		byte[] keyEncodedEncrypted = KeyEncryptionUtil.encryptKey(key, request.getKeyEncryptionTransformation(), request.getKeyEncryptionPublicKey());
		return new GetKeyResponse(request, request.getKeyID(), keyEncodedEncrypted);
	}

	@Override
	protected synchronized Response _query(Class<? extends Response> responseClass, Request request)
	throws TimeoutException, ErrorResponseException
	{
		try {
			if (request instanceof GetActiveEncryptionKeyRequest) {
				return handle((GetActiveEncryptionKeyRequest) request);
			}
			if (request instanceof GetKeyRequest) {
				return handle((GetKeyRequest) request);
			}
		} catch (RuntimeException x) {
			throw x;
		} catch (Exception x) {
			throw new RuntimeException(x);
		}
		throw new UnsupportedOperationException("NYI");
	}

	@Override
	protected Request _pollRequest(String cryptoSessionIDPrefix)
	{
		throw new UnsupportedOperationException("LocalKeyStoreMessageBroker does not implement this!");
	}

	@Override
	protected void _pushResponse(Response response)
	{
		throw new UnsupportedOperationException("LocalKeyStoreMessageBroker does not implement this!");
	}

}
