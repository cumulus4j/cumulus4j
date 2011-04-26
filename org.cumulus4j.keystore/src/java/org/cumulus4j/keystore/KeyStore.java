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
import java.nio.charset.Charset;
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

import javax.crypto.BadPaddingException;
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

	private static final Charset UTF8 = Charset.forName("UTF-8");

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

				logger.info("getKeySize: System property '{}' is set to {} bit. Using this key size.", keySizePropName, ks);
			}
			keySize = ks;
		}

		return ks;
	}
	private int keySize = 0;

	protected String getEncryptionAlgorithm()
	{
		String ea = encryptionAlgorithm;

		if (ea == null) {
			String encryptionAlgorithmPropName = KeyStore.class.getName() + ".encryptionAlgorithm";
			String encryptionAlgorithmPropValue = System.getProperty(encryptionAlgorithmPropName);
			if (encryptionAlgorithmPropValue == null || encryptionAlgorithmPropValue.trim().isEmpty()) {
//				ea = "AES/CBC/PKCS5Padding"; // default value, if the property was not defined.
				ea = "AES/CFB/NoPadding"; // default value, if the property was not defined.
				logger.info("getEncryptionAlgorithm: System property '{}' is not set. Using default algorithm '{}'.", encryptionAlgorithmPropName, ea);
			}
			else {
				ea = encryptionAlgorithmPropValue.trim();
				logger.info("getEncryptionAlgorithm: System property '{}' is set to '{}'. Using this encryption algorithm.", encryptionAlgorithmPropName, ea);
			}
			encryptionAlgorithm = ea;
		}

		return ea;
	}
	private String encryptionAlgorithm = null;

	private long nextKeyID = 1;
	private Map<String, EncryptedKey> user2keyMap = new HashMap<String, EncryptedKey>();
	private Map<Long, EncryptedKey> keyID2keyMap = new HashMap<Long, EncryptedKey>();

	private Map<Integer, KeyGenerator> keySize2keyGenerator = new HashMap<Integer, KeyGenerator>();



	private Map<String, String> stringConstantMap = new HashMap<String, String>();

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

	public KeyStore(File keyStoreFile) throws IOException
	{
		if (keyStoreFile == null)
			throw new IllegalArgumentException("keyStoreFile == null");

		this.keyStoreFile = keyStoreFile;

		if (!keyStoreFile.getParentFile().isDirectory())
			throw new FileNotFoundException("Path does not exist or is not a directory: " + keyStoreFile.getParentFile().getAbsolutePath());

		// In case the old file was already deleted, but the new not yet renamed, we check, if a new file
		// exists and the old file is missing - in this case, we load the new file.
		File newKeyStoreFile = getNewKeyStoreFile();
		if (!keyStoreFile.exists() && newKeyStoreFile.exists())
			keyStoreFile = newKeyStoreFile;

		FileInputStream in = keyStoreFile.length() == 0 ? null : new FileInputStream(keyStoreFile);

		if (in != null)
			load(in);

		if (in != null)
			in.close();

		expireCacheEntryTimer.schedule(expireCacheEntryTimerTask, 60000, 60000); // TODO make this configurable
	}

	private static final String FILE_HEADER = "Cumulus4jKeyStore";
	private static final int FILE_VERSION = 1;

	private void load(InputStream in)
	throws IOException
	{
		stringConstantMap.clear();

		DataInputStream din = new DataInputStream(new BufferedInputStream(in));

		char[] fileHeaderCharArray = FILE_HEADER.toCharArray();
		char[] buf = new char[fileHeaderCharArray.length];
		for (int i = 0; i < buf.length; i++)
			buf[i] = (char) ( din.readByte() & 0xff );

		if (!Arrays.equals(fileHeaderCharArray, buf))
			throw new IOException("Stream does not start with expected HEADER!");

		int fileVersion = din.readInt();
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

	private String stringConstant(String s)
	{
		String v = stringConstantMap.get(s);
		if (v == null)
			stringConstantMap.put(s, s);
		else
			s = v;

		return s;
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

		for (char c : FILE_HEADER.toCharArray()) {
			if (c > 255)
				throw new IllegalStateException("No character in FILE_HEADER should be outside the range 0...255!!! c=" + (int)c);

			dout.writeByte(c);
		}

		dout.writeInt(FILE_VERSION);
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
		dout.flush();
	}

	private void writeKey(DataOutputStream dout, EncryptedKey key) throws IOException
	{
		dout.writeInt(key.getData().length);
		dout.write(key.getData());

		dout.writeInt(key.getSalt().length);
		dout.write(key.getSalt());

		dout.writeUTF(key.getAlgorithm());

		dout.writeUTF(key.getKeyEncryptionAlgorithm());

		dout.writeInt(key.getHash().length);
		dout.write(key.getHash());

		dout.writeUTF(key.getHashAlgorithm());
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
		algorithm = stringConstant(algorithm);

		String keyCryptAlgo = din.readUTF();
		keyCryptAlgo = stringConstant(keyCryptAlgo);

		int hashSize = din.readInt();
		byte hash[] = hashSize == 0 ? null : new byte[hashSize];
		if (hash != null)
			readByteArrayCompletely(din, hash);

		String hashAlgo = din.readUTF();
		hashAlgo = stringConstant(hashAlgo);

		return new EncryptedKey(key, salt, algorithm, keyCryptAlgo, hash, hashAlgo);
	}

	protected File getNewKeyStoreFile()
	{
		return new File(keyStoreFile.getParentFile(), keyStoreFile.getName() + ".new");
	}

	public synchronized boolean isEmpty()
	{
		return user2keyMap.isEmpty();
	}

	protected synchronized long nextKeyID()
	{
		long result = nextKeyID++;
		return result;
	}

	protected synchronized MasterKey getMasterKey(String authUserName, char[] authPassword)
	throws LoginException
	{
		MasterKey result = null;

		// TODO consult a cache to speed things up!

		EncryptedKey encryptedKey = user2keyMap.get(authUserName);
		if (encryptedKey == null)
			logger.warn("login: Unknown userName: {}", authUserName);
		else {
			try {
				Cipher cipher = getCipherForUserPassword(
						authPassword, encryptedKey.getSalt(), encryptedKey.getKeyEncryptionAlgorithm(), Cipher.DECRYPT_MODE
				);
				byte[] decrypted = cipher.doFinal(encryptedKey.getData());
				result = new MasterKey(new SecretKeySpec(decrypted, encryptedKey.getAlgorithm()));

				if (encryptedKey.getHash().length > 0) {
					byte[] hash = hash(decrypted, authUserName.getBytes(UTF8), encryptedKey.getHashAlgorithm());
					if (!Arrays.equals(encryptedKey.getHash(), hash)) {
						result = null;
						logger.warn("login: Wrong password for user \"{}\"!", authUserName);
					}
				}
			} catch (BadPaddingException x) {
				logger.warn("login: Caught BadPaddingException indicating a wrong password for user \"{}\"!", authUserName);
				result = null;
			} catch (GeneralSecurityException x) {
				throw new RuntimeException(x);
			}
		}

		if (result == null)
			throw new LoginException("Unknown user \"" + authUserName + "\" or wrong password!");

		return result;
	}

	private Cipher getCipherForUserPassword(char[] password, byte[] salt, String algorithm, int opmode) throws GeneralSecurityException
	{
		if (algorithm == null) {
			if (Cipher.ENCRYPT_MODE != opmode)
				throw new IllegalArgumentException("algorithm must not be null when decrypting!");

			algorithm = getEncryptionAlgorithm();
		}

		SecretKeyFactory factory = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA1"); // TODO make configurable!
//		SecretKeyFactory factory = SecretKeyFactory.getInstance("PBEWithSHA256And256BitAES-CBC-BC");
		KeySpec spec = new PBEKeySpec(password, salt, 1024, getKeySize()); // TODO make iteration-count configurable!
		SecretKey tmp = factory.generateSecret(spec);
		SecretKey secret = new SecretKeySpec(tmp.getEncoded(), getBaseAlgorithm(algorithm));
		Cipher cipher = Cipher.getInstance(algorithm);
		byte[] iv = new byte[cipher.getBlockSize()];
		Arrays.fill(iv, (byte)0); // No need of an IV, because we use a salt.
		cipher.init(opmode, secret, new IvParameterSpec(iv));
		return cipher;
	}


	private String getBaseAlgorithm(String algorithm)
	{
		int slashIdx = algorithm.indexOf('/');
		if (slashIdx < 0)
			return stringConstant(algorithm);

		return stringConstant(algorithm.substring(0, slashIdx));
	}

	private Cipher getCipherForMasterKey(MasterKey masterKey, String algorithm, int opmode) throws GeneralSecurityException
	{
		if (algorithm == null) {
			if (Cipher.ENCRYPT_MODE != opmode)
				throw new IllegalArgumentException("algorithm must not be null when decrypting!");

			algorithm = getEncryptionAlgorithm();
		}

		Cipher cipher = Cipher.getInstance(algorithm);
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
		if (userName == null)
			throw new IllegalArgumentException("userName must not be null!");

		if (password == null)
			throw new IllegalArgumentException("password must not be null!");

		MasterKey masterKey;

		if (isEmpty())
			masterKey = init();
		else
			masterKey = getMasterKey(authUserName, authPassword);

		String hashAlgo = HASH_ALGORITHM_SIMPLE_XOR;
		byte[] hash = hash(masterKey.getKey().getEncoded(), userName.getBytes(UTF8), hashAlgo);

		byte[] salt = new byte[8]; // TODO make salt-length configurable!
		secureRandom.nextBytes(salt);
		try {
			Cipher cipher = getCipherForUserPassword(password, salt, null, Cipher.ENCRYPT_MODE);
			byte[] encrypted = cipher.doFinal(masterKey.getKey().getEncoded());

			EncryptedKey encryptedKey = new EncryptedKey(
					encrypted, salt, masterKey.getKey().getAlgorithm(), cipher.getAlgorithm(), hash, hashAlgo
			);
			user2keyMap.put(userName, encryptedKey);
		} catch (GeneralSecurityException e) {
			throw new RuntimeException(e);
		}

		storeToFile();
	}

	private static final String HASH_ALGORITHM_SIMPLE_XOR = "SXOR";

	private byte[] hash(byte[] data, byte[] salt, String hashAlgorithm)
	{
		if (HASH_ALGORITHM_SIMPLE_XOR.equals(hashAlgorithm)) {
			byte[] hash = new byte[8];
			int hi = 0;
			int si = 0;
			for (int i = 0; i < data.length; ++i) {
				if (salt != null) {
					hash[hi] ^= salt[si];
					if (++si >= salt.length)
						si = 0;
				}

				hash[hi] ^= data[i];
				if (++hi >= hash.length)
					hi = 0;
			}
			return hash;
		}
		else
			throw new UnsupportedOperationException("Unsupported hash algorithm: " + hashAlgorithm);
	}

	protected synchronized void storeToFile() throws IOException
	{
		File newKeyStoreFile = getNewKeyStoreFile();
		boolean deleteNewKeyStoreFile = true;
		try {
			FileOutputStream out = new FileOutputStream(newKeyStoreFile);
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
			Cipher cipher = getCipherForMasterKey(masterKey, encryptedKey.getAlgorithm(), Cipher.DECRYPT_MODE);
			byte[] decrypted = cipher.doFinal(encryptedKey.getData());

			if (encryptedKey.getHash().length > 0) {
				byte[] hash = hash(decrypted, authUserName.getBytes(UTF8), encryptedKey.getHashAlgorithm());
				if (!Arrays.equals(encryptedKey.getHash(), hash))
					throw new IllegalStateException("Hash codes do not match!!!");
			}

			return new SecretKeySpec(decrypted, encryptedKey.getAlgorithm());
		} catch (GeneralSecurityException e) {
			throw new RuntimeException(e);
		}
	}

	protected synchronized void setKey(String authUserName, char[] authPassword, long keyID, Key key)
	throws LoginException, IOException
	{
		MasterKey masterKey = getMasterKey(authUserName, authPassword);

		String hashAlgo = HASH_ALGORITHM_SIMPLE_XOR;
		byte[] hash = hash(key.getEncoded(), authUserName.getBytes(UTF8), hashAlgo);

		try {
			Cipher cipher = getCipherForMasterKey(masterKey, null, Cipher.ENCRYPT_MODE);
			byte[] encrypted = cipher.doFinal(key.getEncoded());
			EncryptedKey encryptedKey = new EncryptedKey(
					encrypted, null, key.getAlgorithm(), cipher.getAlgorithm(), hash, hashAlgo
			);
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
