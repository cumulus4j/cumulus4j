package org.cumulus4j.jee.test.ejb.cumulus4j;

import java.util.Properties;

import javax.ejb.Stateless;

import org.cumulus4j.jee.test.ejb.datanucleus.DataNucleusTestBean;
import org.cumulus4j.store.crypto.CryptoManager;

@Stateless
public class Cumulus4jTestBean
extends DataNucleusTestBean
implements Cumulus4jTestRemote
{

	@Override
	protected Properties getProperties(){
		Properties props = super.getProperties();
//		props.put("datanucleus.storeManagerType", "cumulus4j");
		props.put(CryptoManager.PROPERTY_CRYPTO_MANAGER_ID, "dummy");

		return props;
	}

//	@Override
//	public void init(String cryptoManagerID, String cryptoSessionID){
//
//
//	}

//	@Resource(name="jdbc/__defaultC4j")
//	private DataSource defaultDataSourceC4j;
//
//	@Override
//	public void test() {
//		try {
//			System.out.println(String.format("PlainDataSourceTestBean.test: defaultDataSourceC4j=%s", defaultDataSourceC4j));
//			Connection connection = defaultDataSourceC4j.getConnection();
//			System.out.println(String.format("PlainDataSourceTestBean.test: connection=%s", connection));
////			executeSomeTestSQL(connection, "c4j-");
//		} catch (Exception x) {
//			throw new RuntimeException(x);
//		}
//	}
}
