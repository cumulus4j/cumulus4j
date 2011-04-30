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
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.spec.KeySpec;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.Map;
import java.util.Set;
import java.util.Timer;
import java.util.TimerTask;
import java.util.zip.CRC32;
import java.util.zip.CheckedInputStream;
import java.util.zip.CheckedOutputStream;

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
 * <p>
 * <code>KeyStore</code> is a storage facility for cryptographic keys.
 * </p>
 * <p>
 * An instance of <code>KeyStore</code> manages a file in the local file system, in which it stores
 * the keys used by the Cumulus4j-DataNucleus-plug-in in an encrypted form. All data written to the
 * file is encrypted, hence plain data never touches the local file system (except for
 * <a href="http://en.wikipedia.org/wiki/Swap_space">swapping</a>!).
 * </p>
 * <p>
 * For every read/write operation, the <code>KeyStore</code> requires a user to authenticate via a
 * user-name and a password. The password is used to encrypt/decrypt an internally used master-key
 * which is then used to encrypt/decrypt the actual keys used by the Cumulus4j-DataNucleus-plug-in.
 * Due to this internal master key, a user can be added or deleted and a user's password can be
 * changed without the need of decrypting and encrypting all the contents of the KeyStore.
 * </p>
 * <p>
 * By default, a <code>KeyStore</code> {@link #generateKey(String, char[]) generates keys} with a size
 * of 128 bit. This can be controlled, however, by specifying the system property
 * {@value #PROPERTY_KEY_SIZE} (e.g. passing the argument "-Dorg.cumulus4j.keystore.KeyStore.keySize=256"
 * to the <code>java</code> command line will switch to 256-bit-keys).
 * </p>
 * <p>
 * <b>Important:</b> As the master key is generated when the first
 * {@link #createUser(String, char[], String, char[]) user is created} and is then not changed anymore, you must therefore
 * specify the desired key-size already at the moment when you initialise the key store (i.e. create the first user). If
 * you change the key-size later, it will affect only those keys that are created later.
 * </p>
 * <p>
 * Note, that key sizes longer than 128
 * bit require the "Java Cryptography Extension (JCE) Unlimited Strength Jurisdiction Policy Files" to be installed!
 * </p>
 * <h3>File format of the key store file (version 1)</h3>
 * <h4>Main</h4>
 * <p>
 * <table border="1" width="100%">
 * 	<tbody>
 * 	<tr>
 * 		<td align="right" valign="top"><b>Bytes</b></td><td valign="top"><b>Descrition</b></td>
 * 	</tr>
 * 	<tr>
 * 		<td align="right" valign="top">17</td><td valign="top">Header "Cumulus4jKeyStore" (ASCII encoded)</td>
 * 	</tr>
 * 	<tr>
 * 		<td align="right" valign="top">4</td><td valign="top">int: File version</td>
 * 	</tr>
 * 	<tr>
 * 		<td align="right" valign="top">8</td><td valign="top">long: Next key ID, i.e. the first not-yet-used ID which will be assigned to the next key that will be generated.</td>
 * 	</tr>
 *	<tr>
 * 		<td align="right" valign="top">4</td><td valign="top">int: Number of entries in 'Block A' to follow.</td>
 * 	</tr>
 * 	<tr>
 * 		<td align="right" valign="top">var</td><td valign="top">Block A: String constants</td>
 * 	</tr>
 * 	<tr>
 * 		<td align="right" valign="top">4</td><td valign="top">int: Number of entries in 'Block B' to follow.</td>
 * 	</tr>
 * 	<tr>
 * 		<td align="right" valign="top">var</td><td valign="top">Block B: User-key-map</td>
 * 	</tr>
 * 	<tr>
 * 		<td align="right" valign="top">4</td><td valign="top">int: Number of entries in 'Block C' to follow.</td>
 * 	</tr>
 * 	<tr>
 * 		<td align="right" valign="top">var</td><td valign="top">Block C: Key-ID-key-map</td>
 * 	</tr>
 * 	</tbody>
 * </table>
 * </p>
 *
 * <h4>Block A: String constants</h4>
 * <p>
 * </p>
 * In order to reduce the file size (and thus increase the write speed), various
 * strings like encryption algorithm, checksum algorithm and the like are not written
 * again and again for every key, but instead only once here. In every key, these
 * Strings are then referenced instead by their position-index (zero-based).
 * <p>
 * <p>
 * Every record in this block has the following structure:
 * </p>
 * <table border="1" width="100%">
 * 	<tbody>
 * 	<tr>
 * 		<td align="right" valign="top"><b>Bytes</b></td><td valign="top"><b>Descrition</b></td>
 * 	</tr>
 * 	<tr>
 * 		<td align="right" valign="top">2</td><td valign="top">short: Number of bytes (X) to follow (written by {@link DataOutputStream#writeUTF(String)}.</td>
 * 	</tr>
 * 	<tr>
 * 		<td align="right" valign="top">X</td><td valign="top">The actual String constant (written by {@link DataOutputStream#writeUTF(String)}.</td>
 * 	</tr>
 * 	</tbody>
 * </table>
 * </p>
 *
 * <h4>Block B: User-key-map</h4>
 * <p>
 * TODO document this!
 * </p>
 *
 * <h4>Block C: Key-ID-key-map</h4>
 * <p>
 * TODO document this!
 * </p>
 *
 * @author Marco หงุ่ยตระกูล-Schulze - marco at nightlabs dot de
 */
public class KeyStore
{
	private static final Logger logger = LoggerFactory.getLogger(KeyStore.class);

	/**
	 * <p>
	 * System property to control the size of the keys {@link #generateKey(String, char[]) generated}. This
	 * includes not only the actual keys for the main encryption/decryption (in the database), but also the
	 * master key used to protect the file managed by the <code>KeyStore</code>.
	 * </p>
	 * <p>
	 * By default (if the system property {@value #PROPERTY_KEY_SIZE} is not specified), keys will have a size of 128 bit.
	 * </p>
	 * <p>
	 * Note, that specifying the system property does currently not change any old keys - only new keys are generated
	 * with the currently active key size. Therefore, if you want to ensure that the internal master key is 256 bit long,
	 * you have to make sure that the proper key size is specified when the first
	 * {@link #createUser(String, char[], String, char[]) user is created}!
	 * </p>
	 */
	public static final String PROPERTY_KEY_SIZE = "org.cumulus4j.keystore.KeyStore" + ".keySize";

	/**
	 * <p>
	 * System property to control the encryption algorithm that is used to encrypt data. Whenever a new user is
	 * created or a new key is generated, data has to be encrypted (note that the encryption does not happen directly
	 * before data is written to the file, but already most data in memory is encrypted!).
	 * </p>
	 * <p>
	 * By default (if the system property {@value #PROPERTY_ENCRYPTION_ALGORITHM} is not specified),
	 * "AES/CFB/NoPadding" is used. For example, to switch to "AES/CBC/PKCS5Padding", you'd have
	 * to specify the command line argument "-Dorg.cumulus4j.keystore.KeyStore.encryptionAlgorithm=AES/CBC/PKCS5Padding".
	 * </p>
	 * <p>
	 * See <a href="http://download.java.net/jdk7/docs/technotes/guides/security/SunProviders.html#SunJCEProvider">this document</a>
	 * for further information about what values are supported.
	 * </p>
	 */
	public static final String PROPERTY_ENCRYPTION_ALGORITHM = "org.cumulus4j.keystore.KeyStore" + ".encryptionAlgorithm";

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

			Date removeCachedEntriesOlderThanThisDate = new Date(System.currentTimeMillis() - 3L * 60L * 1000L); // TODO make this configurable!

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

			if (logger.isDebugEnabled()) {
				synchronized (keyStore) {
					logger.debug("run: {} users left in cache.", keyStore.cache_userName2cachedMasterKey.size());
				}
			}
		}
	}

	/**
	 * Gets the key-size that is currently configured. Therefore, this method checks, if the
	 * system property {@value #PROPERTY_KEY_SIZE} has been specified, and if so returns its value.
	 * If not, it falls back to 128.
	 *
	 * @return the current key-size.
	 */
	int getKeySize()
	{
		int ks = keySize;

		if (ks == 0) {
			String keySizePropName = PROPERTY_KEY_SIZE;
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


	String getEncryptionAlgorithm()
	{
		String ea = encryptionAlgorithm;

		if (ea == null) {
			String encryptionAlgorithmPropName = PROPERTY_ENCRYPTION_ALGORITHM;
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

	synchronized KeyGenerator getKeyGenerator(int keySize)
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

	/**
	 * <p>
	 * Create a new instance of <code>KeyStore</code>.
	 * </p>
	 * <p>
	 * If the file specified by <code>keyStoreFile</code> exists, it is read into memory. If it does not exist,
	 * an empty <code>KeyStore</code> is created and written to this file.
	 * </p>
	 *
	 * @param keyStoreFile the file to be read (if existing) or created. Note that temporary files (and later maybe backup files, too)
	 * are created in the same directory (i.e. in {@link File#getParentFile() keyStoreFile.getParentFile()}).
	 * @throws IOException if reading from or writing to the local file-system failed.
	 */
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

		if (in == null)
			storeToFile();

		expireCacheEntryTimer.schedule(expireCacheEntryTimerTask, 60000, 60000); // TODO make this configurable
	}

	private static final String FILE_HEADER = "Cumulus4jKeyStore";
	private static final int FILE_VERSION = 1;

	private void load(InputStream in)
	throws IOException
	{
		int headerSizeIncludingVersion = FILE_HEADER.length() + 4; // in bytes

		// We must put the BufferedInputStream here, because we must mark() and reset() - see below. Marco :-)
		in = new BufferedInputStream(in);
		in.mark(headerSizeIncludingVersion);

		DataInputStream din = new DataInputStream(in);

		char[] fileHeaderCharArray = FILE_HEADER.toCharArray();
		char[] buf = new char[fileHeaderCharArray.length];
		for (int i = 0; i < buf.length; i++)
			buf[i] = (char) ( din.readByte() & 0xff );

		if (!Arrays.equals(fileHeaderCharArray, buf))
			throw new IOException("Stream does not start with expected HEADER!");

		int fileVersion = din.readInt();
		if (FILE_VERSION != fileVersion)
			throw new IOException("Version not supported! Stream contains a keystore of version \"" + fileVersion + "\" while version \"" + FILE_VERSION + "\" (or lower) is expected!");

		// The checksum is calculated over the complete file, but in order to know which algorithm
		// to use, we first have to read the beginning of the file until including the version.
		// We thus use mark(...) and reset().
		in.reset();

		MessageDigestChecksum checksum = new MessageDigestChecksum.SHA1();
		CheckedInputStream cin = new CheckedInputStream(in, checksum);
		din = new DataInputStream(cin);
		readByteArrayCompletely(din, new byte[headerSizeIncludingVersion]); // We cannot use skip(...), because that would not update our checksum.

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

		byte[] checksumValueCalculated = checksum.getChecksum();
		byte[] checksumValueExpected = new byte[checksumValueCalculated.length];
		readByteArrayCompletely(din, checksumValueExpected);
		if (!Arrays.equals(checksumValueCalculated, checksumValueExpected))
			throw new IOException("Checksum error! Expected=" + encodeHexStr(checksumValueExpected) + " Found=" + encodeHexStr(checksumValueCalculated));
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
		// We calculate the checksum over the complete file.
		MessageDigestChecksum checksum = new MessageDigestChecksum.SHA1();
		CheckedOutputStream cout = new CheckedOutputStream(out, checksum);

		// We put the BufferedOutputStream around the CheckedOutputStream, because this is significantly faster
		// (nearly factor 2; 86 vs 46 sec) and has no disadvantages. Marco.
		DataOutputStream dout = new DataOutputStream(new BufferedOutputStream(cout));

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

		byte[] checksumValue = checksum.getChecksum();
//		logger.debug("store: checksum={}", checksumValue);
		dout.write(checksumValue);
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

		dout.writeInt(key.getKeyEncryptionIV().length);
		dout.write(key.getKeyEncryptionIV());

		idx = stringConstant2idMap.get(key.getKeyEncryptionAlgorithm());
		dout.writeInt(idx);

		dout.writeShort(key.getChecksumSize());

		idx = stringConstant2idMap.get(key.getChecksumAlgorithm());
		dout.writeInt(idx);
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

		int keyCryptIVSize = din.readInt();
		byte[] keyCryptIV = keyCryptIVSize == 0 ? null : new byte[keyCryptIVSize];
		if (keyCryptIV != null)
			readByteArrayCompletely(din, keyCryptIV);

		int keyCryptAlgoIdx = din.readInt();
		String keyCryptAlgo = stringConstantList.get(keyCryptAlgoIdx);

		short checksumSize = din.readShort();

		int checksumAlgoIdx = din.readInt();
		String checksumAlgo = stringConstantList.get(checksumAlgoIdx);

		return new EncryptedKey(key, salt, algorithm, keyCryptIV, keyCryptAlgo, checksumSize, checksumAlgo);
	}

	File getNewKeyStoreFile()
	{
		return new File(keyStoreFile.getParentFile(), keyStoreFile.getName() + ".new");
	}

	/**
	 * Determine if this <code>KeyStore</code> is completely empty. As soon as the first user has been
	 * created, this method will return <code>false</code>.
	 *
	 * @return <code>true</code> if this <code>KeyStore</code> contains neither any user nor any key, i.e. is totally empty;
	 * <code>false</code> otherwise.
	 */
	public synchronized boolean isEmpty()
	{
		return user2keyMap.isEmpty();
	}

	synchronized long nextKeyID()
	{
		long result = nextKeyID++;
		return result;
	}

	private Map<String, CachedMasterKey> cache_userName2cachedMasterKey = new HashMap<String, CachedMasterKey>();

	/**
	 * Authenticate and get the master-key. If there is a cache-entry existing, directly return this
	 * (after comparing the password); otherwise decrypt the master-key using the given password.
	 *
	 * @param authUserName the user from whose slot to take and decrypt the master-key.
	 * @param authPassword the password with which to try to decrypt the master-key.
	 * @return the decrypted, plain master-key.
	 * @throws AuthenticationException if the specified <code>authUserName</code> does not exist or the specified <code>authPassword</code>
	 * is not correct for the given <code>authUserName</code>.
	 */
	synchronized MasterKey getMasterKey(String authUserName, char[] authPassword)
	throws AuthenticationException
	{
		CachedMasterKey cachedMasterKey = cache_userName2cachedMasterKey.get(authUserName);
		MasterKey result = cachedMasterKey == null ? null : cachedMasterKey.getMasterKey();
		if (result != null && Arrays.equals(authPassword, cachedMasterKey.getPassword())) {
			cachedMasterKey.updateLastUse();
			return result;
		}

		EncryptedKey encryptedKey = user2keyMap.get(authUserName);
		if (encryptedKey == null)
			logger.warn("login: Unknown userName: {}", authUserName); // NOT throw exception here to not disclose the true reason of the AuthenticationException - see below
		else {
			try {
				Cipher cipher = getCipherForUserPassword(
						authPassword, encryptedKey.getSalt(),
						encryptedKey.getKeyEncryptionIV(), encryptedKey.getKeyEncryptionAlgorithm(),
						Cipher.DECRYPT_MODE
				);
				byte[] decrypted = cipher.doFinal(encryptedKey.getData());
//				result = new MasterKey(new SecretKeySpec(decrypted, encryptedKey.getAlgorithm()));

				byte[][] checksumAndData = splitChecksumAndData(decrypted, encryptedKey.getChecksumSize());
				result = new MasterKey(checksumAndData[1], encryptedKey.getAlgorithm());
				byte[] checksum = checksum(checksumAndData[1], authUserName.getBytes(UTF8), encryptedKey.getChecksumAlgorithm());
				if (!Arrays.equals(checksumAndData[0], checksum)) {
					result = null;
					logger.warn("login: Wrong password for user \"{}\"!", authUserName);
				}
			} catch (BadPaddingException x) {
				logger.warn("login: Caught BadPaddingException indicating a wrong password for user \"{}\"!", authUserName);
				result = null;
			} catch (GeneralSecurityException x) {
				throw new RuntimeException(x);
			}
		}

		// We check only once at the end of this method if we could successfully authenticate and otherwise
		// throw a AuthenticationException. If we threw the AuthenticationException at different locations (even with the same
		// message), and attacker might know from the stack trace (=> line number) whether the user-name
		// or the password was wrong. This information will be logged, but not disclosed in the exception.
		// Marco :-)
		if (result == null)
			throw new AuthenticationException("Unknown user \"" + authUserName + "\" or wrong password!");

		cache_userName2cachedMasterKey.put(authUserName, new CachedMasterKey(authUserName, authPassword, result));
		return result;
	}

	/**
	 * <p>
	 * Split the given plain (decrypted) <code>data</code> byte array into two parts. The first part
	 * is the checksum and the second part the actual plain (decrypted) data.
	 * </p>
	 * <p>
	 * The opposite of this method is {@link #catChecksumAndData(byte[], byte[])} which concats the two parts into one.
	 * </p>
	 *
	 * @param checksumAndData the plain data prepended with the hash. The hash thus always comes before the actual data.
	 * @param checksumSize the number of bytes that are the actual hash.
	 * @return a byte-array-array with two elements. The 1st element is the byte-array containing the checksum, the 2nd element
	 * is the byte-array containing the actual data.
	 * @see #catChecksumAndData(byte[], byte[])
	 */
	private static byte[][] splitChecksumAndData(byte[] checksumAndData, short checksumSize)
	{
		byte[][] result = new byte[2][];
		result[0] = new byte[checksumSize];
		result[1] = new byte[checksumAndData.length - checksumSize];
		System.arraycopy(checksumAndData, 0, result[0], 0, result[0].length);
		System.arraycopy(checksumAndData, result[0].length, result[1], 0, result[1].length);
		return result;
	}

	/**
	 * Concat the two byte-arrays <code>hash</code> and <code>data</code> into one combined byte-array
	 * which contains then first the hash and directly following the data.
	 *
	 * @param checksum the checksum.
	 * @param data the actual data.
	 * @return the combined hash and data.
	 * @see #splitChecksumAndData(byte[], short)
	 */
	private static byte[] catChecksumAndData(byte[] checksum, byte[] data)
	{
		byte[] result = new byte[checksum.length + data.length];
		System.arraycopy(checksum, 0, result, 0, checksum.length);
		System.arraycopy(data, 0, result, checksum.length, data.length);
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
			cipher.init(opmode, masterKey);
		else
			cipher.init(opmode, masterKey, new IvParameterSpec(iv));

		return cipher;
	}

	/**
	 * <p>
	 * Generate a new key and store it to the file.
	 * </p>
	 * <p>
	 * The new key will be generated with the size specified by the
	 * system property {@value #PROPERTY_KEY_SIZE} and encrypted with the
	 * master-key and the encryption-algorithm specified by the
	 * system property {@value #PROPERTY_ENCRYPTION_ALGORITHM}.
	 * </p>
	 *
	 * @param authUserName the authenticated user authorizing this action.
	 * @param authPassword the password for authenticating the user specified by <code>authUserName</code>.
	 * @return the newly created key.
	 * @throws AuthenticationException if the specified <code>authUserName</code> does not exist or the specified <code>authPassword</code>
	 * is not correct for the given <code>authUserName</code>.
	 * @throws IOException if writing to the local file-system failed.
	 */
	public synchronized GeneratedKey generateKey(String authUserName, char[] authPassword)
	throws AuthenticationException, IOException
	{
		long keyID = nextKeyID();
		SecretKey key = getKeyGenerator(getKeySize()).generateKey();
		GeneratedKey generatedKey = new GeneratedKey(keyID, key);
		setKey(authUserName, authPassword, keyID, key);
//		storeToFile(); // setKey(...) already calls storeToFile(...)
		return generatedKey;
	}

	/**
	 * <p>
	 * Create a new user.
	 * </p>
	 * <p>
	 * Before the <code>KeyStore</code> can be used (i.e. before most methods work), this method has to be called
	 * to create the first user. When the first user is created, the internal master-key is generated, which will
	 * then not be changed anymore (double-check that the {@link #PROPERTY_KEY_SIZE key-size} is set correctly at
	 * this time).
	 * </p>
	 *
	 * @param authUserName the authenticated user authorizing this action. If the very first user is created, this value
	 * is ignored and can be <code>null</code>.
	 * @param authPassword the password for authenticating the user specified by <code>authUserName</code>. If the very first user is created, this value
	 * is ignored and can be <code>null</code>.
	 * @param userName the name of the user to be created.
	 * @param password the password of the new user.
	 * @throws AuthenticationException if the specified <code>authUserName</code> does not exist or the specified <code>authPassword</code>
	 * is not correct for the given <code>authUserName</code>.
	 * @throws UserAlreadyExistsException if a user with the name specified by <code>userName</code> already exists.
	 * @throws IOException if writing to the local file-system failed.
	 */
	public synchronized void createUser(String authUserName, char[] authPassword, String userName, char[] password)
	throws AuthenticationException, UserAlreadyExistsException, IOException
	{
		if (userName == null)
			throw new IllegalArgumentException("userName must not be null!");

		if (password == null)
			throw new IllegalArgumentException("password must not be null!");

		MasterKey masterKey;

		if (isEmpty()) {
			Key key = getKeyGenerator(getKeySize()).generateKey();
			byte[] keyBytes = key.getEncoded();
			masterKey = new MasterKey(keyBytes, key.getAlgorithm());
			// Unfortunately, we cannot clear the sensitive data from the key instance, because
			// there is no nice way to do this (we could only do very ugly reflection-based stuff).
			// But fortunately, this happens only the very first time a new, empty KeyStore is created.
			// With an existing KeyStore we won't come here and our MasterKey can [and will] be cleared.
			// Marco :-)
			logger.info("createUser: Created master-key with a size of {} bits. This key will not be modified for this key-store anymore.", keyBytes.length * 8);
		}
		else
			masterKey = getMasterKey(authUserName, authPassword);

		if (user2keyMap.containsKey(userName))
			throw new UserAlreadyExistsException("User '" + userName + "' already exists!");

		setUser(masterKey, userName, password);
	}

	synchronized void setUser(MasterKey masterKey, String userName, char[] password)
	throws IOException
	{
		byte[] plainMasterKeyData = masterKey.getEncoded();
		byte[] hash = checksum(plainMasterKeyData, userName.getBytes(UTF8), CHECKSUM_ALGORITHM_ACTIVE);
		byte[] hashAndData = catChecksumAndData(hash, plainMasterKeyData);

		byte[] salt = new byte[8]; // TODO make salt-length configurable!
		secureRandom.nextBytes(salt);
		try {
			Cipher cipher = getCipherForUserPassword(password, salt, null, null, Cipher.ENCRYPT_MODE);
			byte[] encrypted = cipher.doFinal(hashAndData);

			EncryptedKey encryptedKey = new EncryptedKey(
					encrypted, salt,
					stringConstant(masterKey.getAlgorithm()),
					cipher.getIV(), stringConstant(cipher.getAlgorithm()),
					(short)hash.length, stringConstant(CHECKSUM_ALGORITHM_ACTIVE)
			);
			user2keyMap.put(userName, encryptedKey);
			usersCache = null;
		} catch (GeneralSecurityException e) {
			throw new RuntimeException(e);
		}

		storeToFile();
	}

	private static final String CHECKSUM_ALGORITHM_XOR8 = "XOR8";
	private static final String CHECKSUM_ALGORITHM_SHA1 = "SHA1";
	private static final String CHECKSUM_ALGORITHM_MD5 = "MD5";
	private static final String CHECKSUM_ALGORITHM_CRC32 = "CRC32";

	/**
	 * The one that is used for new entries.
	 */
	private static final String CHECKSUM_ALGORITHM_ACTIVE = CHECKSUM_ALGORITHM_CRC32;

	private MessageDigest checksum_md_sha1; // the checksum(...) method is called only within synchronized blocks - we can thus reuse the MessageDigest.
	private MessageDigest checksum_md_md5;
	private CRC32 checksum_crc32; // the checksum(...) method is called only within synchronized blocks - we can thus reuse the CRC32.

	private byte[] checksum(byte[] data, byte[] salt, String hashAlgorithm)
	{
		if (CHECKSUM_ALGORITHM_CRC32.equals(hashAlgorithm)) {
			if (checksum_crc32 == null)
				checksum_crc32 = new CRC32();
			else
				checksum_crc32.reset();

			checksum_crc32.update(salt);
			checksum_crc32.update(data);
			long crc32Value = checksum_crc32.getValue();
			byte[] result = new byte[4]; // CRC 32 has only 32 bits (= 4 bytes), hence the name
			result[0] = (byte)(crc32Value >>> 3);
			result[1] = (byte)(crc32Value >>> 2);
			result[2] = (byte)(crc32Value >>> 1);
			result[3] = (byte)crc32Value;
			return result;
		}
		else if (CHECKSUM_ALGORITHM_SHA1.equals(hashAlgorithm)) {
			if (checksum_md_sha1 == null) {
				try {
					checksum_md_sha1 = MessageDigest.getInstance("SHA1");
				} catch (NoSuchAlgorithmException e) {
					throw new RuntimeException(e);
				}
			}
			checksum_md_sha1.update(salt);
			checksum_md_sha1.update(data);
			return checksum_md_sha1.digest();
		}
		else if (CHECKSUM_ALGORITHM_MD5.equals(hashAlgorithm)) {
			if (checksum_md_md5 == null) {
				try {
					checksum_md_md5 = MessageDigest.getInstance("MD5");
				} catch (NoSuchAlgorithmException e) {
					throw new RuntimeException(e);
				}
			}
			checksum_md_md5.update(salt);
			checksum_md_md5.update(data);
			return checksum_md_md5.digest();
		}
		else if (CHECKSUM_ALGORITHM_XOR8.equals(hashAlgorithm)) {
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

	synchronized void storeToFile() throws IOException
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

	/**
	 * <p>
	 * Get all users who can authenticate at this <code>KeyStore</code>.
	 * </p>
	 *
	 * @param authUserName the authenticated user authorizing this action.
	 * @param authPassword the password for authenticating the user specified by <code>authUserName</code>.
	 * @return a read-only {@link Set} of all user-names known to this <code>KeyStore</code>. This
	 * <code>Set</code> is an unmodifiable copy of the internally used data and therefore is both thread-safe
	 * and iteration-safe (i.e. it can be iterated while simultaneously users are {@link #deleteUser(String, char[], String) deleted}).
	 * @throws AuthenticationException if the specified <code>authUserName</code> does not exist or the specified <code>authPassword</code>
	 * is not correct for the given <code>authUserName</code>.
	 */
	public synchronized Set<String> getUsers(String authUserName, char[] authPassword)
	throws AuthenticationException
	{
		// The following getMasterKey(...) is no real protection, because the information returned by this method
		// is currently not protected, but this way, we already have the right arguments to later encrypt this
		// information, too - if we ever want to.
		// Marco :-)
		getMasterKey(authUserName, authPassword);

		Set<String> users = usersCache;
		if (users == null) {
			users = Collections.unmodifiableSet(new HashSet<String>(user2keyMap.keySet()));
			usersCache = users;
		}

		return users;
	}

	private Set<String> usersCache = null;

	/**
	 * <p>
	 * Delete the user specified by <code>userName</code>.
	 * </p>
	 * <p>
	 * Deleting the authenticated user himself (i.e. <code>authUserName == userName</code>) is possible,
	 * as long as it is not the last user.
	 * </p>
	 *
	 * @param authUserName the name of the principal, i.e. the user authorizing this operation.
	 * @param authPassword the password of the principal.
	 * @param userName the name of the user to be deleted.
	 * @throws AuthenticationException if the specified <code>authUserName</code> does not exist or the specified <code>authPassword</code>
	 * is not correct for the given <code>authUserName</code>.
	 * @throws UserNotFoundException if there is no user with the name specified by <code>userName</code>.
	 * @throws CannotDeleteLastUserException if the last user would be deleted by this method invocation (thus rendering
	 * the <code>KeyStore</code> unusable and unrecoverable - i.e. totally lost).
	 * @throws IOException if writing to the local file-system failed.
	 */
	public synchronized void deleteUser(String authUserName, char[] authPassword, String userName)
	throws AuthenticationException, UserNotFoundException, CannotDeleteLastUserException, IOException
	{
		// The following getMasterKey(...) is no real protection, because a user can be deleted without
		// authenticating on the file-base (as this doesn't require to decrypt data, currently), but
		// this way, we already have the right arguments here and might later encrypt the required infos.
		// Marco :-)
		getMasterKey(authUserName, authPassword);

		EncryptedKey encryptedKey = user2keyMap.get(userName);
		if (encryptedKey == null)
			throw new UserNotFoundException("The user \"" + userName + "\" does not exist!");

		if (user2keyMap.size() == 1)
			throw new CannotDeleteLastUserException("You cannot delete the last user and \"" + userName + "\" is the last user!");

		clearCache(userName);
		user2keyMap.remove(userName);
		usersCache = null;

		storeToFile();
	}

//	public synchronized void changeMyPassword(String userName, char[] oldPassword, char[] newPassword)
//	throws AuthenticationException, IOException
//	{
//		try {
//			changeUserPassword(userName, oldPassword, userName, newPassword);
//		} catch (UserNotFoundException e) {
//			throw new RuntimeException("How the hell can this happen? The AuthenticationException should have occured in this case!", e);
//		}
//	}

	/**
	 * <p>
	 * Change a user's password.
	 * </p>
	 * <p>
	 * The user identified by <code>userName</code> will have the new password specified by
	 * <code>newPassword</code> immediately after this method. Authenticating this user with
	 * his old password will fail afterwards.
	 * </p>
	 *
	 * @param authUserName the authenticated user authorizing this action.
	 * @param authPassword the password for authenticating the user specified by <code>authUserName</code>.
	 * @param userName the user whose password is to be changed. This can be the same as <code>authUserName</code>.
	 * @param newPassword the new password.
	 * @throws AuthenticationException if the specified <code>authUserName</code> does not exist or the specified <code>authPassword</code>
	 * is not correct for the given <code>authUserName</code>.
	 * @throws UserNotFoundException if there is no user with the name specified by <code>userName</code>.
	 * @throws IOException if writing to the local file-system failed.
	 */
	public synchronized void changeUserPassword(String authUserName, char[] authPassword, String userName, char[] newPassword)
	throws AuthenticationException, UserNotFoundException, IOException
	{
		MasterKey masterKey = getMasterKey(authUserName, authPassword);

		if (!user2keyMap.containsKey(userName))
			throw new UserNotFoundException("User '" + userName + "' does not exist!");

		setUser(masterKey, userName, newPassword);
	}

	/**
	 * Get the key identified by the given <code>keyID</code>.
	 *
	 * @param authUserName the authenticated user authorizing this action.
	 * @param authPassword the password for authenticating the user specified by <code>authUserName</code>.
	 * @param keyID the identifier of the key to get.
	 * @return the key associated with the given identifier; never <code>null</code> (if there is no key for the given <code>keyID</code>,
	 * a {@link KeyNotFoundException} is thrown).
	 * @throws AuthenticationException if the specified <code>authUserName</code> does not exist or the specified <code>authPassword</code>
	 * is not correct for the given <code>authUserName</code>.
	 * @throws KeyNotFoundException if the specified <code>keyID</code> does not reference any existing key. Note, that the
	 * authentication process occurs before any lookup and therefore a {@link KeyNotFoundException} indicates a correct authentication
	 * (otherwise the {@link AuthenticationException} would have been thrown before).
	 */
	public synchronized Key getKey(String authUserName, char[] authPassword, long keyID)
	throws AuthenticationException, KeyNotFoundException
	{
		MasterKey masterKey = getMasterKey(authUserName, authPassword);
		EncryptedKey encryptedKey = keyID2keyMap.get(keyID);
		if (encryptedKey == null)
			throw new KeyNotFoundException("There is no key with keyID=" + keyID + "!");

		try {
			Cipher cipher = getCipherForMasterKey(
					masterKey,
					encryptedKey.getKeyEncryptionIV(),
					encryptedKey.getKeyEncryptionAlgorithm(),
					Cipher.DECRYPT_MODE
			);
			byte[] decrypted = cipher.doFinal(encryptedKey.getData());
			byte[][] checksumAndData = splitChecksumAndData(decrypted, encryptedKey.getChecksumSize());

			byte[] checksum = checksum(checksumAndData[1], authUserName.getBytes(UTF8), encryptedKey.getChecksumAlgorithm());
			if (!Arrays.equals(checksumAndData[0], checksum))
				throw new IllegalStateException("Checksum mismatch!!! This means, the decryption key was wrong!");

			return new SecretKeySpec(checksumAndData[1], encryptedKey.getAlgorithm());
		} catch (GeneralSecurityException e) {
			throw new RuntimeException(e);
		}
	}

	synchronized void setKey(String authUserName, char[] authPassword, long keyID, Key key)
	throws AuthenticationException, IOException
	{
		MasterKey masterKey = getMasterKey(authUserName, authPassword);

		byte[] plainKeyData = key.getEncoded();
		byte[] checksum = checksum(plainKeyData, authUserName.getBytes(UTF8), CHECKSUM_ALGORITHM_ACTIVE);
		byte[] checksumAndData = catChecksumAndData(checksum, plainKeyData);

		try {
			Cipher cipher = getCipherForMasterKey(masterKey, null, null, Cipher.ENCRYPT_MODE);
			byte[] encrypted = cipher.doFinal(checksumAndData);
			EncryptedKey encryptedKey = new EncryptedKey(
					encrypted, null, stringConstant(key.getAlgorithm()),
					cipher.getIV(), stringConstant(cipher.getAlgorithm()),
					(short)checksum.length, stringConstant(CHECKSUM_ALGORITHM_ACTIVE)
			);
			keyID2keyMap.put(keyID, encryptedKey);
		} catch (GeneralSecurityException e) {
			throw new RuntimeException(e);
		}

		storeToFile();
	}

	/**
	 * <p>
	 * Clear all cached data for the specified user name.
	 * </p>
	 * <p>
	 * Every time, a user
	 * calls a method requiring <code>authUserName</code> and <code>authPassword</code>,
	 * either an authentication process happens, or a previously cached authentication
	 * result (i.e. a decrypted master-key) is used. In order to speed things up, authentication results are cached for a
	 * limited time. After this time elapses, the data is cleared by a timer. If a user wants (for security reasons)
	 * remove the cached data from the memory earlier, he can call this method.
	 * </p>
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
			if (cachedMasterKey != null)
				cachedMasterKey.clear();
		}
	}

	@Override
	protected void finalize() throws Throwable {
		clearCache(null);
		super.finalize();
	}

	/**
	 * This method encodes a byte array into a human readable hex string. For each byte,
	 * two hex digits are produced. They are concatted without any separators.
	 * <p>
	 * This is a convenience method for <code>encodeHexStr(buf, 0, buf.length)</code>
	 * <p>
	 * This is copied from project <code>org.nightlabs.util</code>. If we need  more from this
	 * lib, we should add a dependency onto it.
	 *
	 * @param buf The byte array to translate into human readable text.
	 * @return a human readable string like "fa3d70" for a byte array with 3 bytes and these values.
	 * @see #encodeHexStr(byte[], int, int)
	 * @see #decodeHexStr(String)
	 */
	private static String encodeHexStr(byte[] buf)
	{
		return encodeHexStr(buf, 0, buf.length);
	}

	/**
	 * Encode a byte array into a human readable hex string. For each byte,
	 * two hex digits are produced. They are concatted without any separators.
	 * <p>
	 * This is copied from project <code>org.nightlabs.util</code>. If we need  more from this
	 * lib, we should add a dependency onto it.
	 *
	 * @param buf The byte array to translate into human readable text.
	 * @param pos The start position (0-based).
	 * @param len The number of bytes that shall be processed beginning at the position specified by <code>pos</code>.
	 * @return a human readable string like "fa3d70" for a byte array with 3 bytes and these values.
	 * @see #encodeHexStr(byte[])
	 * @see #decodeHexStr(String)
	 */
	private static String encodeHexStr(byte[] buf, int pos, int len)
	{
		 StringBuffer hex = new StringBuffer();
		 while (len-- > 0) {
				byte ch = buf[pos++];
				int d = (ch >> 4) & 0xf;
				hex.append((char)(d >= 10 ? 'a' - 10 + d : '0' + d));
				d = ch & 0xf;
				hex.append((char)(d >= 10 ? 'a' - 10 + d : '0' + d));
		 }
		 return hex.toString();
	}
}
