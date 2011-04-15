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

import javax.crypto.KeyGenerator;
import javax.crypto.spec.SecretKeySpec;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class KeyStore
{
	private static final Logger logger = LoggerFactory.getLogger(KeyStore.class);

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

	public synchronized MasterKey login(String userName, char[] password)
	throws LoginException
	{
		MasterKey result = null;

		String userNameAlias = getAliasForUserName(userName);
		try {
			if (!jks.containsAlias(userNameAlias)) {
				logger.warn("login: Unknown userName: " + userName);
			}
			else {
				try {
					result = new MasterKey(jks.getKey(userNameAlias, password));
				} catch (UnrecoverableKeyException e) {
					logger.warn("login: Wrong password for userName=\"{}\"!", userName);
				} catch (NoSuchAlgorithmException e) {
					throw new RuntimeException(e); // This keystore should only be managed by us => rethrow as RuntimeException
				}
			}
		} catch (KeyStoreException e) {
			// should only be thrown, if the keyStore is not initialized and this is done in our constructor!
			throw new RuntimeException(e);
		}

		if (result == null)
			throw new LoginException("Unknown user \"" + userName + "\" or wrong password!");

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
	public synchronized MasterKey init()
	throws NotEmptyException
	{
		if (!isEmpty())
			throw new NotEmptyException("This KeyStore has already been initialised!");

		Key key = getKeyGenerator(getKeySize()).generateKey();
		MasterKey result = new MasterKey(key);
		setVersion();
		return result;
	}

	protected void checkMasterKey(MasterKey masterKey)
	{

	}

	public synchronized void createUser(String userName, char[] password, MasterKey masterKey)
	throws UserAlreadyExistsException, IOException
	{
		checkMasterKey(masterKey);

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

	public synchronized boolean deleteUser(String userName, MasterKey masterKey)
	{
		throw new UnsupportedOperationException("NYI");
	}

	public synchronized void changeMyPassword(String userName, char[] oldPassword, char[] newPassword)
	{

	}

	public synchronized void changeUserPassword(String userName, char[] newPassword, MasterKey masterKey)
	throws UserDoesNotExistException, IOException
	{
		checkMasterKey(masterKey);

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

	public synchronized Key getKey(long keyID, MasterKey masterKey)
	{
		throw new UnsupportedOperationException("NYI");
	}

	public synchronized void setKey(long keyID, Key key, MasterKey masterKey)
	{

	}
}
