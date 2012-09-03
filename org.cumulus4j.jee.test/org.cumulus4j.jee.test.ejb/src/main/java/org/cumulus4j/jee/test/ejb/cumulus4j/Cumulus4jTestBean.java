package org.cumulus4j.jee.test.ejb.cumulus4j;

import java.util.Properties;
import java.util.UUID;

import javax.ejb.Stateless;

import org.cumulus4j.jee.test.ejb.datanucleus.DataNucleusTestBean;
import org.cumulus4j.store.crypto.CryptoSession;

@Stateless
public class Cumulus4jTestBean extends DataNucleusTestBean implements
		Cumulus4jTestRemote {

	private String cryptoManagerID;
	private String cryptoSessionID;

	@Override
	protected Properties getProperties() {
		Properties props = super.getProperties();
		props.put("datanucleus.storeManagerType", "cumulus4j");
		props.put(CryptoSession.PROPERTY_CRYPTO_SESSION_ID, "dummy" + '_'
				+ UUID.randomUUID() + '*' + UUID.randomUUID());

		return props;
	}

	@Override
	public void init(String... args) throws Exception {
		super.init();

		cryptoManagerID = "storeManager";
		cryptoSessionID = args[0];
	}
}