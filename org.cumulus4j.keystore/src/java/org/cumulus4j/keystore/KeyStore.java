package org.cumulus4j.keystore;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.math.BigInteger;
import java.security.GeneralSecurityException;
import java.security.Key;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.UnrecoverableKeyException;
import java.security.cert.CertificateException;
import java.util.HashMap;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;

import javax.crypto.KeyGenerator;
import javax.crypto.spec.SecretKeySpec;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class KeyStore
{
	private static final Logger logger = LoggerFactory.getLogger(KeyStore.class);

	private static Timer expireCacheEntryTimer = new Timer();

	private TimerTask expireCacheEntryTimerTask = new TimerTask() {
		@Override
		public void run() {
			// TODO expire cache entries, i.e. call
		}
	};

//	private SecureRandom random;
//
//	protected SecureRandom getRandom()
//	{
//		if (random == null)
//			random = new SecureRandom();
//
//		return random;
//	}

	private int keySize = 0;
	protected int getKeySize()
	{
		int ks = keySize;

		if (ks == 0) {
			String keySizePropName = KeyStore.class.getName() + ".keySize";
			String keySizePropValue = System.getProperty(keySizePropName);
			if (keySizePropValue == null || keySizePropValue.trim().isEmpty())
				ks = 128;
			else {
				try {
					ks = Integer.parseInt(keySizePropValue.trim());
				} catch (NumberFormatException x) {
					NumberFormatException n = new NumberFormatException("Value of system property '" + keySizePropName + "' is not a valid integer!");
					n.initCause(x);
					throw n;
				}
				if (ks < 1)
					throw new IllegalStateException("Value of system property '" + keySizePropName + "' is " + keySize + " but must be >= 1!!!");
			}

			keySize = ks;
		}

		return ks;
	}

	private Map<Integer, KeyGenerator> keySize2keyGenerator = new HashMap<Integer, KeyGenerator>();

	protected synchronized KeyGenerator getKeyGenerator(int keySize)
	{
		KeyGenerator keyGenerator = keySize2keyGenerator.get(keySize);

		if (keyGenerator == null) {
			try {
				keyGenerator = KeyGenerator.getInstance("AES"); // TODO make this configurable
			} catch (NoSuchAlgorithmException e) {
				throw new RuntimeException(e);
			}
			keyGenerator.init(keySize);
			keySize2keyGenerator.put(keySize, keyGenerator);
		}

		return keyGenerator;
	}

	private File keyStoreFile;
	private java.security.KeyStore jks;

	public KeyStore(File keyStoreFile) throws IOException
	{
		if (keyStoreFile == null)
			throw new IllegalArgumentException("keyStoreFile == null");

		this.keyStoreFile = keyStoreFile;

		if (!keyStoreFile.getParentFile().isDirectory())
			throw new FileNotFoundException("Path does not exist or is not a directory: " + keyStoreFile.getParentFile().getAbsolutePath());

		try {
			jks = java.security.KeyStore.getInstance("JCEKS"); // http://khylo.blogspot.com/2009/12/keytool-keystore-cannot-store-non.html
		} catch (KeyStoreException e) {
			throw new RuntimeException("Something is wrong in the JRE: Could not get a java.security.KeyStore instance: " + e, e);
		}

		// In case the old file was already deleted, but the new not yet renamed, we check, if a new file
		// exists and the old file is missing - in this case, we load the new file.
		File newKeyStoreFile = getNewKeyStoreFile();
		if (!keyStoreFile.exists() && newKeyStoreFile.exists())
			keyStoreFile = newKeyStoreFile;

		FileInputStream in = keyStoreFile.length() == 0 ? null : new FileInputStream(keyStoreFile);
		try {
			jks.load(in, KEY_STORE_PASSWORD);
		} catch (GeneralSecurityException e) {
			throw new IOException("Could not load java.security.KeyStore from \"" + keyStoreFile.getAbsolutePath() + "\": " + e, e);
		}
		if (in != null)
			in.close();

		expireCacheEntryTimer.schedule(expireCacheEntryTimerTask, 60000, 60000); // TODO make this configurable
	}

	protected File getNewKeyStoreFile()
	{
		return new File(keyStoreFile.getParentFile(), keyStoreFile.getName() + ".new");
	}

	private String getAliasForUserName(String userName)
	{
		return "user:"+userName;
	}

	private static final String ALIAS_VERSION = "version";

	public synchronized boolean isEmpty()
	{
		try {
			return !jks.containsAlias(ALIAS_VERSION);
		} catch (KeyStoreException e) {
			// should only be thrown, if the keyStore is not initialized and this is done in our constructor!
			throw new RuntimeException(e);
		}
	}

	public synchronized int getVersion()
	{
		try {
			if (!jks.containsAlias(ALIAS_VERSION))
				return 0;

			Key key = jks.getKey(ALIAS_VERSION, null); // not protected
			BigInteger bi = new BigInteger(key.getEncoded());
			return bi.intValue();
		} catch (GeneralSecurityException e) {
			throw new RuntimeException(e);
		}
	}

	protected void setVersion()
	{
		try {
			Key key = new SecretKeySpec(new byte[] { 1 }, ALIAS_VERSION);
			jks.setKeyEntry(ALIAS_VERSION, key, new char[0], null);
		} catch (GeneralSecurityException e) {
			throw new RuntimeException(e);
		}
	}

	protected synchronized MasterKey getMasterKey(String principalUserName, char[] principalPassword)
	throws LoginException
	{
		MasterKey result = null;

		// TODO consult a cache to speed things up!

		String userNameAlias = getAliasForUserName(principalUserName);
		try {
			if (!jks.containsAlias(userNameAlias)) {
				logger.warn("login: Unknown userName: " + principalUserName);
			}
			else {
				try {
					result = new MasterKey(jks.getKey(userNameAlias, principalPassword));
				} catch (UnrecoverableKeyException e) {
					logger.warn("login: Wrong password for userName=\"{}\"!", principalUserName);
				} catch (NoSuchAlgorithmException e) {
					throw new RuntimeException(e); // This keystore should only be managed by us => rethrow as RuntimeException
				}
			}
		} catch (KeyStoreException e) {
			// should only be thrown, if the keyStore is not initialized and this is done in our constructor!
			throw new RuntimeException(e);
		}

		if (result == null)
			throw new LoginException("Unknown user \"" + principalUserName + "\" or wrong password!");

		return result;
	}

	protected static char[] toCharArray(byte[] byteArray)
	{
		int charArrayLength = byteArray.length / 2;
		if ((byteArray.length % 2) != 0)
			++charArrayLength;

		char[] charArray = new char[charArrayLength];
		for (int i = 0; i < charArray.length; i++) {
			int first = byteArray[i * 2] & 0xff;
			int second = (byteArray.length <= i * 2 + 1 ? 0 : byteArray[i * 2 + 1]) & 0xff;
			charArray[i] = (char)((first << 8) + second);
		}
		return charArray;
	}

	protected static byte[] toByteArray(char[] charArray)
	{
		byte[] byteArray = new byte[charArray.length * 2];
		for (int i = 0; i < charArray.length; i++) {
			int v = charArray[i];
			byteArray[i * 2] = (byte) (v >>> 8);
			byteArray[i * 2 + 1] = (byte) v;
		}
		return byteArray;
	}

//	public static void main(String[] args) throws Exception
//	{
//		KeyStore keyStore = new KeyStore(File.createTempFile("test-", ".keystore"));
//		for (int i = 0; i < 10; i++) {
//			SecretKey key = keyStore.getKeyGenerator(keyStore.getKeySize()).generateKey();
//			char[] charArray = KeyStore.toCharArray(key.getEncoded());
//			byte[] byteArray = KeyStore.toByteArray(charArray);
//			if (!Arrays.equals(byteArray, key.getEncoded()))
//				throw new IllegalStateException("round-trip-converted byte-array is not equal to original!");
//		}
//	}

	/**
	 * Initialises an empty key store. This generates a new {@link MasterKey} and holds the initialised
	 * state in memory. It does not yet write anything to the file, because this key store holds no
	 * user yet and could thus never used again.
	 *
	 * @return a new {@link MasterKey}; never <code>null</code>.
	 * @throws NotEmptyException if the key store was already initialised.
	 */
	protected synchronized MasterKey init()
	throws NotEmptyException
	{
		if (!isEmpty())
			throw new NotEmptyException("This KeyStore has already been initialised!");

		Key key = getKeyGenerator(getKeySize()).generateKey();
		MasterKey result = new MasterKey(key);
		setVersion();
		return result;
	}

	public synchronized void createUser(String principalUserName, char[] principalPassword, String userName, char[] password)
	throws LoginException, UserAlreadyExistsException, IOException
	{
		MasterKey masterKey = getMasterKey(principalUserName, principalPassword);

		String userNameAlias = getAliasForUserName(userName);
		try {
			if (jks.containsAlias(userNameAlias))
				throw new UserAlreadyExistsException("The user \"" + userName + "\" already exists!");

			jks.setKeyEntry(userNameAlias, masterKey.getKey(), password, null);
		} catch (KeyStoreException e) {
			throw new RuntimeException(e);
		}

		storeToFile();
	}

	private static char[] KEY_STORE_PASSWORD = new char[0];

	protected void storeToFile() throws IOException
	{
		File newKeyStoreFile = getNewKeyStoreFile();
		boolean deleteNewKeyStoreFile = true;
		try {
			FileOutputStream out = new FileOutputStream(newKeyStoreFile);
			try {
				jks.store(out, KEY_STORE_PASSWORD);
			} catch (KeyStoreException e) {
				throw new IOException("Storing JKS failed!", e);
			} catch (NoSuchAlgorithmException e) {
				throw new IOException("Storing JKS failed!", e);
			} catch (CertificateException e) {
				throw new IOException("Storing JKS failed!", e);
			}
			out.close();

			deleteNewKeyStoreFile = false;
			keyStoreFile.delete();
			newKeyStoreFile.renameTo(keyStoreFile);
		} finally {
			if (deleteNewKeyStoreFile) {
				try {
					newKeyStoreFile.delete();
				} catch (Exception x) {
					logger.warn("Deleting the newKeyStoreFile failed!", x);
				}
			}
		}
	}

	/**
	 * Delete the user specified by <code>userName</code>.
	 *
	 * @param principalUserName the name of the principal, i.e. the user authorizing this operation.
	 * @param principalPassword the password of the principal.
	 * @param userName the name of the user to be deleted.
	 */
	public synchronized void deleteUser(String principalUserName, char[] principalPassword, String userName)
	throws LoginException, UserDoesNotExistException, IOException
	{
		throw new UnsupportedOperationException("NYI");
	}

	public synchronized void changeMyPassword(String userName, char[] oldPassword, char[] newPassword)
	throws LoginException, IOException
	{
		try {
			changeUserPassword(userName, oldPassword, userName, newPassword);
		} catch (UserDoesNotExistException e) {
			throw new RuntimeException("How the hell can this happen? The LoginException should have occured in this case!", e);
		}
	}

	public synchronized void changeUserPassword(String principalUserName, char[] principalPassword, String userName, char[] newPassword)
	throws LoginException, UserDoesNotExistException, IOException
	{
		MasterKey masterKey = getMasterKey(principalUserName, principalPassword);

		String userNameAlias = getAliasForUserName(userName);
		try {
			if (!jks.containsAlias(userNameAlias))
				throw new UserDoesNotExistException("The user \"" + userName + "\" already exists!");

			jks.setKeyEntry(userNameAlias, masterKey.getKey(), newPassword, null);
		} catch (KeyStoreException e) {
			// should only be thrown, if the keyStore is not initialized and this is done in our constructor!
			throw new RuntimeException(e);
		}

		storeToFile();
	}

	public synchronized Key getKey(String principalUserName, char[] principalPassword, long keyID)
	throws LoginException
	{
		MasterKey masterKey = getMasterKey(principalUserName, principalPassword);

		throw new UnsupportedOperationException("NYI");
	}

	public synchronized void setKey(String principalUserName, char[] principalPassword, long keyID, Key key)
	throws LoginException, IOException
	{
		MasterKey masterKey = getMasterKey(principalUserName, principalPassword);

	}

	/**
	 * Clear all cached data for the specified user name. Every time, a user
	 * calls a method requiring <code>principalUserName</code> and <code>principalPassword</code>),
	 * either a authentication process happens implicitely, or a previously cached authentication
	 * result is used. In order to speed things up, authentication results are cached for a
	 * limited time. After this time elapses, the data is cleared by a timer. If a user wants (for security reasons)
	 * remove the cached data from the memory earlier, he can call this method from the outside.
	 *
	 * @param principalUserName the user for which to clear all the cached data.
	 */
	public synchronized void clearCache(String principalUserName)
	{

	}
}
