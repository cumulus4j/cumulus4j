//package org.cumulus4j.jee.test.glassfish;
//
//import java.security.SecureRandom;
//
//import javax.naming.InitialContext;
//
//import org.cumulus4j.jee.test.ejb.RollbackTestRemote;
//import org.cumulus4j.jee.test.ejb.cumulus4j.Cumulus4jTestRemote;
//import org.slf4j.Logger;
//import org.slf4j.LoggerFactory;
//
//public class DefaultDataSourceC4jEjbInvocationIT extends AbstractGlassfishIT {
//
//	private static final String URL_APP_SERVER = "http://localhost:8080";
//	private static final String URL_INTEGRATIONTEST_CONTEXT = URL_APP_SERVER + "/autodeploy/org.cumulus4j.jee.test.ear-1.1.0-SNAPSHOT";
//	private static final String URL_KEY_MANAGER_BACK_WEBAPP = URL_INTEGRATIONTEST_CONTEXT + "/org.cumulus4j.keymanager.back.webapp";
//
//	private static final Logger logger = LoggerFactory
//			.getLogger(DefaultDataSourceC4jEjbInvocationIT.class);
//
//	private static final String KEY_STORE_USER = "test";
//	private static final char[] KEY_STORE_PASSWORD = "abcdefg-very+secret"
//			.toCharArray();
//
//	private static SecureRandom random = new SecureRandom();
//
//	@Override
//	protected RollbackTestRemote getRemote() throws Exception {
//
//		InitialContext ic = createInitialContext();
//
//		return (Cumulus4jTestRemote) ic.lookup(Cumulus4jTestRemote.class
//				.getName());
//
//	}
//
//	@Override
//	protected void init(RollbackTestRemote remote) throws Exception{
//
//		remote.init();
//
////		File keyStoreDir = new File(IOUtil.getTempDir(),
////				"cumulus4j-benchmark-key-stores");
////
////		logger.debug("Key store file directory: "
////				+ keyStoreDir.getAbsolutePath());
////
////		KeyManagerAPIConfiguration configuration = new KeyManagerAPIConfiguration();
////		configuration.setAuthUserName(KEY_STORE_USER);
////		configuration.setAuthPassword(KEY_STORE_PASSWORD);
////		configuration.setKeyStoreID("test-"
////				+ Long.toString(System.currentTimeMillis(), 36) + '-'
////				+ Long.toString(random.nextLong(), 36));
////		configuration.setKeyManagerBaseURL(keyStoreDir.toURI().toString());
////
////		try {
////			KeyManagerAPI keyManagerAPI = new DefaultKeyManagerAPI();
////			keyManagerAPI.setConfiguration(configuration);
////
////			DateDependentKeyStrategyInitParam param = new DateDependentKeyStrategyInitParam();
////			param.setKeyActivityPeriodMSec(3600L * 1000L);
////			param.setKeyStorePeriodMSec(24L * 3600L * 1000L);
////			keyManagerAPI.initDateDependentKeyStrategy(param);
////
////			org.cumulus4j.keymanager.api.CryptoSession cryptoSession = keyManagerAPI
////					.getCryptoSession(URL_KEY_MANAGER_BACK_WEBAPP);
////
////			// RefreshCryptoSessionThread refreshCryptoSessionThread = new
////			// RefreshCryptoSessionThread(cryptoSession);
////			// refreshCryptoSessionThread.start();
////
////			String cryptoSessionID = cryptoSession.acquire();
////			try {
//////				 invokeTestWithinServer(cryptoSessionID);
////				remote.init(cryptoSessionID);
////			} finally {
////				cryptoSession.release();
////			}
////
////		} finally {
////			File keyStoreFile = new File(keyStoreDir,
////					configuration.getKeyStoreID() + ".keystore");
////			if (!keyStoreFile.exists()) {
////				logger.warn("*** The key-store-file does not exist: "
////						+ keyStoreFile.getAbsolutePath());
////			} else {
////				keyStoreFile.delete();
////				if (keyStoreFile.exists())
////					logger.warn("The key store file could not be deleted: "
////							+ keyStoreFile.getAbsolutePath());
////				else
////					logger.info("The key store file has been deleted: "
////							+ keyStoreFile.getAbsolutePath());
////			}
////		}
//	}
//}
