/*
 * Cumulus4j - Securing your data in the cloud - http://cumulus4j.org
 * Copyright (C) 2011 NightLabs Consulting GmbH
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.cumulus4j.keystore;

import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.lang.ref.WeakReference;
import java.security.GeneralSecurityException;
import java.security.SecureRandom;
import java.security.spec.KeySpec;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.SortedSet;
import java.util.Timer;
import java.util.TimerTask;
import java.util.TreeSet;
import java.util.UUID;

import javax.crypto.SecretKey;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;

import org.bouncycastle.crypto.CryptoException;
import org.bouncycastle.crypto.params.KeyParameter;
import org.bouncycastle.crypto.params.ParametersWithIV;
import org.cumulus4j.crypto.Cipher;
import org.cumulus4j.crypto.CipherOperationMode;
import org.cumulus4j.crypto.CryptoRegistry;
import org.cumulus4j.keystore.prop.LongProperty;
import org.cumulus4j.keystore.prop.Property;
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
 * <a target="_blank" href="http://en.wikipedia.org/wiki/Swap_space">swapping</a>!).
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
 * of 256 bit. This can be controlled, however, by specifying the system property
 * {@value #SYSTEM_PROPERTY_KEY_SIZE} (e.g. passing the argument "-Dcumulus4j.KeyStore.keySize=128"
 * to the <code>java</code> command line will switch to 128-bit-keys).
 * </p>
 * <p>
 * <b>Important:</b> As the master key is generated when the first
 * {@link #createUser(String, char[], String, char[]) user is created} and is then not changed anymore, you must therefore
 * specify the desired key-size already at the moment when you initialise the key store (i.e. create the first user). If
 * you change the key-size later, it will affect only those keys that are created later.
 * </p>
 * <p>
 * Note, that the "Java Cryptography Extension (JCE) Unlimited Strength Jurisdiction Policy Files" does not
 * need to be installed for very strong cryptography, because we don't use the JCE (see {@link Cipher}).
 * </p>
 * <h3>File format of the key store file (version 1)</h3>
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
 *	<tr>
 * 		<td align="right" valign="top">4</td><td valign="top">int: Number of entries in 'Block A' to follow.</td>
 * 	</tr>
 * 	<tr>
 * 		<td colspan="2">
 * 		<table bgcolor="#F0F0F0" border="1" width="100%">
 * 			<tbody>
 * 			<tr><td bgcolor="#D0D0D0" colspan="2"><b>Block A: String constants</b></td></tr>
 * 			<tr>
 * 				<td colspan="2">
 * In order to reduce the file size (and thus increase the write speed), various
 * strings like encryption algorithm, checksum algorithm and the like are not written
 * again and again for every key, but instead only once here. In every key, these
 * Strings are then referenced instead by their position-index (zero-based).
 * 				</td>
 * 			</tr>
 *
 * 			<tr>
 * 				<td align="right" valign="top"><b>Bytes</b></td><td valign="top"><b>Descrition</b></td>
 * 			</tr>
 * 			<tr>
 * 				<td align="right" valign="top">2</td><td valign="top">short <i>len</i>: Number of bytes to follow (written by {@link DataOutputStream#writeUTF(String)}).</td>
 * 			</tr>
 * 			<tr>
 * 				<td align="right" valign="top"><i>len</i></td><td valign="top">String: Constant&apos;s value (written by {@link DataOutputStream#writeUTF(String)}).</td>
 * 			</tr>
 * 			</tbody>
 * 		</table>
 * 		</td>
 * 	</tr>
 *
 *
 * 	<tr>
 * 		<td align="right" valign="top">4</td><td valign="top">int: Number of entries in 'Block B' to follow.</td>
 * 	</tr>
 *  <tr>
 * 		<td colspan="2">
 * 		<table bgcolor="#F0F0F0" border="1" width="100%">
 * 			<tbody>
 * 			<tr><td bgcolor="#D0D0D0" colspan="2"><b>Block B: User-key-map</b></td></tr>
 *
 * 			<tr>
 * 				<td colspan="2">
 * 					For every user, the master-key is stored encrypted with the user's password in this block.
 * 				</td>
 * 			</tr>
 *
 * 			<tr>
 * 				<td align="right" valign="top"><b>Bytes</b></td><td valign="top"><b>Descrition</b></td>
 * 			</tr>
 *
 * 			<tr>
 * 				<td align="right" valign="top">2</td><td valign="top">short <i>len1</i>: User name: Number of bytes to follow (written by {@link DataOutputStream#writeUTF(String)}).</td>
 * 			</tr>
 * 			<tr>
 * 				<td align="right" valign="top"><i>len1</i></td><td valign="top">String: User name (written by {@link DataOutputStream#writeUTF(String)}).</td>
 * 			</tr>
 *
 * 			<tr>
 * 				<td align="right" valign="top">4</td><td valign="top">int: Key size for the password-based key (in bits! i.e. usually 128 or 256).</td>
 * 			</tr>
 * 			<tr>
 * 				<td align="right" valign="top">4</td><td valign="top">int: Iteration count for the password-based key.</td>
 * 			</tr>
 * 			<tr>
 * 				<td align="right" valign="top">4</td><td valign="top">int: Reference to the name of the key-generator-algorithm for creating the password-based key (index in the list of 'Block A').</td>
 * 			</tr>
 *
 * 			<tr>
 * 				<td align="right" valign="top">2</td><td valign="top">UNSIGNED short <i>len2</i>: Salt: Number of bytes to follow (written by {@link KeyStoreUtil#writeByteArrayWithShortLengthHeader(DataOutputStream, byte[])}).</td>
 * 			</tr>
 * 			<tr>
 * 				<td align="right" valign="top"><i>len2</i></td><td valign="top">byte[]: Salt to be used when generating the password-based key (written by {@link KeyStoreUtil#writeByteArrayWithShortLengthHeader(DataOutputStream, byte[])}).</td>
 * 			</tr>
 *
 *			<!-- BEGIN written by {@link AbstractEncryptedData#write(DataOutputStream, Map)} -->
 * 				<tr>
 * 					<td align="right" valign="top">4</td><td valign="top">int: Reference to the name of the encryption algorithm used to encrypt this record's data (index in the list of 'Block A').</td>
 * 				</tr>
 *
 * 				<tr>
 * 					<td align="right" valign="top">2</td><td valign="top">UNSIGNED short <i>lenIV</i>: IV: Number of bytes to follow (written by {@link KeyStoreUtil#writeByteArrayWithShortLengthHeader(DataOutputStream, byte[])}).</td>
 * 				</tr>
 * 				<tr>
 * 					<td align="right" valign="top"><i>lenIV</i></td><td valign="top">byte[]: The actual IV (initialisation vector) used to encrypt the key's data (written by {@link KeyStoreUtil#writeByteArrayWithShortLengthHeader(DataOutputStream, byte[])}).</td>
 * 				</tr>
 *
 * 				<tr>
 * 					<td align="right" valign="top">4</td><td valign="top">int: Reference to the name of the <a target="_blank" href="http://en.wikipedia.org/wiki/Message_authentication_code">MAC</a> algorithm used to authenticate this record's data (index in the list of 'Block A').</td>
 * 				</tr>
 *
 * 				<tr>
 * 					<td align="right" valign="top">2</td><td valign="top">short <i>lenMACKey</i>: MAC key: Number of bytes in the MAC's key.</td>
 * 				</tr>
 * 				<tr>
 * 					<td align="right" valign="top">2</td><td valign="top">short <i>lenMACIV</i>: MAC IV: Number of bytes in the MAC's IV.</td>
 * 				</tr>
 * 				<tr>
 * 					<td align="right" valign="top">2</td><td valign="top">short <i>lenMAC</i>: MAC: Number of bytes in the MAC.</td>
 * 				</tr>
 *
 *				<tr>
 *					<td colspan="2">
 * 						<table bgcolor="#E0E0E0" border="1" width="100%">
 * 							<tbody>
 * 								<tr><td bgcolor="#C0C0C0" colspan="2"><b>ENCRYPTED</b></td></tr>
 * 								<tr>
 * 									<td align="right" valign="top"><i>lenMACKey</i></td><td valign="top">MAC key: The actual MAC's key (random).</td>
 * 								</tr>
 * 								<tr>
 * 									<td align="right" valign="top"><i>lenMACIV</i></td><td valign="top">MAC IV: The actual MAC's IV (random).</td>
 * 								</tr>
 * 								<tr>
 * 									<td align="right" valign="top"><i>all until MAC</i></td><td valign="top">The actual data (payload).</td>
 * 								</tr>
 * 								<tr>
 * 									<td align="right" valign="top"><i>lenMAC</i></td><td valign="top">MAC: The actual MAC.</td>
 * 								</tr>
 * 							</tbody>
 * 						</table>
 * 					</td>
 *				</tr>
 *
 *			<!-- END written by {@link AbstractEncryptedData#write(DataOutputStream, Map)} -->
 *
 *			</tbody>
 * 		</table>
 * 		</td>
 * 	</tr>
 *
 *
 * 	<tr>
 * 		<td align="right" valign="top">4</td><td valign="top">int: Number of entries in 'Block C' to follow.</td>
 * 	</tr>
 * 	<tr>
 * 		<td colspan="2">
 * 		<table bgcolor="#F0F0F0" border="1" width="100%">
 * 			<tbody>
 * 			<tr><td bgcolor="#D0D0D0" colspan="2"><b>Block C: Key-ID-key-map</b></td></tr>
 *
 * 			<tr>
 * 				<td colspan="2">
 * 					This block contains the actual keys. Every key is encrypted with the master-key.
 * 				</td>
 * 			</tr>
 *
 * 			<tr>
 * 				<td align="right" valign="top"><b>Bytes</b></td><td valign="top"><b>Descrition</b></td>
 * 			</tr>
 *
 * 			<tr>
 * 				<td align="right" valign="top">8</td><td valign="top">long: Key identifier.</td>
 * 			</tr>
 *
 *			<!-- BEGIN written by {@link AbstractEncryptedData#write(DataOutputStream, Map)} -->
 * 				<tr>
 * 					<td align="right" valign="top">4</td><td valign="top">int: Reference to the name of the encryption algorithm used to encrypt this record's data (index in the list of 'Block A').</td>
 * 				</tr>
 *
 * 				<tr>
 * 					<td align="right" valign="top">2</td><td valign="top">UNSIGNED short <i>lenIV</i>: IV: Number of bytes to follow (written by {@link KeyStoreUtil#writeByteArrayWithShortLengthHeader(DataOutputStream, byte[])}).</td>
 * 				</tr>
 * 				<tr>
 * 					<td align="right" valign="top"><i>lenIV</i></td><td valign="top">byte[]: The actual IV (initialisation vector) used to encrypt the key's data (written by {@link KeyStoreUtil#writeByteArrayWithShortLengthHeader(DataOutputStream, byte[])}).</td>
 * 				</tr>
 *
 * 				<tr>
 * 					<td align="right" valign="top">4</td><td valign="top">int: Reference to the name of the MAC algorithm used to authenticate this record's data (index in the list of 'Block A').</td>
 * 				</tr>
 *
 * 				<tr>
 * 					<td align="right" valign="top">2</td><td valign="top">short <i>lenMACKey</i>: MAC key: Number of bytes in the MAC's key.</td>
 * 				</tr>
 * 				<tr>
 * 					<td align="right" valign="top">2</td><td valign="top">short <i>lenMACIV</i>: MAC IV: Number of bytes in the MAC's IV.</td>
 * 				</tr>
 * 				<tr>
 * 					<td align="right" valign="top">2</td><td valign="top">short <i>lenMAC</i>: MAC: Number of bytes in the MAC.</td>
 * 				</tr>
 *
 *				<tr>
 *					<td colspan="2">
 * 						<table bgcolor="#E0E0E0" border="1" width="100%">
 * 							<tbody>
 * 								<tr><td bgcolor="#C0C0C0" colspan="2"><b>ENCRYPTED</b></td></tr>
 * 								<tr>
 * 									<td align="right" valign="top"><i>lenMACKey</i></td><td valign="top">MAC key: The actual MAC's key (random).</td>
 * 								</tr>
 * 								<tr>
 * 									<td align="right" valign="top"><i>lenMACIV</i></td><td valign="top">MAC IV: The actual MAC's IV (random).</td>
 * 								</tr>
 * 								<tr>
 * 									<td align="right" valign="top"><i>all until MAC</i></td><td valign="top">The actual data (payload).</td>
 * 								</tr>
 * 								<tr>
 * 									<td align="right" valign="top"><i>lenMAC</i></td><td valign="top">MAC: The actual MAC.</td>
 * 								</tr>
 * 							</tbody>
 * 						</table>
 * 					</td>
 *				</tr>
 *
 *			<!-- END written by {@link AbstractEncryptedData#write(DataOutputStream, Map)} -->
 *
 *			</tbody>
 * 		</table>
 * 		</td>
 * 	</tr>
 *
 *
 *	<tr>
 * 		<td align="right" valign="top">4</td><td valign="top">int: Number of entries in 'Block D' to follow.</td>
 * 	</tr>
 * 	<tr>
 * 		<td colspan="2">
 * 		<table bgcolor="#F0F0F0" border="1" width="100%">
 * 			<tbody>
 * 			<tr><td bgcolor="#D0D0D0" colspan="2"><b>Block D: Properties</b></td></tr>
 * 			<tr>
 * 				<td colspan="2">
 * See {@link Property} for details about what this block is used for.
 * 				</td>
 * 			</tr>
 * 			<tr>
 * 				<td align="right" valign="top"><b>Bytes</b></td><td valign="top"><b>Descrition</b></td>
 * 			</tr>
 *
 * 			<tr>
 * 				<td align="right" valign="top">2</td><td valign="top">short <i>len1</i>: Property name: Number of bytes to follow (written by {@link DataOutputStream#writeUTF(String)}).</td>
 * 			</tr>
 * 			<tr>
 * 				<td align="right" valign="top"><i>len1</i></td><td valign="top">String: Property name (written by {@link DataOutputStream#writeUTF(String)}).</td>
 * 			</tr>
 *
 * 			<tr>
 * 				<td align="right" valign="top">4</td><td valign="top">int: Reference to the fully qualified class name of the {@link Property} (index in the list of 'Block A').</td>
 * 			</tr>
 *
 *			<!-- BEGIN written by {@link AbstractEncryptedData#write(DataOutputStream, Map)} -->
 * 				<tr>
 * 					<td align="right" valign="top">4</td><td valign="top">int: Reference to the name of the encryption algorithm used to encrypt this record's data (index in the list of 'Block A').</td>
 * 				</tr>
 *
 * 				<tr>
 * 					<td align="right" valign="top">2</td><td valign="top">UNSIGNED short <i>lenIV</i>: IV: Number of bytes to follow (written by {@link KeyStoreUtil#writeByteArrayWithShortLengthHeader(DataOutputStream, byte[])}).</td>
 * 				</tr>
 * 				<tr>
 * 					<td align="right" valign="top"><i>lenIV</i></td><td valign="top">byte[]: The actual IV (initialisation vector) used to encrypt the key's data (written by {@link KeyStoreUtil#writeByteArrayWithShortLengthHeader(DataOutputStream, byte[])}).</td>
 * 				</tr>
 *
 * 				<tr>
 * 					<td align="right" valign="top">4</td><td valign="top">int: Reference to the name of the MAC algorithm used to authenticate this record's data (index in the list of 'Block A').</td>
 * 				</tr>
 *
 * 				<tr>
 * 					<td align="right" valign="top">2</td><td valign="top">short <i>lenMACKey</i>: MAC key: Number of bytes in the MAC's key.</td>
 * 				</tr>
 * 				<tr>
 * 					<td align="right" valign="top">2</td><td valign="top">short <i>lenMACIV</i>: MAC IV: Number of bytes in the MAC's IV.</td>
 * 				</tr>
 * 				<tr>
 * 					<td align="right" valign="top">2</td><td valign="top">short <i>lenMAC</i>: MAC: Number of bytes in the MAC.</td>
 * 				</tr>
 *
 *				<tr>
 *					<td colspan="2">
 * 						<table bgcolor="#E0E0E0" border="1" width="100%">
 * 							<tbody>
 * 								<tr><td bgcolor="#C0C0C0" colspan="2"><b>ENCRYPTED</b></td></tr>
 * 								<tr>
 * 									<td align="right" valign="top"><i>lenMACKey</i></td><td valign="top">MAC key: The actual MAC's key (random).</td>
 * 								</tr>
 * 								<tr>
 * 									<td align="right" valign="top"><i>lenMACIV</i></td><td valign="top">MAC IV: The actual MAC's IV (random).</td>
 * 								</tr>
 * 								<tr>
 * 									<td align="right" valign="top"><i>all until MAC</i></td><td valign="top">The actual data (payload).</td>
 * 								</tr>
 * 								<tr>
 * 									<td align="right" valign="top"><i>lenMAC</i></td><td valign="top">MAC: The actual MAC.</td>
 * 								</tr>
 * 							</tbody>
 * 						</table>
 * 					</td>
 *				</tr>
 *
 *			<!-- END written by {@link AbstractEncryptedData#write(DataOutputStream, Map)} -->
 *
 * 			<tr>
 * 				<td align="right" valign="top">20</td><td valign="top">SHA1 checksum over the complete file except for the header "Cumulus4jKeyStore", i.e. from the file version at byte offset 17 (including) till here (excluding).</td>
 * 			</tr>
 *			</tbody>
 * 		</table>
 * 		</td>
 * 	</tr>
 *
 * 	</tbody>
 * </table>
 * </p>
 *
 * @author Marco หงุ่ยตระกูล-Schulze - marco at nightlabs dot de
 */
public class KeyStore
{
	static final Logger logger = LoggerFactory.getLogger(KeyStore.class);

//	private static final BouncyCastleProvider bouncyCastleProvider = new BouncyCastleProvider();
//	static {
//		Security.insertProviderAt(bouncyCastleProvider, 2);
//
//		KeyGenerator kg;
//		try {
//			kg = KeyGenerator.getInstance("AES");
//		} catch (NoSuchAlgorithmException e) {
//			logger.warn("KeyGenerator.getInstance(\"AES\") failed: " + e, e);
//			kg = null;
//		}
//
//		if (kg == null || kg.getProvider() != bouncyCastleProvider)
//			logger.warn("BouncyCastleProvider was NOT registered!!!");
//	}

	/**
	 * <p>
	 * System property to control the size of the keys {@link #generateKey(String, char[]) generated}. This
	 * includes not only the actual keys for the main encryption/decryption (in the database), but also the
	 * master key used to protect the file managed by the <code>KeyStore</code>.
	 * </p>
	 * <p>
	 * By default (if the system property {@value #SYSTEM_PROPERTY_KEY_SIZE} is not specified), keys will have a size of 256 bit.
	 * </p>
	 * <p>
	 * Note, that specifying the system property does not change any old keys - only new keys are generated
	 * with the currently active key size. Therefore, if you want to ensure that the internal master key is
	 * only 128 bit long, you have to make sure that the proper key size is specified when the first
	 * {@link #createUser(String, char[], String, char[]) user is created}!
	 * </p>
	 */
	public static final String SYSTEM_PROPERTY_KEY_SIZE = "cumulus4j.KeyStore" + ".keySize";

	/**
	 * <p>
	 * System property to control the encryption algorithm that is used to encrypt data within the key-store. Whenever a new user is
	 * created or a new key is generated, data has to be encrypted (note that the encryption does not happen directly
	 * before data is written to the file, but already most data in memory is encrypted!).
	 * </p>
	 * <p>
	 * By default (if the system property {@value #SYSTEM_PROPERTY_ENCRYPTION_ALGORITHM} is not specified),
	 * "Twofish/GCM/NoPadding" is used. For example, to switch to "AES/CFB/NoPadding", you'd have
	 * to specify the command line argument "-Dcumulus4j.KeyStore.encryptionAlgorithm=AES/CFB/NoPadding".
	 * </p>
	 * <p>
	 * See <a target="_blank" href="http://cumulus4j.org/${project.version}/documentation/supported-algorithms.html">this document</a>
	 * for further information about what values are supported.
	 * </p>
	 * <p>
	 * <b>Important:</b> The default MAC algorithm is "NONE", which is a very bad choice for most encryption algorithms!
	 * Therefore, you must change the MAC algorithm via the system property {@value #SYSTEM_PROPERTY_MAC_ALGORITHM}
	 * if you change the encryption algorithm!
	 * </p>
	 */
	public static final String SYSTEM_PROPERTY_ENCRYPTION_ALGORITHM = "cumulus4j.KeyStore" + ".encryptionAlgorithm";

	/**
	 * <p>
	 * System property to control the <a target="_blank" href="http://en.wikipedia.org/wiki/Message_authentication_code">MAC</a>
	 * algorithm that is used to protect the data within the key-store against manipulation.
	 * </p>
	 * <p>
	 * Whenever data is encrypted, this MAC algorithm is used to calculate a MAC over the original plain-text-data.
	 * The MAC is then stored together with the plain-text-data within the encrypted area.
	 * When data is decrypted, the MAC is calculated again over the decrypted plain-text-data and compared to the
	 * original MAC in order to make sure (1) that data was correctly decrypted [i.e. the password provided by the user
	 * is correct] and (2) that the data in the key-store was not manipulated by an attacker.
	 * </p>
	 * <p>
	 * The MAC algorithm used during encryption is stored in the encryption-record's meta-data in order
	 * to use the correct algorithm during decryption, no matter what current MAC algorithm is configured.
	 * Therefore, you can safely change this setting at any time - it will affect future encryption
	 * operations, only.
	 * </p>
	 * <p>
	 * Some block cipher modes (e.g. <a target="_blank" href="http://en.wikipedia.org/wiki/Galois/Counter_Mode">GCM</a>) already include authentication
	 * and therefore no MAC is necessary. In this case, you can specify the MAC algorithm {@value #MAC_ALGORITHM_NONE}.
	 * </p>
	 * <p>
	 * <b>Important:</b> If you specify the MAC algorithm "NONE" and use an encryption algorithm without
	 * authentication, the key store will not be able to detect a wrong password and instead return
	 * corrupt data!!! Be VERY careful with the MAC algorithm "NONE"!!!
	 * </p>
	 * <p>
	 * The default value (used when this system property is not specified) is "NONE", because the default
	 * encryption algorithm is "Twofish/GCM/NoPadding", which (due to "GCM") does not require an additional
	 * MAC.
	 * </p>
	 */
	public static final String SYSTEM_PROPERTY_MAC_ALGORITHM = "cumulus4j.KeyStore" + ".macAlgorithm";

	/**
	 * <p>
	 * Constant for deactivating the <a target="_blank" href="http://en.wikipedia.org/wiki/Message_authentication_code">MAC</a>.
	 * </p>
	 * <p>
	 * <b>Important: Deactivating the MAC is dangerous!</b> Choose this value only, if you are absolutely
	 * sure that your {@link #SYSTEM_PROPERTY_ENCRYPTION_ALGORITHM encryption algorithm} already
	 * provides authentication - like <a target="_blank" href="http://en.wikipedia.org/wiki/Galois/Counter_Mode">GCM</a>
	 * does for example.
	 * </p>
	 * @see #SYSTEM_PROPERTY_MAC_ALGORITHM
	 */
	public static final String MAC_ALGORITHM_NONE = "NONE";

	private static final String KEY_STORE_PROPERTY_NAME_NEXT_KEY_ID = "nextKeyID";

	private SecureRandom secureRandom = new SecureRandom();

	private static Timer expireCacheEntryTimer = new Timer(true);

	private TimerTask expireCacheEntryTimerTask = new ExipreCacheEntryTimerTask(this);

	private KeyStoreData keyStoreData = new KeyStoreData();

	private static class ExipreCacheEntryTimerTask extends TimerTask
	{
		private static final Logger logger = LoggerFactory.getLogger(ExipreCacheEntryTimerTask.class);

		private WeakReference<KeyStore> keyStoreRef;

		public ExipreCacheEntryTimerTask(KeyStore keyStore)
		{
			if (keyStore == null)
				throw new IllegalArgumentException("keyStore == null");

			this.keyStoreRef = new WeakReference<KeyStore>(keyStore);
		}

		@Override
		public void run()
		{
			try {
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
			} catch (Throwable x) {
				// The TimerThread is cancelled, if a task throws an exception. Furthermore, they are not logged at all.
				// Since we do not want the TimerThread to die, we catch everything (Throwable - not only Exception) and log
				// it here. IMHO there's nothing better we can do. Marco :-)
				logger.error("run: " + x, x);
			}
		}
	}

	/**
	 * Gets the key-size that is currently configured. Therefore, this method checks, if the
	 * system property {@value #SYSTEM_PROPERTY_KEY_SIZE} has been specified, and if so returns its value.
	 * If not, it falls back to 256.
	 *
	 * @return the current key-size.
	 */
	int getKeySize()
	{
		int ks = keySize;

		if (ks == 0) {
			String keySizePropName = SYSTEM_PROPERTY_KEY_SIZE;
			String keySizePropValue = System.getProperty(keySizePropName);
			if (keySizePropValue == null || keySizePropValue.trim().isEmpty()) {
				ks = 256; // default value, if the property was not defined.
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
			String encryptionAlgorithmPropName = SYSTEM_PROPERTY_ENCRYPTION_ALGORITHM;
			String encryptionAlgorithmPropValue = System.getProperty(encryptionAlgorithmPropName);
			if (encryptionAlgorithmPropValue == null || encryptionAlgorithmPropValue.trim().isEmpty()) {
				ea = "Twofish/GCM/NoPadding"; // default value, if the property was not defined.
//				ea = "Twofish/CBC/PKCS5Padding"; // default value, if the property was not defined.
//				ea = "AES/CBC/PKCS5Padding"; // default value, if the property was not defined.
//				ea = "AES/CFB/NoPadding"; // default value, if the property was not defined.
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


	String getMACAlgorithm()
	{
		String ma = macAlgorithm;

		if (ma == null) {
			String macAlgorithmPropName = SYSTEM_PROPERTY_MAC_ALGORITHM;
			String macAlgorithmPropValue = System.getProperty(macAlgorithmPropName);
			if (macAlgorithmPropValue == null || macAlgorithmPropValue.trim().isEmpty()) {
				ma = MAC_ALGORITHM_NONE; // default value, if the property was not defined.
				logger.info("getMACAlgorithm: System property '{}' is not set. Using default MAC algorithm '{}'.", macAlgorithmPropName, ma);
			}
			else {
				ma = macAlgorithmPropValue.trim();
				logger.info("getMACAlgorithm: System property '{}' is set to '{}'. Using this MAC algorithm.", macAlgorithmPropName, ma);
			}
			macAlgorithm = ma;
		}

		return ma;
	}
	private String macAlgorithm = null;


	byte[] generateKey(int keySize)
	{
		byte[] result = new byte[(keySize + 7) / 8];
		secureRandom.nextBytes(result);
		return result;
	}

	byte[] generateKey()
	{
		return generateKey(getKeySize());
//		return new SecretKeySpec(
//				generateKey(getKeySize()),
//				getBaseAlgorithm(getEncryptionAlgorithm())
//		);
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
		if (in != null) {
			try {
				keyStoreData.readFromStream(in);
			} finally {
				in.close();
			}
		}
		else
			storeToFile(); // create the file (empty) already now, if it does not exist.

		expireCacheEntryTimer.schedule(expireCacheEntryTimerTask, 60000, 60000); // TODO make this configurable
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
		return keyStoreData.user2keyMap.isEmpty();
	}

	synchronized long nextKeyID(String authUserName, char[] authPassword) throws AuthenticationException
	{
		LongProperty property = getProperty(authUserName, authPassword, LongProperty.class, KEY_STORE_PROPERTY_NAME_NEXT_KEY_ID);
		if (property.getValue() == null)
			property.setValue(1L);

		long result = property.getValue();
		property.setValue(result + 1);
		_setProperty(authUserName, authPassword, property);
		return result;
	}

	private Map<String, CachedMasterKey> cache_userName2cachedMasterKey = new HashMap<String, CachedMasterKey>();

	public synchronized int getMasterKeySize(String authUserName, char[] authPassword)
	throws AuthenticationException
	{
		MasterKey masterKey = getMasterKey(authUserName, authPassword);
		return masterKey.getEncoded().length * 8;
	}

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
//		logger.trace("getMasterKey: authUserName={} authPassword={}", authUserName, new String(authPassword));

		CachedMasterKey cachedMasterKey = cache_userName2cachedMasterKey.get(authUserName);
		MasterKey result = cachedMasterKey == null ? null : cachedMasterKey.getMasterKey();
		if (result != null && Arrays.equals(authPassword, cachedMasterKey.getPassword())) {
			cachedMasterKey.updateLastUse();
			return result;
		}
		result = null;

		EncryptedMasterKey encryptedKey = keyStoreData.user2keyMap.get(authUserName);
		if (encryptedKey == null)
			logger.warn("getMasterKey: Unknown userName: {}", authUserName); // NOT throw exception here to not disclose the true reason of the AuthenticationException - see below
		else {
			PlaintextDataAndMAC plaintextDataAndMAC;
			try {
				Cipher cipher = getCipherForUserPassword(
						authPassword,
						encryptedKey.getPasswordBasedKeySize(),
						encryptedKey.getPasswordBasedIterationCount(),
						encryptedKey.getPasswordBasedKeyGeneratorAlgorithm(),
						encryptedKey.getSalt(),
						encryptedKey.getEncryptionIV(), encryptedKey.getEncryptionAlgorithm(),
						CipherOperationMode.DECRYPT
				);
				byte[] decrypted = cipher.doFinal(encryptedKey.getEncryptedData());

				plaintextDataAndMAC = new PlaintextDataAndMAC(decrypted, encryptedKey);
			} catch (CryptoException x) {
				logger.warn("getMasterKey: Caught CryptoException indicating a wrong password for user \"{}\"!", authUserName);
				plaintextDataAndMAC = null;
			} catch (GeneralSecurityException x) {
				throw new RuntimeException(x);
			}

			try {
				if (plaintextDataAndMAC != null && plaintextDataAndMAC.verifyMAC())
					result = new MasterKey(plaintextDataAndMAC.getData());
				else
					logger.warn("getMasterKey: Wrong password for user \"{}\"! MAC verification failed.", authUserName);
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

	private Cipher getCipherForUserPassword(
			char[] password,
			int passwordBasedKeySize, int passwordBasedIterationCount, String passwordBasedKeyGeneratorAlgorithm,
			byte[] salt, byte[] iv, String algorithm, CipherOperationMode opmode) throws GeneralSecurityException
	{
		if (iv == null) {
			if (CipherOperationMode.ENCRYPT != opmode)
				throw new IllegalArgumentException("iv must not be null when decrypting!");
		}
		else {
			if (CipherOperationMode.ENCRYPT == opmode)
				throw new IllegalArgumentException("iv must be null when encrypting!");
		}

		if (algorithm == null) {
			if (CipherOperationMode.ENCRYPT != opmode)
				throw new IllegalArgumentException("algorithm must not be null when decrypting!");

			algorithm = getEncryptionAlgorithm();
		}

		SecretKeyFactory factory = SecretKeyFactory.getInstance(passwordBasedKeyGeneratorAlgorithm);

		KeySpec spec = new PBEKeySpec(password, salt, passwordBasedIterationCount, passwordBasedKeySize);
		SecretKey secretKey = factory.generateSecret(spec);

		Cipher cipher = CryptoRegistry.sharedInstance().createCipher(algorithm);

		if (iv == null) {
			iv = new byte[cipher.getIVSize()];
			secureRandom.nextBytes(iv);
		}

		cipher.init(opmode, new ParametersWithIV(new KeyParameter(secretKey.getEncoded()), iv));

		return cipher;
	}

//	private String getBaseAlgorithm(String algorithm)
//	{
//		int slashIdx = algorithm.indexOf('/');
//		if (slashIdx < 0)
//			return algorithm;
//
//		return algorithm.substring(0, slashIdx);
//	}

	private Cipher getCipherForMasterKey(MasterKey masterKey, byte[] iv, String algorithm, CipherOperationMode opmode) throws GeneralSecurityException
	{
		if (iv == null) {
			if (CipherOperationMode.ENCRYPT != opmode)
				throw new IllegalArgumentException("iv must not be null when decrypting!");
		}
		else {
			if (CipherOperationMode.ENCRYPT == opmode)
				throw new IllegalArgumentException("iv must be null when encrypting!");
		}

		if (algorithm == null) {
			if (CipherOperationMode.ENCRYPT != opmode)
				throw new IllegalArgumentException("algorithm must not be null when decrypting!");

			algorithm = getEncryptionAlgorithm();
		}

		Cipher cipher = CryptoRegistry.sharedInstance().createCipher(algorithm);

		if (iv == null) {
			iv = new byte[cipher.getIVSize()];
			secureRandom.nextBytes(iv);
		}
		cipher.init(opmode, new ParametersWithIV(new KeyParameter(masterKey.getEncoded()), iv));
		return cipher;
	}

	/**
	 * <p>
	 * Generate a new key and store it to the file.
	 * </p>
	 * <p>
	 * The new key will be generated with the size specified by the
	 * system property {@value #SYSTEM_PROPERTY_KEY_SIZE} and encrypted with the
	 * master-key and the encryption-algorithm specified by the
	 * system property {@value #SYSTEM_PROPERTY_ENCRYPTION_ALGORITHM}.
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
		long keyID = nextKeyID(authUserName, authPassword);
		byte[] key = generateKey();
		GeneratedKey generatedKey = new GeneratedKey(keyID, key);
		_setKey(authUserName, authPassword, keyID, key);
		storeToFile();
		return generatedKey;
	}

	/**
	 * <p>
	 * Generate <code>qty</code> new keys and store them to the file.
	 * </p>
	 * <p>
	 * This method behaves like {@link #generateKey(String, char[])} but is much
	 * faster when multiple keys have to be generated (bulk operation).
	 * </p>
	 *
	 * @param authUserName the authenticated user authorizing this action.
	 * @param authPassword the password for authenticating the user specified by <code>authUserName</code>.
	 * @param qty the number of keys to be generated. If 0, the method will do nothing and return
	 * an empty list, if &lt; 0, an {@link IllegalArgumentException} will be thrown.
	 * @return a list of generated keys; never <code>null</code>.
	 * @throws AuthenticationException if the specified <code>authUserName</code> does not exist or the specified <code>authPassword</code>
	 * is not correct for the given <code>authUserName</code>.
	 * @throws IOException if writing to the local file-system failed.
	 */
	public synchronized List<GeneratedKey> generateKeys(String authUserName, char[] authPassword, int qty)
	throws AuthenticationException, IOException
	{
		if (qty < 0)
			throw new IllegalArgumentException("qty < 0");

		List<GeneratedKey> result = new ArrayList<GeneratedKey>(qty);
		for (int i = 0; i < qty; ++i) {
			long keyID = nextKeyID(authUserName, authPassword);
			byte[] key = generateKey();
			GeneratedKey generatedKey = new GeneratedKey(keyID, key);
			_setKey(authUserName, authPassword, keyID, key);
			result.add(generatedKey);
		}
		storeToFile();
		return result;
	}

	/**
	 * <p>
	 * Create a new user.
	 * </p>
	 * <p>
	 * Before the <code>KeyStore</code> can be used (i.e. before most methods work), this method has to be called
	 * to create the first user. When the first user is created, the internal master-key is generated, which will
	 * then not be changed anymore (double-check that the {@link #SYSTEM_PROPERTY_KEY_SIZE key-size} is set correctly at
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
			byte[] key = generateKey();
			masterKey = new MasterKey(key);
			// Unfortunately, we cannot clear the sensitive data from the key instance, because
			// there is no nice way to do this (we could only do very ugly reflection-based stuff).
			// But fortunately, this happens only the very first time a new, empty KeyStore is created.
			// With an existing KeyStore we won't come here and our MasterKey can [and will] be cleared.
			// Marco :-)
			logger.info("createUser: Created master-key with a size of {} bits. This key will not be modified for this key-store anymore.", key.length * 8);
		}
		else
			masterKey = getMasterKey(authUserName, authPassword);

		if (keyStoreData.user2keyMap.containsKey(userName))
			throw new UserAlreadyExistsException("User '" + userName + "' already exists!");

		setUser(masterKey, userName, password);
	}

	synchronized void setUser(MasterKey masterKey, String userName, char[] password)
	throws IOException
	{
		byte[] plainMasterKeyData = masterKey.getEncoded();

		byte[] salt = new byte[8]; // Are 8 bytes salt salty (i.e. secure) enough?
		secureRandom.nextBytes(salt);
		try {
			int passwordBasedKeySize = getKeySize();
			int passwordBasedIterationCount = 1024; // TODO make configurable!
			String passwordBasedKeyGeneratorAlgorithm = "PBKDF2WithHmacSHA1"; // TODO make configurable

			Cipher cipher = getCipherForUserPassword(
					password,
					passwordBasedKeySize,
					passwordBasedIterationCount,
					passwordBasedKeyGeneratorAlgorithm,
					salt, null, null, CipherOperationMode.ENCRYPT
			);

			PlaintextDataAndMAC plaintextDataAndMAC = new PlaintextDataAndMAC(plainMasterKeyData, getMACAlgorithm());
			byte[] encrypted = cipher.doFinal(plaintextDataAndMAC.toByteArray());

			byte[] iv = ((ParametersWithIV)cipher.getParameters()).getIV();

			EncryptedMasterKey encryptedKey = new EncryptedMasterKey(
					keyStoreData,
					userName,
					passwordBasedKeySize,
					passwordBasedIterationCount,
					keyStoreData.stringConstant(passwordBasedKeyGeneratorAlgorithm),
					salt,
					keyStoreData.stringConstant(cipher.getTransformation()),
					iv,
					keyStoreData.stringConstant(plaintextDataAndMAC.getMACAlgorithm()),
					(short)plaintextDataAndMAC.getMACKey().length,
					(short)plaintextDataAndMAC.getMACIV().length,
					(short)plaintextDataAndMAC.getMAC().length, encrypted
			);
			keyStoreData.user2keyMap.put(userName, encryptedKey);
			usersCache = null;
		} catch (CryptoException e) {
			throw new RuntimeException(e);
		} catch (GeneralSecurityException e) {
			throw new RuntimeException(e);
		}

		storeToFile();
	}

	synchronized void storeToFile() throws IOException
	{
		File newKeyStoreFile = getNewKeyStoreFile();
		boolean deleteNewKeyStoreFile = true;
		try {
			OutputStream out = new FileOutputStream(newKeyStoreFile);
			try {
				keyStoreData.writeToStream(out);
			} finally {
				out.close();
			}

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
	public synchronized SortedSet<String> getUsers(String authUserName, char[] authPassword)
	throws AuthenticationException
	{
		// The following getMasterKey(...) is no real protection, because the information returned by this method
		// is currently not protected, but this way, we already have the right arguments to later encrypt this
		// information, too - if we ever want to.
		// Marco :-)
		getMasterKey(authUserName, authPassword);

		SortedSet<String> users = usersCache;
		if (users == null) {
			users = Collections.unmodifiableSortedSet(new TreeSet<String>(keyStoreData.user2keyMap.keySet()));
			usersCache = users;
		}

		return users;
	}

	private SortedSet<String> usersCache = null;

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

		EncryptedMasterKey encryptedKey = keyStoreData.user2keyMap.get(userName);
		if (encryptedKey == null)
			throw new UserNotFoundException("The user \"" + userName + "\" does not exist!");

		if (keyStoreData.user2keyMap.size() == 1)
			throw new CannotDeleteLastUserException("You cannot delete the last user and \"" + userName + "\" is the last user!");

		clearCache(userName);
		keyStoreData.user2keyMap.remove(userName);
		usersCache = null;

		storeToFile();
	}

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

		if (!keyStoreData.user2keyMap.containsKey(userName))
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
	public synchronized byte[] getKey(String authUserName, char[] authPassword, long keyID)
	throws AuthenticationException, KeyNotFoundException
	{
		MasterKey masterKey = getMasterKey(authUserName, authPassword);
		EncryptedKey encryptedKey = keyStoreData.keyID2keyMap.get(keyID);
		if (encryptedKey == null)
			throw new KeyNotFoundException("There is no key with keyID=" + keyID + "!");

		try {
			Cipher cipher = getCipherForMasterKey(
					masterKey,
					encryptedKey.getEncryptionIV(),
					encryptedKey.getEncryptionAlgorithm(),
					CipherOperationMode.DECRYPT
			);
			byte[] decrypted = cipher.doFinal(encryptedKey.getEncryptedData());

			PlaintextDataAndMAC plaintextDataAndMAC = new PlaintextDataAndMAC(decrypted, encryptedKey);
			if (!plaintextDataAndMAC.verifyMAC())
				throw new IllegalStateException("MAC mismatch!!! This means, the decryption key was wrong!");

			return plaintextDataAndMAC.getData();
		} catch (CryptoException e) {
			throw new RuntimeException(e);
		} catch (GeneralSecurityException e) {
			throw new RuntimeException(e);
		}
	}

	public synchronized SortedSet<Long> getKeyIDs(String authUserName, char[] authPassword)
	throws AuthenticationException
	{
		getMasterKey(authUserName, authPassword);
		SortedSet<Long> result = new TreeSet<Long>(keyStoreData.keyID2keyMap.keySet());
		return result;
	}

	private void _setKey(String authUserName, char[] authPassword, long keyID, byte[] key)
	throws AuthenticationException
	{
		MasterKey masterKey = getMasterKey(authUserName, authPassword);

		try {
			PlaintextDataAndMAC plaintextDataAndMAC = new PlaintextDataAndMAC(key, getMACAlgorithm());

			Cipher cipher = getCipherForMasterKey(masterKey, null, null, CipherOperationMode.ENCRYPT);
			byte[] iv = ((ParametersWithIV)cipher.getParameters()).getIV();
			byte[] encrypted = cipher.doFinal(plaintextDataAndMAC.toByteArray());

			EncryptedKey encryptedKey = new EncryptedKey(
					keyStoreData,
					keyID,
					keyStoreData.stringConstant(cipher.getTransformation()),
					iv,
					plaintextDataAndMAC.getMACAlgorithm(),
					(short)plaintextDataAndMAC.getMACKey().length,
					(short)plaintextDataAndMAC.getMACIV().length,
					(short)plaintextDataAndMAC.getMAC().length, encrypted
			);
			keyStoreData.keyID2keyMap.put(keyID, encryptedKey);
		} catch (CryptoException e) {
			throw new RuntimeException(e);
		} catch (GeneralSecurityException e) {
			throw new RuntimeException(e);
		}
	}

	/**
	 * <p>
	 * Get a named property.
	 * </p>
	 * <p>
	 * The <code>KeyStore</code> supports managing arbitrary properties in the form of
	 * name-value-pairs. The names are plain-text, but the values are encrypted.
	 * A property-value can be of any type for which a subclass of
	 * {@link org.cumulus4j.keystore.prop.Property} exists.
	 * </p>
	 * <p>
	 * This method will always return an instance of the given <code>propertyType</code>, no matter,
	 * if the property exists in this <code>KeyStore</code> or not. If the property does not exist,
	 * its {@link Property#getValue() value} will be <code>null</code>.
	 * </p>
	 * <p>
	 * <b>Important:</b> Never directly instantiate a {@link Property}-subclass. Always use this method
	 * as a factory for property instances.
	 * </p>
	 *
	 * @param <P> the type of the property.
	 * @param authUserName the authenticated user authorizing this action.
	 * @param authPassword the password for authenticating the user specified by <code>authUserName</code>.
	 * @param propertyType the type of the property; must not be <code>null</code>. If the property does not yet exist,
	 * every type can be specified. If the property already exists, this type must match the type of the property.
	 * If they do not match, an {@link IllegalArgumentException} is thrown.
	 * @param name the unique name of the property; must not be <code>null</code>.
	 * @return the property; never <code>null</code>. If the property does not yet exist, a new, empty property is returned.
	 * @throws AuthenticationException if the specified <code>authUserName</code> does not exist or the specified <code>authPassword</code>
	 * is not correct for the given <code>authUserName</code>.
	 * @see #setProperty(String, char[], Property)
	 * @see #removeProperty(String, char[], String)
	 */
	public synchronized <P extends Property<?>> P getProperty(String authUserName, char[] authPassword, Class<P> propertyType, String name)
	throws AuthenticationException
	{
		MasterKey masterKey = getMasterKey(authUserName, authPassword);

		if (name == null)
			throw new IllegalArgumentException("name == null");

		EncryptedProperty encryptedProperty = keyStoreData.name2propertyMap.get(name);

		P result;
		try {
			result = propertyType.newInstance();
		} catch (InstantiationException e) {
			throw new RuntimeException(e);
		} catch (IllegalAccessException e) {
			throw new RuntimeException(e);
		}
		result.setName(name);
		result.setXxx(propertyXxx);

		if (encryptedProperty != null) {
			if (!propertyType.equals(encryptedProperty.getType()))
				throw new IllegalArgumentException("propertyType != encryptedProperty.type :: " + propertyType.getClass().getName() + " != " + encryptedProperty.getType().getName());

			try {
				Cipher cipher = getCipherForMasterKey(
						masterKey,
						encryptedProperty.getEncryptionIV(),
						encryptedProperty.getEncryptionAlgorithm(),
						CipherOperationMode.DECRYPT
				);
				byte[] decrypted = cipher.doFinal(encryptedProperty.getEncryptedData());

				PlaintextDataAndMAC plaintextDataAndMAC = new PlaintextDataAndMAC(decrypted, encryptedProperty);
				if (!plaintextDataAndMAC.verifyMAC())
					throw new IllegalStateException("MAC mismatch!!! This means, the decryption key was wrong!");

				result.setValueEncoded(plaintextDataAndMAC.getData());
			} catch (CryptoException e) {
				throw new RuntimeException(e);
			} catch (GeneralSecurityException e) {
				throw new RuntimeException(e);
			}
		}

		return result;
	}

	public synchronized SortedSet<Property<?>> getProperties(String authUserName, char[] authPassword)
	throws AuthenticationException
	{
		SortedSet<Property<?>> result = new TreeSet<Property<?>>();
		for (Map.Entry<String, EncryptedProperty> me : keyStoreData.name2propertyMap.entrySet()) {
			Property<?> property = getProperty(authUserName, authPassword, me.getValue().getType(), me.getKey());
			result.add(property);
		}
		return result;
	}

	/**
	 * <p>
	 * Remove a property.
	 * </p>
	 * <p>
	 * If the property with the given name does not exist, this method won't do anything.
	 * </p>
	 *
	 * @param authUserName the authenticated user authorizing this action.
	 * @param authPassword the password for authenticating the user specified by <code>authUserName</code>.
	 * @param name the unique name of the property; must not be <code>null</code>.
	 * @return whether the property was removed, i.e. whether this <code>KeyStore</code> was changed by the operation.
	 * @throws AuthenticationException if the specified <code>authUserName</code> does not exist or the specified <code>authPassword</code>
	 * is not correct for the given <code>authUserName</code>.
	 * @throws IOException if writing to the local file-system failed.
	 * @see #getProperty(String, char[], Class, String)
	 * @see #setProperty(String, char[], Property)
	 */
	public synchronized boolean removeProperty(String authUserName, char[] authPassword, String name)
	throws AuthenticationException, IOException
	{
		boolean removed = _removeProperty(authUserName, authPassword, name);

		if (removed)
			storeToFile();

		return removed;
	}

	boolean _removeProperty(String authUserName, char[] authPassword, String name)
	throws AuthenticationException
	{
		getMasterKey(authUserName, authPassword);
		return keyStoreData.name2propertyMap.remove(name) != null;
	}

	private UUID propertyXxx = UUID.randomUUID();

	/**
	 * <p>
	 * Set a property.
	 * </p>
	 * <p>
	 * If the property's {@link Property#getValue() value} is <code>null</code>, the property is
	 * {@link #removeProperty(String, char[], String) removed} instead.
	 * </p>
	 * <p>
	 * If a property with the same {@link Property#getName() name} already exists, it is overwritten.
	 * </p>
	 * <p>
	 * The property's value is encrypted with the internal master-key. The property's name is stored
	 * in plain (unencrypted) form.
	 * </p>
	 *
	 * @param authUserName the authenticated user authorizing this action.
	 * @param authPassword the password for authenticating the user specified by <code>authUserName</code>.
	 * @param property the property to set. Do not instantiate any property directly!
	 * Use {@link #getProperty(String, char[], Class, String)} instead!
	 * @throws AuthenticationException if the specified <code>authUserName</code> does not exist or the specified <code>authPassword</code>
	 * is not correct for the given <code>authUserName</code>.
	 * @throws IOException if writing to the local file-system failed.
	 * @see #getProperty(String, char[], Class, String)
	 * @see #removeProperty(String, char[], String)
	 */
	public synchronized void setProperty(String authUserName, char[] authPassword, Property<?> property)
	throws AuthenticationException, IOException
	{
		_setProperty(authUserName, authPassword, property);
		storeToFile();
	}

	private void _setProperty(String authUserName, char[] authPassword, Property<?> property)
	throws AuthenticationException
	{
		MasterKey masterKey = getMasterKey(authUserName, authPassword);

		if (property == null)
			throw new IllegalArgumentException("property == null");

		if (!propertyXxx.equals(property.getXxx()))
			throw new IllegalArgumentException("property was not created by this KeyStore! You should use 'getProperty(...)' instead of 'new SomeProperty(...)'!!! And you should never store properties unencrypted somewhere outside!");

		if (property.getName() == null)
			throw new IllegalArgumentException("property.name == null");

		keyStoreData.stringConstant(property.getClass().getName());

		byte[] plainValueEncoded = property.getValueEncoded();
		if (plainValueEncoded == null) {
			_removeProperty(authUserName, authPassword, property.getName());
		}
		else {
			try {
				PlaintextDataAndMAC plaintextDataAndMAC = new PlaintextDataAndMAC(plainValueEncoded, getMACAlgorithm());

				Cipher cipher = getCipherForMasterKey(masterKey, null, null, CipherOperationMode.ENCRYPT);
				byte[] encrypted = cipher.doFinal(plaintextDataAndMAC.toByteArray());
				byte[] iv = ((ParametersWithIV)cipher.getParameters()).getIV();

				@SuppressWarnings("unchecked")
				Class<? extends Property<?>> propertyType = (Class<? extends Property<?>>) property.getClass();
				EncryptedProperty encryptedProperty = new EncryptedProperty(
						keyStoreData, property.getName(),
						propertyType,
						keyStoreData.stringConstant(cipher.getTransformation()),
						iv,
						plaintextDataAndMAC.getMACAlgorithm(),
						(short)plaintextDataAndMAC.getMACKey().length,
						(short)plaintextDataAndMAC.getMACIV().length,
						(short)plaintextDataAndMAC.getMAC().length, encrypted
				);
				keyStoreData.name2propertyMap.put(encryptedProperty.getName(), encryptedProperty);
			} catch (CryptoException e) {
				throw new RuntimeException(e);
			} catch (GeneralSecurityException e) {
				throw new RuntimeException(e);
			}
		}
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
}
