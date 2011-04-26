package org.cumulus4j.keystore;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.ref.WeakReference;
import java.security.GeneralSecurityException;
import java.security.Key;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.spec.KeySpec;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.Timer;
import java.util.TimerTask;

import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.PBEKeySpec;
import javax.crypto.spec.SecretKeySpec;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class KeyStore
{
	private static final Logger logger = LoggerFactory.getLogger(KeyStore.class);

	private SecureRandom secureRandom = new SecureRandom();

	private static Timer expireCacheEntryTimer = new Timer();

	private TimerTask expireCacheEntryTimerTask = new ExipreCacheEntryTimerTask(this);

	private static class ExipreCacheEntryTimerTask extends TimerTask
	{
		private static final Logger logger = LoggerFactory.getLogger(ExipreCacheEntryTimerTask.class);

		private WeakReference<KeyStore> keyStoreRef;

		public ExipreCacheEntryTimerTask(KeyStore keyStore) {
			this.keyStoreRef = new WeakReference<KeyStore>(keyStore);
		}

		@Override
		public void run() {
			KeyStore keyStore = keyStoreRef.get();
			if (keyStore == null) {
				logger.info("run: KeyStore has been garbage-collected. Removing this ExipreCacheEntryTimerTask.");
				this.cancel();
				return;
			}

			// TODO expire cache entries, i.e. call clearCache(...) for all old, expired entries
		}
	};

	private int keySize = 0;
	protected int getKeySize()
	{
		int ks = keySize;

		if (ks == 0) {
			String keySizePropName = KeyStore.class.getName() + ".keySize";
			String keySizePropValue = System.getProperty(keySizePropName);
			if (keySizePropValue == null || keySizePropValue.trim().isEmpty()) {
				ks = 128; // default value, if the property was not defined.
				logger.info("getKeySize: System property '{}' is not set. Using default key size ({} bit).", keySizePropName, ks);
			}
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

			logger.info("getKeySize: System property '{}' is set to {} bit. Using this key size.", keySizePropName, ks);
			keySize = ks;
		}

		return ks;
	}

	private long nextKeyID = 1;
	private Map<String, EncryptedKey> user2keyMap = new HashMap<String, EncryptedKey>();
	private Map<Long, EncryptedKey> keyID2keyMap = new HashMap<Long, EncryptedKey>();

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
//	private java.security.KeyStore jks;

	public KeyStore(File keyStoreFile) throws IOException
	{
		if (keyStoreFile == null)
			throw new IllegalArgumentException("keyStoreFile == null");

		this.keyStoreFile = keyStoreFile;

		if (!keyStoreFile.getParentFile().isDirectory())
			throw new FileNotFoundException("Path does not exist or is not a directory: " + keyStoreFile.getParentFile().getAbsolutePath());

//		try {
//			jks = java.security.KeyStore.getInstance("JCEKS"); // http://khylo.blogspot.com/2009/12/keytool-keystore-cannot-store-non.html
//		} catch (KeyStoreException e) {
//			throw new RuntimeException("Something is wrong in the JRE: Could not get a java.security.KeyStore instance: " + e, e);
//		}

		// In case the old file was already deleted, but the new not yet renamed, we check, if a new file
		// exists and the old file is missing - in this case, we load the new file.
		File newKeyStoreFile = getNewKeyStoreFile();
		if (!keyStoreFile.exists() && newKeyStoreFile.exists())
			keyStoreFile = newKeyStoreFile;

		FileInputStream in = keyStoreFile.length() == 0 ? null : new FileInputStream(keyStoreFile);
//		try {
//			jks.load(in, KEY_STORE_PASSWORD);
//		} catch (GeneralSecurityException e) {
//			throw new IOException("Could not load java.security.KeyStore from \"" + keyStoreFile.getAbsolutePath() + "\": " + e, e);
//		}
		if (in != null)
			load(in);

		if (in != null)
			in.close();

		expireCacheEntryTimer.schedule(expireCacheEntryTimerTask, 60000, 60000); // TODO make this configurable
	}

	private static final String FILE_HEADER = "Cumulus4jKeyStore";
	private static final long FILE_VERSION = 1;

	private void load(InputStream in)
	throws IOException
	{
		DataInputStream din = new DataInputStream(new BufferedInputStream(in));

		char[] fileHeaderCharArray = FILE_HEADER.toCharArray();
		char[] buf = new char[fileHeaderCharArray.length];
		for (int i = 0; i < buf.length; i++) {
			buf[i] = din.readChar();
		}

		if (!Arrays.equals(fileHeaderCharArray, buf))
			throw new IOException("Stream does not start with expected HEADER!");

		long fileVersion = din.readLong();
		if (FILE_VERSION != fileVersion)
			throw new IOException("Version not supported! Stream contains a keystore of version \"" + fileVersion + "\" while version \"" + FILE_VERSION + "\" (or lower) is expected!");

		nextKeyID = din.readLong();

		int user2keyMapSize = din.readInt();
		user2keyMap.clear();
		for (int i = 0; i < user2keyMapSize; ++i) {
			String user = din.readUTF();
			EncryptedKey key = readKey(din);
			user2keyMap.put(user, key);
		}

		int keyID2keyMapSize = din.readInt();
		keyID2keyMap.clear();
		for (int i = 0; i < keyID2keyMapSize; ++i) {
			long keyID = din.readLong();
			EncryptedKey key = readKey(din);
			keyID2keyMap.put(keyID, key);
		}
	}

	private static void readByteArrayCompletely(InputStream in, byte[] dest)
	throws IOException
	{
		int off = 0;
		while (off < dest.length) {
			int read = in.read(dest, off, dest.length - off);
			if (read < 0)
				throw new IOException("Unexpected early end of stream!");

			off += read;
		}
	}

	private void store(OutputStream out)
	throws IOException
	{
		DataOutputStream dout = new DataOutputStream(new BufferedOutputStream(out));
		dout.writeChars(FILE_HEADER);
		dout.writeLong(FILE_VERSION);
		dout.writeLong(nextKeyID);

		dout.writeInt(user2keyMap.size());
		for (Map.Entry<String, EncryptedKey> me : user2keyMap.entrySet()) {
			dout.writeUTF(me.getKey());
			writeKey(dout, me.getValue());
		}

		dout.writeInt(keyID2keyMap.size());
		for (Map.Entry<Long, EncryptedKey> me : keyID2keyMap.entrySet()) {
			dout.writeLong(me.getKey());
			writeKey(dout, me.getValue());
		}
	}

	private void writeKey(DataOutputStream dout, EncryptedKey key) throws IOException
	{
		byte[] keyBytes = key.getData();
		dout.writeInt(keyBytes.length);
		dout.write(keyBytes);

		byte[] saltBytes = key.getSalt();
		dout.writeInt(saltBytes.length);
		dout.write(saltBytes);

		dout.writeUTF(key.getAlgorithm());
	}

	private EncryptedKey readKey(DataInputStream din) throws IOException
	{
		int keySize = din.readInt();
		byte[] key = new byte[keySize];
		readByteArrayCompletely(din, key);

		int saltSize = din.readInt();
		byte[] salt = saltSize == 0 ? null : new byte[saltSize];
		if (salt != null)
			readByteArrayCompletely(din, salt);

		String algorithm = din.readUTF();

		return new EncryptedKey(key, salt, algorithm);
	}

	protected File getNewKeyStoreFile()
	{
		return new File(keyStoreFile.getParentFile(), keyStoreFile.getName() + ".new");
	}

	public synchronized boolean isEmpty()
	{
		return user2keyMap.isEmpty();
	}

//	public synchronized int getVersion()
//	{
//		try {
//			if (!jks.containsAlias(ALIAS_VERSION))
//				return 0;
//
//			Key key = jks.getKey(ALIAS_VERSION, null); // not protected
//			BigInteger bi = new BigInteger(key.getEncoded());
//			return bi.intValue();
//		} catch (GeneralSecurityException e) {
//			throw new RuntimeException(e);
//		}
//	}
//
//	protected synchronized void setVersion()
//	{
//		try {
//			Key key = new SecretKeySpec(new byte[] { 1 }, ALIAS_VERSION);
//			jks.setKeyEntry(ALIAS_VERSION, key, new char[0], null);
//		} catch (GeneralSecurityException e) {
//			throw new RuntimeException(e);
//		}
//	}

//	private static final BigInteger ONE = new BigInteger(new byte[] { 1 });

	protected synchronized long nextKeyID()
	{
		long result = nextKeyID++;
		return result;
//		long resultNextKeyID;
//
//		try {
//			BigInteger bi;
//
//			if (!jks.containsAlias(ALIAS_NEXT_KEY_ID))
//				bi = ONE; // First value
//			else {
//				Key key = jks.getKey(ALIAS_NEXT_KEY_ID, null); // not protected
//				bi = new BigInteger(key.getEncoded());
//			}
//			resultNextKeyID = bi.longValue();
//
//			Key key = new SecretKeySpec(bi.add(ONE).toByteArray(), ALIAS_NEXT_KEY_ID);
//			jks.setKeyEntry(ALIAS_NEXT_KEY_ID, key, new char[0], null);
//		} catch (GeneralSecurityException e) {
//			throw new RuntimeException(e);
//		}
//
//		return resultNextKeyID;
	}

	protected synchronized MasterKey getMasterKey(String authUserName, char[] authPassword)
	throws LoginException
	{
		MasterKey result = null;

		// TODO consult a cache to speed things up!

//		String userNameAlias = getAliasForUserName(authUserName);
//		try {
//			if (!jks.containsAlias(userNameAlias)) {
//				logger.warn("login: Unknown userName: " + authUserName);
//			}
//			else {
//				try {
//					result = new MasterKey(jks.getKey(userNameAlias, authPassword));
//				} catch (UnrecoverableKeyException e) {
//					logger.warn("login: Wrong password for userName=\"{}\"!", authUserName);
//				} catch (NoSuchAlgorithmException e) {
//					throw new RuntimeException(e); // This keystore should only be managed by us => rethrow as RuntimeException
//				}
//			}
//		} catch (KeyStoreException e) {
//			// should only be thrown, if the keyStore is not initialized and this is done in our constructor!
//			throw new RuntimeException(e);
//		}

		EncryptedKey key = user2keyMap.get(authUserName);
		if (key == null)
			logger.warn("login: Unknown userName: " + authUserName);
		else {
			try {
				Cipher cipher = getCipherForUserPassword(authPassword, key.getSalt(), Cipher.DECRYPT_MODE);
				byte[] decrypted = cipher.doFinal(key.getData());
				// TODO verify, if the 'decrypted' is really correct! Maybe use a hash?!
				result = new MasterKey(new SecretKeySpec(decrypted, key.getAlgorithm()));
			} catch (GeneralSecurityException x) {
				throw new RuntimeException(x);
			}
		}

		if (result == null)
			throw new LoginException("Unknown user \"" + authUserName + "\" or wrong password!");

		return result;
	}

	private Cipher getCipherForUserPassword(char[] password, byte[] salt, int opmode) throws GeneralSecurityException
	{
		SecretKeyFactory factory = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA1"); // TODO make configurable!
		KeySpec spec = new PBEKeySpec(password, salt, 1024, getKeySize());
		SecretKey tmp = factory.generateSecret(spec);
		SecretKey secret = new SecretKeySpec(tmp.getEncoded(), "AES"); // TODO is this correct or should it be "RAW" or sth. else? Maybe make configurable!?
		Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5Padding"); // TODO make configurable!
		byte[] iv = new byte[16];
		Arrays.fill(iv, (byte)0); // No need of an IV, because we use a salt.
		cipher.init(opmode, secret, new IvParameterSpec(iv));
		return cipher;
	}

	private Cipher getCipherForMasterKey(MasterKey masterKey, int opmode) throws GeneralSecurityException
	{
		Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5Padding"); // TODO make configurable!
		cipher.init(opmode, masterKey.getKey());
		return cipher;
	}

//	protected static char[] toCharArray(byte[] byteArray)
//	{
//		int charArrayLength = byteArray.length / 2;
//		if ((byteArray.length % 2) != 0)
//			++charArrayLength;
//
//		char[] charArray = new char[charArrayLength];
//		for (int i = 0; i < charArray.length; i++) {
//			int first = byteArray[i * 2] & 0xff;
//			int second = (byteArray.length <= i * 2 + 1 ? 0 : byteArray[i * 2 + 1]) & 0xff;
//			charArray[i] = (char)((first << 8) + second);
//		}
//		return charArray;
//	}
//
//	protected static byte[] toByteArray(char[] charArray)
//	{
//		byte[] byteArray = new byte[charArray.length * 2];
//		for (int i = 0; i < charArray.length; i++) {
//			int v = charArray[i];
//			byteArray[i * 2] = (byte) (v >>> 8);
//			byteArray[i * 2 + 1] = (byte) v;
//		}
//		return byteArray;
//	}

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

	public synchronized GeneratedKey generateKey(String authUserName, char[] authPassword)
	throws LoginException, IOException
	{
		long keyID = nextKeyID();
		SecretKey key = getKeyGenerator(getKeySize()).generateKey();
		GeneratedKey generatedKey = new GeneratedKey(keyID, key);
		setKey(authUserName, authPassword, keyID, key);
//		storeToFile(); // setKey(...) already calls storeToFile(...)
		return generatedKey;
	}

	/**
	 * Initialises an empty key store. This generates a new {@link MasterKey} and holds the initialised
	 * state in memory. It does not yet write anything to the file, because this key store holds no
	 * user yet and could thus never be used again.
	 *
	 * @return a new {@link MasterKey}; never <code>null</code>.
	 */
	protected synchronized MasterKey init()
	{
		if (!isEmpty())
			throw new IllegalStateException("This KeyStore has already been initialised!");

		Key key = getKeyGenerator(getKeySize()).generateKey();
		MasterKey result = new MasterKey(key);
//		setVersion();
		return result;
	}

	public synchronized void createUser(String authUserName, char[] authPassword, String userName, char[] password)
	throws LoginException, UserAlreadyExistsException, IOException
	{
		MasterKey masterKey;

		if (isEmpty())
			masterKey = init();
		else
			masterKey = getMasterKey(authUserName, authPassword);

//		String userNameAlias = getAliasForUserName(userName);
//		try {
//			if (jks.containsAlias(userNameAlias))
//				throw new UserAlreadyExistsException("The user \"" + userName + "\" already exists!");
//
//			jks.setKeyEntry(userNameAlias, masterKey.getKey(), password, null);
//		} catch (KeyStoreException e) {
//			throw new RuntimeException(e);
//		}

		byte[] salt = new byte[8]; // TODO make salt-length configurable!
		secureRandom.nextBytes(salt);
		byte[] encrypted;
		try {
			Cipher cipher = getCipherForUserPassword(password, salt, Cipher.ENCRYPT_MODE);
			encrypted = cipher.doFinal(masterKey.getKey().getEncoded());
		} catch (GeneralSecurityException e) {
			throw new RuntimeException(e);
		}

		EncryptedKey encryptedKey = new EncryptedKey(encrypted, salt, masterKey.getKey().getAlgorithm());
		user2keyMap.put(userName, encryptedKey);

		storeToFile();
	}

	private static char[] KEY_STORE_PASSWORD = new char[0];

	protected synchronized void storeToFile() throws IOException
	{
		File newKeyStoreFile = getNewKeyStoreFile();
		boolean deleteNewKeyStoreFile = true;
		try {
			FileOutputStream out = new FileOutputStream(newKeyStoreFile);
//			try {
//				jks.store(out, KEY_STORE_PASSWORD);
//			} catch (KeyStoreException e) {
//				throw new IOException("Storing JKS failed!", e);
//			} catch (NoSuchAlgorithmException e) {
//				throw new IOException("Storing JKS failed!", e);
//			} catch (CertificateException e) {
//				throw new IOException("Storing JKS failed!", e);
//			}
			store(out);
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

	public synchronized Set<String> getUsers(String authUserName, char[] authPassword)
	throws LoginException
	{
		if (isEmpty())
			return Collections.emptySet();

		// The following getMasterKey(...) is no real protection, because the information returned by this method
		// is currently not protected, but this way, we already have the right arguments to later encrypt this
		// information, too - if we ever want to.
		// Marco :-)
		getMasterKey(authUserName, authPassword);

//		Set<String> result = new HashSet<String>(); // or better use a TreeSet?
//
//		try {
//			for (Enumeration<String> enumS = jks.aliases(); enumS.hasMoreElements(); ) {
//				String alias = enumS.nextElement();
//				if (alias.startsWith(ALIAS_USER_PREFIX)) {
//					result.add(alias.substring(ALIAS_USER_PREFIX.length()));
//				}
//			}
//		} catch (KeyStoreException e) {
//			throw new RuntimeException(e);
//		}
//
//		return Collections.unmodifiableSet(result);
		return Collections.unmodifiableSet(user2keyMap.keySet());
	}

	/**
	 * Delete the user specified by <code>userName</code>.
	 *
	 * @param authUserName the name of the principal, i.e. the user authorizing this operation.
	 * @param authPassword the password of the principal.
	 * @param delUserName the name of the user to be deleted.
	 */
	public synchronized void deleteUser(String authUserName, char[] authPassword, String delUserName)
	throws LoginException, UserDoesNotExistException, CannotDeleteLastUserException, IOException
	{
		// The following getMasterKey(...) is no real protection, because a user can be deleted without
		// authenticating on the file-base (as this doesn't require to decrypt data, currently), but
		// this way, we already have the right arguments here and might later encrypt the required infos.
		// Marco :-)
		getMasterKey(authUserName, authPassword);

		EncryptedKey encryptedKey = user2keyMap.get(delUserName);
		if (encryptedKey == null)
			throw new UserDoesNotExistException("The user \"" + delUserName + "\" does not exist!");

		if (user2keyMap.size() == 1)
			throw new CannotDeleteLastUserException("You cannot delete the last user and \"" + delUserName + "\" is the last user!");

		clearCache(delUserName);
		user2keyMap.remove(delUserName);

//		Set<String> users = getUsers(authUserName, authPassword);
//
//		if (users.size() == 1 && users.contains(delUserName))
//			throw new CannotDeleteLastUserException("You cannot delete the last user and \"" + delUserName + "\" is the last user!");
//
//		String delUserAlias = getAliasForUserName(delUserName);
//		try {
//			if (!jks.containsAlias(delUserAlias))
//				throw new UserDoesNotExistException("The user \"" + delUserName + "\" does not exist!");
//
//			jks.deleteEntry(delUserAlias);
//		} catch (KeyStoreException e) {
//			throw new RuntimeException(e);
//		}

		storeToFile();
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

	public synchronized void changeUserPassword(String authUserName, char[] authPassword, String userName, char[] newPassword)
	throws LoginException, UserDoesNotExistException, IOException
	{
		throw new UnsupportedOperationException("NYI");
//		MasterKey masterKey = getMasterKey(authUserName, authPassword);
//
//		String userNameAlias = getAliasForUserName(userName);
//		try {
//			if (!jks.containsAlias(userNameAlias))
//				throw new UserDoesNotExistException("The user \"" + userName + "\" does not exist!");
//
//			jks.setKeyEntry(userNameAlias, masterKey.getKey(), newPassword, null);
//		} catch (KeyStoreException e) {
//			// should only be thrown, if the keyStore is not initialized and this is done in our constructor!
//			throw new RuntimeException(e);
//		}
//
//		storeToFile();
	}

	public synchronized Key getKey(String authUserName, char[] authPassword, long keyID)
	throws LoginException
	{
		MasterKey masterKey = getMasterKey(authUserName, authPassword);
		EncryptedKey encryptedKey = keyID2keyMap.get(keyID);
		if (encryptedKey == null)
			return null;

		try {
			Cipher cipher = getCipherForMasterKey(masterKey, Cipher.DECRYPT_MODE);
			byte[] decrypted = cipher.doFinal(encryptedKey.getData());
			return new SecretKeySpec(decrypted, encryptedKey.getAlgorithm());
		} catch (GeneralSecurityException e) {
			throw new RuntimeException(e);
		}

//		String keyAlias = getAliasForKey(keyID);
//		try {
//			Key key = jks.getKey(keyAlias, toCharArray(masterKey.getKey().getEncoded()));
//			return key;
//		} catch (KeyStoreException e) {
//			// should only be thrown, if the keyStore is not initialized and this is done in our constructor!
//			throw new RuntimeException(e);
//		} catch (UnrecoverableKeyException e) {
//			// This means our masterKey is wrong - should never happen!
//			throw new RuntimeException(e);
//		} catch (NoSuchAlgorithmException e) {
//			// Means sth. is very weird, too.
//			throw new RuntimeException(e);
//		}
	}

	protected synchronized void setKey(String authUserName, char[] authPassword, long keyID, Key key)
	throws LoginException, IOException
	{
		MasterKey masterKey = getMasterKey(authUserName, authPassword);

//		String keyAlias = getAliasForKey(keyID);
//		try {
//			jks.setKeyEntry(keyAlias, key, toCharArray(masterKey.getKey().getEncoded()), null);
//		} catch (KeyStoreException e) {
//			// should only be thrown, if the keyStore is not initialized and this is done in our constructor!
//			throw new RuntimeException(e);
//		}
		try {
			Cipher cipher = getCipherForMasterKey(masterKey, Cipher.ENCRYPT_MODE);
			byte[] encrypted = cipher.doFinal(key.getEncoded());
			EncryptedKey encryptedKey = new EncryptedKey(encrypted, null, key.getAlgorithm());
			keyID2keyMap.put(keyID, encryptedKey);
		} catch (GeneralSecurityException e) {
			throw new RuntimeException(e);
		}

		storeToFile();
	}

	/**
	 * Clear all cached data for the specified user name. Every time, a user
	 * calls a method requiring <code>principalUserName</code> and <code>principalPassword</code>),
	 * either a authentication process happens implicitely, or a previously cached authentication
	 * result is used. In order to speed things up, authentication results are cached for a
	 * limited time. After this time elapses, the data is cleared by a timer. If a user wants (for security reasons)
	 * remove the cached data from the memory earlier, he can call this method from the outside.
	 *
	 * @param userName the user for which to clear all the cached data.
	 */
	public synchronized void clearCache(String userName)
	{

	}
}
