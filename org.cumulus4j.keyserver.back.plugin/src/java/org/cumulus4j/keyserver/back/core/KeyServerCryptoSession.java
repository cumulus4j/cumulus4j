package org.cumulus4j.keyserver.back.core;

import org.cumulus4j.api.crypto.AbstractCryptoSession;
import org.cumulus4j.api.crypto.Ciphertext;
import org.cumulus4j.api.crypto.Plaintext;

/**
 * @author Marco หงุ่ยตระกูล-Schulze - marco at nightlabs dot de
 */
public class KeyServerCryptoSession
extends AbstractCryptoSession
{

	@Override
	public Ciphertext encrypt(Plaintext plaintext) {
//		RequestResponseBroker.sharedInstance().query(new GetKeyRequest(keyServerID, cryptoSessionID, keyID));
		return null;
	}

	@Override
	public Plaintext decrypt(Ciphertext ciphertext) {
//		RequestResponseBroker.sharedInstance().query(new GetKeyRequest(keyServerID, getCryptoSessionID(), ciphertext.getKeyID()));
		// TODO Auto-generated method stub
		return null;
	}

}
