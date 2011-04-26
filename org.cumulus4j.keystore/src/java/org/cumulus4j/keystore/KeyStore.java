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
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedList;
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

/**
 * @author Marco หงุ่ยตระกูล-Schulze - marco at nightlabs dot de
 */
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
		public void run()
		{
			KeyStore keyStore = keyStoreRef.get();
			if (keyStore == null) {
				logger.info("run: KeyStore has been garbage-collected. Removing this ExipreCacheEntryTimerTask.");
				this.cancel();
				return;
			}

			Date removeCachedEntriesOlderThanThisDate = new Date(System.currentTimeMillis() - 1L * 60L * 1000L); // TODO make this configurable!

			LinkedList<String> userNamesToExpire = new LinkedList<String>();
			synchronized (keyStore) {
				for (CachedMasterKey cmk : keyStore.cache_userName2cachedMasterKey.values()) {
					if (cmk.getLastUse().before(removeCachedEntriesOlderThanThisDate))
						userNamesToExpire.add(cmk.getUserName());
				}
			}

			for (String userName : userNamesToExpire) {
				logger.info("run: Expiring cache for user '{}'.", userName);
				keyStore.clearCache(userName);
			}
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



	private Map<String, Integer> stringConstant2idMap = new HashMap<String, Integer>();
	private ArrayList<String> stringConstantList = new ArrayList<String>();

	private synchronized String stringConstant(String s)
	{
		Integer idx = stringConstant2idMap.get(s);
		if (idx == null) {
			stringConstant2idMap.put(s, stringConstantList.size());
			stringConstantList.add(s);
			return s;
		}
		else {
			String e = stringConstantList.get(idx);
			if (!s.equals(e))
				throw new IllegalStateException("Map and list are not matching!");

			return e;
		}
	}

	protected synchronized KeyGenerator getKeyGenerator(int keySize)
	{
		KeyGenerator keyGenerator = keySize2keyGenerator.get(keySize);

		if (keyGenerator == null) {
			try {
				keyGenerator = KeyGenerator.getInstance(getBaseAlgorithm(getEncryptionAlgorithm()));
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

		int stringConstantSize = din.readInt();
		stringConstant2idMap.clear();
		stringConstantList.clear();
		for (int i = 0; i < stringConstantSize; ++i) {
			String stringConstant = din.readUTF();
			stringConstant(stringConstant);
		}

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

		for (char c : FILE_HEADER.toCharArray()) {
			if (c > 255)
				throw new IllegalStateException("No character in FILE_HEADER should be outside the range 0...255!!! c=" + (int)c);

			dout.writeByte(c);
		}

		dout.writeInt(FILE_VERSION);
		dout.writeLong(nextKeyID);

		dout.writeInt(stringConstantList.size());
		for (String stringConstant : stringConstantList) {
			dout.writeUTF(stringConstant);
		}

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

		Integer idx = stringConstant2idMap.get(key.getAlgorithm());
		dout.writeInt(idx);
//		dout.writeUTF(key.getAlgorithm());

		dout.writeInt(key.getKeyEncryptionIV().length);
		dout.write(key.getKeyEncryptionIV());

		idx = stringConstant2idMap.get(key.getKeyEncryptionAlgorithm());
		dout.writeInt(idx);
//		dout.writeUTF(key.getKeyEncryptionAlgorithm());

		dout.writeInt(key.getHash().length);
		dout.write(key.getHash());

		idx = stringConstant2idMap.get(key.getHashAlgorithm());
		dout.writeInt(idx);
//		dout.writeUTF(key.getHashAlgorithm());
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

		int algorithmIdx = din.readInt();
		String algorithm = stringConstantList.get(algorithmIdx);
//		String algorithm = din.readUTF();
//		algorithm = stringConstant(algorithm);

		int keyCryptIVSize = din.readInt();
		byte[] keyCryptIV = keyCryptIVSize == 0 ? null : new byte[keyCryptIVSize];
		if (keyCryptIV != null)
			readByteArrayCompletely(din, keyCryptIV);

		int keyCryptAlgoIdx = din.readInt();
		String keyCryptAlgo = stringConstantList.get(keyCryptAlgoIdx);
//		String keyCryptAlgo = din.readUTF();
//		keyCryptAlgo = stringConstant(keyCryptAlgo);

		int hashSize = din.readInt();
		byte hash[] = hashSize == 0 ? null : new byte[hashSize];
		if (hash != null)
			readByteArrayCompletely(din, hash);

		int hashAlgoIdx = din.readInt();
		String hashAlgo = stringConstantList.get(hashAlgoIdx);
//		String hashAlgo = din.readUTF();
//		hashAlgo = stringConstant(hashAlgo);

		return new EncryptedKey(key, salt, algorithm, keyCryptIV, keyCryptAlgo, hash, hashAlgo);
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

	private Map<String, CachedMasterKey> cache_userName2cachedMasterKey = new HashMap<String, CachedMasterKey>();

	protected synchronized MasterKey getMasterKey(String authUserName, char[] authPassword)
	throws LoginException
	{
		CachedMasterKey cachedMasterKey = cache_userName2cachedMasterKey.get(authUserName);
		MasterKey result = cachedMasterKey == null ? null : cachedMasterKey.getMasterKey();
		if (result != null && Arrays.equals(authPassword, cachedMasterKey.getPassword())) {
			cachedMasterKey.updateLastUse();
			return result;
		}

		EncryptedKey encryptedKey = user2keyMap.get(authUserName);
		if (encryptedKey == null)
			logger.warn("login: Unknown userName: {}", authUserName);
		else {
			try {
				Cipher cipher = getCipherForUserPassword(
						authPassword, encryptedKey.getSalt(),
						encryptedKey.getKeyEncryptionIV(), encryptedKey.getKeyEncryptionAlgorithm(),
						Cipher.DECRYPT_MODE
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

		cache_userName2cachedMasterKey.put(authUserName, new CachedMasterKey(authUserName, authPassword, result));
		return result;
	}

	private Cipher getCipherForUserPassword(char[] password, byte[] salt, byte[] iv, String algorithm, int opmode) throws GeneralSecurityException
	{
		if (iv == null) {
			if (Cipher.ENCRYPT_MODE != opmode)
				throw new IllegalArgumentException("iv must not be null when decrypting!");
		}
		else {
			if (Cipher.ENCRYPT_MODE == opmode)
				throw new IllegalArgumentException("iv must be null when encrypting!");
		}

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

		if (iv == null)
			cipher.init(opmode, secret);
		else
			cipher.init(opmode, secret, new IvParameterSpec(iv));

		return cipher;
	}


	private String getBaseAlgorithm(String algorithm)
	{
		int slashIdx = algorithm.indexOf('/');
		if (slashIdx < 0)
			return algorithm;

		return algorithm.substring(0, slashIdx);
	}

	private Cipher getCipherForMasterKey(MasterKey masterKey, byte[] iv, String algorithm, int opmode) throws GeneralSecurityException
	{
		if (iv == null) {
			if (Cipher.ENCRYPT_MODE != opmode)
				throw new IllegalArgumentException("iv must not be null when decrypting!");
		}
		else {
			if (Cipher.ENCRYPT_MODE == opmode)
				throw new IllegalArgumentException("iv must be null when encrypting!");
		}

		if (algorithm == null) {
			if (Cipher.ENCRYPT_MODE != opmode)
				throw new IllegalArgumentException("algorithm must not be null when decrypting!");

			algorithm = getEncryptionAlgorithm();
		}

		Cipher cipher = Cipher.getInstance(algorithm);

		if (iv == null)
			cipher.init(opmode, masterKey.getKey());
		else
			cipher.init(opmode, masterKey.getKey(), new IvParameterSpec(iv));

		return cipher;
	}

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

	public synchronized void createUser(String authUserName, char[] authPassword, String userName, char[] password)
	throws LoginException, UserAlreadyExistsException, IOException
	{
		if (userName == null)
			throw new IllegalArgumentException("userName must not be null!");

		if (password == null)
			throw new IllegalArgumentException("password must not be null!");

		MasterKey masterKey;

		if (isEmpty()) {
			Key key = getKeyGenerator(getKeySize()).generateKey();
			masterKey = new MasterKey(key);
		}
		else
			masterKey = getMasterKey(authUserName, authPassword);

		if (user2keyMap.containsKey(userName))
			throw new UserAlreadyExistsException("User '" + userName + "' already exists!");

		setUser(masterKey, userName, password);
	}

	protected synchronized void setUser(MasterKey masterKey, String userName, char[] password)
	throws IOException
	{
		String hashAlgo = HASH_ALGORITHM_SIMPLE_XOR;
		byte[] hash = hash(masterKey.getKey().getEncoded(), userName.getBytes(UTF8), hashAlgo);

		byte[] salt = new byte[8]; // TODO make salt-length configurable!
		secureRandom.nextBytes(salt);
		try {
			Cipher cipher = getCipherForUserPassword(password, salt, null, null, Cipher.ENCRYPT_MODE);
			byte[] encrypted = cipher.doFinal(masterKey.getKey().getEncoded());

			EncryptedKey encryptedKey = new EncryptedKey(
					encrypted, salt,
					stringConstant(masterKey.getKey().getAlgorithm()),
					cipher.getIV(), stringConstant(cipher.getAlgorithm()),
					hash, stringConstant(hashAlgo)
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
			OutputStream out = new FileOutputStream(newKeyStoreFile);
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
		MasterKey masterKey = getMasterKey(authUserName, authPassword);

		if (!user2keyMap.containsKey(userName))
			throw new UserDoesNotExistException("User '" + userName + "' does not exist!");

		setUser(masterKey, userName, newPassword);
	}

	public synchronized Key getKey(String authUserName, char[] authPassword, long keyID)
	throws LoginException
	{
		MasterKey masterKey = getMasterKey(authUserName, authPassword);
		EncryptedKey encryptedKey = keyID2keyMap.get(keyID);
		if (encryptedKey == null)
			return null;

		try {
			Cipher cipher = getCipherForMasterKey(
					masterKey,
					encryptedKey.getKeyEncryptionIV(),
					encryptedKey.getKeyEncryptionAlgorithm(),
					Cipher.DECRYPT_MODE
			);
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
			Cipher cipher = getCipherForMasterKey(masterKey, null, null, Cipher.ENCRYPT_MODE);
			byte[] encrypted = cipher.doFinal(key.getEncoded());
			EncryptedKey encryptedKey = new EncryptedKey(
					encrypted, null, stringConstant(key.getAlgorithm()),
					cipher.getIV(), stringConstant(cipher.getAlgorithm()),
					hash, stringConstant(hashAlgo)
			);
			keyID2keyMap.put(keyID, encryptedKey);
		} catch (GeneralSecurityException e) {
			throw new RuntimeException(e);
		}

		storeToFile();
	}

	/**
	 * Clear all cached data for the specified user name. Every time, a user
	 * calls a method requiring <code>authUserName</code> and <code>authPassword</code>),
	 * either an authentication process happens implicitely, or a previously cached authentication
	 * result is used. In order to speed things up, authentication results are cached for a
	 * limited time. After this time elapses, the data is cleared by a timer. If a user wants (for security reasons)
	 * remove the cached data from the memory earlier, he can call this method from the outside.
	 *
	 * @param userName the user for which to clear all the cached data. <code>null</code> to clear the complete cache for all users.
	 */
	public synchronized void clearCache(String userName)
	{
		if (userName == null) {
			for(CachedMasterKey cachedMasterKey : cache_userName2cachedMasterKey.values())
				cachedMasterKey.clear();

			cache_userName2cachedMasterKey.clear();
		}
		else {
			CachedMasterKey cachedMasterKey = cache_userName2cachedMasterKey.remove(userName);
			cachedMasterKey.clear();
		}
	}

	@Override
	protected void finalize() throws Throwable {
		clearCache(null);
		super.finalize();
	}
}
