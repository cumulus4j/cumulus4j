package org.cumulus4j.crypto;

import java.lang.reflect.Constructor;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

import javax.crypto.NoSuchPaddingException;

import org.bouncycastle.crypto.AsymmetricBlockCipher;
import org.bouncycastle.crypto.BlockCipher;
import org.bouncycastle.crypto.BufferedAsymmetricBlockCipher;
import org.bouncycastle.crypto.BufferedBlockCipher;
import org.bouncycastle.crypto.StreamCipher;
import org.bouncycastle.crypto.encodings.ISO9796d1Encoding;
import org.bouncycastle.crypto.encodings.OAEPEncoding;
import org.bouncycastle.crypto.encodings.PKCS1Encoding;
import org.bouncycastle.crypto.engines.AESEngine;
import org.bouncycastle.crypto.engines.AESFastEngine;
import org.bouncycastle.crypto.engines.AESLightEngine;
import org.bouncycastle.crypto.engines.BlowfishEngine;
import org.bouncycastle.crypto.engines.CAST5Engine;
import org.bouncycastle.crypto.engines.CAST6Engine;
import org.bouncycastle.crypto.engines.CamelliaEngine;
import org.bouncycastle.crypto.engines.CamelliaLightEngine;
import org.bouncycastle.crypto.engines.DESEngine;
import org.bouncycastle.crypto.engines.DESedeEngine;
import org.bouncycastle.crypto.engines.ElGamalEngine;
import org.bouncycastle.crypto.engines.GOST28147Engine;
import org.bouncycastle.crypto.engines.Grain128Engine;
import org.bouncycastle.crypto.engines.Grainv1Engine;
import org.bouncycastle.crypto.engines.HC128Engine;
import org.bouncycastle.crypto.engines.HC256Engine;
import org.bouncycastle.crypto.engines.ISAACEngine;
import org.bouncycastle.crypto.engines.NaccacheSternEngine;
import org.bouncycastle.crypto.engines.NoekeonEngine;
import org.bouncycastle.crypto.engines.NullEngine;
import org.bouncycastle.crypto.engines.RC2Engine;
import org.bouncycastle.crypto.engines.RC4Engine;
import org.bouncycastle.crypto.engines.RC532Engine;
import org.bouncycastle.crypto.engines.RC564Engine;
import org.bouncycastle.crypto.engines.RC6Engine;
import org.bouncycastle.crypto.engines.RSABlindedEngine;
import org.bouncycastle.crypto.engines.RijndaelEngine;
import org.bouncycastle.crypto.engines.SEEDEngine;
import org.bouncycastle.crypto.engines.Salsa20Engine;
import org.bouncycastle.crypto.engines.SerpentEngine;
import org.bouncycastle.crypto.engines.SkipjackEngine;
import org.bouncycastle.crypto.engines.TEAEngine;
import org.bouncycastle.crypto.engines.TwofishEngine;
import org.bouncycastle.crypto.engines.XTEAEngine;
import org.bouncycastle.crypto.modes.AEADBlockCipher;
import org.bouncycastle.crypto.modes.CBCBlockCipher;
import org.bouncycastle.crypto.modes.CCMBlockCipher;
import org.bouncycastle.crypto.modes.CTSBlockCipher;
import org.bouncycastle.crypto.modes.EAXBlockCipher;
import org.bouncycastle.crypto.modes.GCMBlockCipher;
import org.bouncycastle.crypto.modes.GOFBBlockCipher;
import org.bouncycastle.crypto.modes.SICBlockCipher;
import org.bouncycastle.crypto.paddings.BlockCipherPadding;
import org.bouncycastle.crypto.paddings.ISO10126d2Padding;
import org.bouncycastle.crypto.paddings.ISO7816d4Padding;
import org.bouncycastle.crypto.paddings.PKCS7Padding;
import org.bouncycastle.crypto.paddings.PaddedBufferedBlockCipher;
import org.bouncycastle.crypto.paddings.TBCPadding;
import org.bouncycastle.crypto.paddings.X923Padding;
import org.bouncycastle.crypto.paddings.ZeroBytePadding;
import org.cumulus4j.crypto.mode.C4jCFBBlockCipher;
import org.cumulus4j.crypto.mode.C4jOFBBlockCipher;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Registry for obtaining a {@link Cipher} instance by a <code>String</code> name.
 *
 * @author Marco หงุ่ยตระกูล-Schulze - marco at nightlabs dot de
 */
public final class CipherRegistry
{
	private static final Logger logger = LoggerFactory.getLogger(CipherRegistry.class);
	private static CipherRegistry sharedInstance = new CipherRegistry();

	/**
	 * Get the shared instance of this registry.
	 * @return the shared instance.
	 */
	public static CipherRegistry sharedInstance()
	{
		return sharedInstance;
	}

	//////////////////// BEGIN cipher engines ////////////////////
	private Map<String, Class<? extends AsymmetricBlockCipher>> algorithmName2asymmetricBlockCipherEngineClass = new HashMap<String, Class<? extends AsymmetricBlockCipher>>();
	private Map<String, Class<? extends BlockCipher>> algorithmName2blockCipherEngineClass = new HashMap<String, Class<? extends BlockCipher>>();
	private Map<String, Class<? extends StreamCipher>> algorithmName2streamCipherEngineClass = new HashMap<String, Class<? extends StreamCipher>>();

	private void registerBlockCipherEngineClass(Class<? extends BlockCipher> engineClass)
	{
		String algorithmName;
		try {
			BlockCipher engine = engineClass.newInstance();
			algorithmName = engine.getAlgorithmName();
		} catch (InstantiationException e) {
			throw new RuntimeException(e);
		} catch (IllegalAccessException e) {
			throw new RuntimeException(e);
		}
		_registerBlockCipherEngineClass(algorithmName, engineClass);
	}
	private void registerBlockCipherEngineClass(String algorithmName, Class<? extends BlockCipher> engineClass)
	{
		try {
			engineClass.newInstance(); // for testing, if the default constructor can be used, only - instance is not used
		} catch (InstantiationException e) {
			throw new RuntimeException(e);
		} catch (IllegalAccessException e) {
			throw new RuntimeException(e);
		}
		_registerBlockCipherEngineClass(algorithmName, engineClass);
	}

	private void _registerBlockCipherEngineClass(String algorithmName, Class<? extends BlockCipher> engineClass)
	{
			logger.debug("registerSymmetricEngineClass: algorithmName=\"{}\" engineClass=\"{}\"", algorithmName, engineClass.getName());
			algorithmName2blockCipherEngineClass.put(algorithmName.toUpperCase(Locale.ENGLISH), engineClass);
	}

	private void registerAsymmetricBlockCipherEngineClass(String algorithmName, Class<? extends AsymmetricBlockCipher> engineClass)
	{
		try {
			engineClass.newInstance(); // for testing, if the default constructor can be used, only - instance is not used
			logger.debug("registerAsymmetricEngineClass: algorithmName=\"{}\" engineClass=\"{}\"", algorithmName, engineClass.getName());
			algorithmName2asymmetricBlockCipherEngineClass.put(algorithmName.toUpperCase(Locale.ENGLISH), engineClass);
		} catch (InstantiationException e) {
			throw new RuntimeException(e);
		} catch (IllegalAccessException e) {
			throw new RuntimeException(e);
		}
	}

	private void registerStreamCipherEngineClass(Class<? extends StreamCipher> engineClass)
	{
		try {
			StreamCipher engine = engineClass.newInstance();
			String algorithmName = engine.getAlgorithmName();
			logger.debug("registerSymmetricEngineClass: algorithmName=\"{}\" engineClass=\"{}\"", algorithmName, engineClass.getName());
			algorithmName2streamCipherEngineClass.put(algorithmName.toUpperCase(Locale.ENGLISH), engineClass);
		} catch (InstantiationException e) {
			throw new RuntimeException(e);
		} catch (IllegalAccessException e) {
			throw new RuntimeException(e);
		}
	}
	//////////////////// END cipher engines ////////////////////

	//////////////////// BEGIN block cipher modes ////////////////////
	private Map<String, Class<? extends BlockCipher>> modeName2blockCipherModeClass = new HashMap<String, Class<? extends BlockCipher>>();
	private Map<String, Class<? extends BufferedBlockCipher>> modeName2bufferedBlockCipherModeClass = new HashMap<String, Class<? extends BufferedBlockCipher>>();
	private Map<String, Class<? extends AEADBlockCipher>> modeName2aeadBlockCipherModeClass = new HashMap<String, Class<? extends AEADBlockCipher>>();

	private void registerBlockCipherMode(String modeName, Class<? extends BlockCipher> modeClass)
	{
		logger.debug("registerBlockCipherMode: modeName=\"{}\" modeClass=\"{}\"", modeName, modeClass.getName());
		modeName2blockCipherModeClass.put(modeName.toUpperCase(Locale.ENGLISH), modeClass);
	}
	private void registerBufferedBlockCipherMode(String modeName, Class<? extends BufferedBlockCipher> modeClass)
	{
		logger.debug("registerBufferedBlockCipherMode: modeName=\"{}\" modeClass=\"{}\"", modeName, modeClass.getName());
		modeName2bufferedBlockCipherModeClass.put(modeName.toUpperCase(Locale.ENGLISH), modeClass);
	}
	private void registerAEADBlockCipherMode(String modeName, Class<? extends AEADBlockCipher> modeClass)
	{
		logger.debug("registerAEADBlockCipherMode: modeName=\"{}\" modeClass=\"{}\"", modeName, modeClass.getName());
		modeName2aeadBlockCipherModeClass.put(modeName.toUpperCase(Locale.ENGLISH), modeClass);
	}
	//////////////////// END block cipher modes ////////////////////


	//////////////////// BEGIN block cipher paddings ////////////////////
	private Map<String, Class<? extends BlockCipherPadding>> paddingName2blockCipherPaddingClass = new HashMap<String, Class<? extends BlockCipherPadding>>();
	private void registerBlockCipherPadding(Class<? extends BlockCipherPadding> paddingClass)
	{
		String paddingName;
		try {
			BlockCipherPadding padding = paddingClass.newInstance();
			paddingName = padding.getPaddingName();
		} catch (InstantiationException e) {
			throw new RuntimeException(e);
		} catch (IllegalAccessException e) {
			throw new RuntimeException(e);
		}
		logger.debug("registerBlockCipherPadding: paddingName=\"{}\" paddingClass=\"{}\"", paddingName, paddingClass.getName());
		paddingName2blockCipherPaddingClass.put(paddingName.toUpperCase(Locale.ENGLISH), paddingClass);
		paddingName2blockCipherPaddingClass.put((paddingName + "Padding").toUpperCase(Locale.ENGLISH), paddingClass);
	}
	private void registerBlockCipherPadding(String paddingName, Class<? extends BlockCipherPadding> paddingClass)
	{
		try {
			paddingClass.newInstance(); // for testing to be sure there is a default constructor and we can call it.
		} catch (InstantiationException e) {
			throw new RuntimeException(e);
		} catch (IllegalAccessException e) {
			throw new RuntimeException(e);
		}
		logger.debug("registerBlockCipherPadding: paddingName=\"{}\" paddingClass=\"{}\"", paddingName, paddingClass.getName());
		paddingName2blockCipherPaddingClass.put(paddingName.toUpperCase(Locale.ENGLISH), paddingClass);
		paddingName2blockCipherPaddingClass.put((paddingName + "Padding").toUpperCase(Locale.ENGLISH), paddingClass);
	}
	//////////////////// END block cipher paddings ////////////////////

	//////////////////// BEGIN asymmetric paddings ////////////////////
	private Map<String, Class<? extends AsymmetricBlockCipher>> paddingName2asymmetricBlockCipherPaddingClass = new HashMap<String, Class<? extends AsymmetricBlockCipher>>();
	private void registerAsymmetricBlockCipherPadding(String paddingName, Class<? extends AsymmetricBlockCipher> paddingClass)
	{
		logger.debug("registerAsymmetricBlockCipherPadding: paddingName=\"{}\" paddingClass=\"{}\"", paddingName, paddingClass.getName());
		paddingName2asymmetricBlockCipherPaddingClass.put(paddingName.toUpperCase(Locale.ENGLISH), paddingClass);
		paddingName2asymmetricBlockCipherPaddingClass.put((paddingName + "Padding").toUpperCase(Locale.ENGLISH), paddingClass);
	}
	//////////////////// END asymmetric paddings ////////////////////


	private CipherRegistry() {
		// *** BEGIN AsymmetricBlockCipher engines ***
		registerAsymmetricBlockCipherEngineClass("ElGamal", ElGamalEngine.class);
		registerAsymmetricBlockCipherEngineClass("NaccacheStern", NaccacheSternEngine.class);

		// According to the JCERSACipher class, the RSABlindedEngine is used for RSA in the JCE, thus commenting out the other two.
		registerAsymmetricBlockCipherEngineClass("RSA", RSABlindedEngine.class);
//		registerAsymmetricBlockCipherEngineClass("RSA", RSABlindingEngine.class);
//		registerAsymmetricBlockCipherEngineClass("RSA", RSAEngine.class);
		// *** END AsymmetricBlockCipher engines ***

		// *** BEGIN BlockCipher engines ***
		registerBlockCipherEngineClass(AESEngine.class);
		// We register the other two AES implementations under alternative names.
		registerBlockCipherEngineClass("AES.fast", AESFastEngine.class);
		registerBlockCipherEngineClass("AES.light", AESLightEngine.class);

		registerBlockCipherEngineClass(BlowfishEngine.class);
		registerBlockCipherEngineClass(CamelliaEngine.class);
		// Registering the alternative implementation under an alternative name.
		registerBlockCipherEngineClass("Camellia.light", CamelliaLightEngine.class);

		registerBlockCipherEngineClass(CAST5Engine.class);
		registerBlockCipherEngineClass(CAST6Engine.class);
		registerBlockCipherEngineClass(DESedeEngine.class);
		registerBlockCipherEngineClass(DESEngine.class);
		registerBlockCipherEngineClass(GOST28147Engine.class);
		// IDEA is only in the "ext" BouncyCastle lib - not in the normal one. I think it's not needed, anyway. ...at least for now...
//		registerBlockCipherEngineClass(IDEAEngine.class);
		registerBlockCipherEngineClass(NoekeonEngine.class);
		registerBlockCipherEngineClass(NullEngine.class);
		registerBlockCipherEngineClass(RC2Engine.class);
		registerBlockCipherEngineClass(RC532Engine.class);
		registerBlockCipherEngineClass(RC564Engine.class);
		registerBlockCipherEngineClass(RC6Engine.class);
		registerBlockCipherEngineClass(RijndaelEngine.class);
		registerBlockCipherEngineClass(SEEDEngine.class);
		registerBlockCipherEngineClass(SerpentEngine.class);
		registerBlockCipherEngineClass(SkipjackEngine.class);
		registerBlockCipherEngineClass(TEAEngine.class);
		registerBlockCipherEngineClass(TwofishEngine.class);
//		registerSymmetricEngineClass(VMPCEngine.class);
//		registerSymmetricEngineClass(VMPCKSA3Engine.class);
		registerBlockCipherEngineClass(XTEAEngine.class);
		// *** END BlockCipher engines ***


		// *** BEGIN StreamCipher engines ***
		registerStreamCipherEngineClass(Grain128Engine.class);
		registerStreamCipherEngineClass(Grainv1Engine.class);
		registerStreamCipherEngineClass(HC128Engine.class);
		registerStreamCipherEngineClass(HC256Engine.class);
		registerStreamCipherEngineClass(ISAACEngine.class);
		registerStreamCipherEngineClass(RC4Engine.class);
		registerStreamCipherEngineClass(Salsa20Engine.class);
		// *** END StreamCipher engines ***


// *** Wrap engines ***
//		register___(AESWrapEngine.class);
//		register___(CamelliaWrapEngine.class);
//		register___(DESedeWrapEngine.class);
//		register___(RC2WrapEngine.class);
//		register___(RFC3211WrapEngine.class);
//		register___(RFC3394WrapEngine.class);
//		register___(SEEDWrapEngine.class);

		// *** Other stuff ***
//		register___(IESEngine.class);



		// *** BEGIN block cipher modes ***
		registerBlockCipherMode("CBC", CBCBlockCipher.class);
		registerAEADBlockCipherMode("CCM", CCMBlockCipher.class);

		registerBlockCipherMode("CFB", C4jCFBBlockCipher.class);
		for (int i = 1; i <= 32; ++i)
			registerBlockCipherMode("CFB" + (i * 8), C4jCFBBlockCipher.class);

		registerBufferedBlockCipherMode("CTS", CTSBlockCipher.class);
		registerAEADBlockCipherMode("EAX", EAXBlockCipher.class);
		registerAEADBlockCipherMode("GCM", GCMBlockCipher.class);
		registerBlockCipherMode("GOFB", GOFBBlockCipher.class);

		registerBlockCipherMode("OFB", C4jOFBBlockCipher.class);
		for (int i = 1; i <= 32; ++i)
			registerBlockCipherMode("OFB" + (i * 8), C4jOFBBlockCipher.class);

//		registerBlockCipherMode("OpenPGPCFB", OpenPGPCFBBlockCipher.class);
//		registerBlockCipherMode("PGPCFB", PGPCFBBlockCipher.class);
		registerBlockCipherMode("SIC", SICBlockCipher.class);

		// Test all registered BlockCipherModes - MUST BE HERE AFTER THEIR REGISTRATION
		testBlockCipherModes();
		// *** END block cipher modes ***

		// *** BEGIN block cipher paddings ***
		registerBlockCipherPadding(ISO10126d2Padding.class);
		registerBlockCipherPadding("ISO10126", ISO10126d2Padding.class);
		registerBlockCipherPadding(ISO7816d4Padding.class);
		registerBlockCipherPadding(PKCS7Padding.class);
		registerBlockCipherPadding("PKCS5", PKCS7Padding.class);
		registerBlockCipherPadding(TBCPadding.class);
		registerBlockCipherPadding(X923Padding.class);
		registerBlockCipherPadding(ZeroBytePadding.class);
		// *** END block cipher paddings ***


		// *** BEGIN asymmetric paddings ***
		registerAsymmetricBlockCipherPadding("ISO9796-1", ISO9796d1Encoding.class);
		registerAsymmetricBlockCipherPadding("OAEP", OAEPEncoding.class);
		registerAsymmetricBlockCipherPadding("PKCS1", PKCS1Encoding.class);

		// Test all registered asymmetric paddings - MUST BE HERE AFTER THEIR REGISTRATION
		testAsymmetricBlockCipherPaddings();
		// *** END asymmetric paddings ***
	}

	private void testAsymmetricBlockCipherPaddings()
	{
		AsymmetricBlockCipher engine = createAsymmetricBlockCipherEngine("RSA");
		if (engine == null)
			throw new IllegalStateException("No engine!");

		for (String paddingName : paddingName2asymmetricBlockCipherPaddingClass.keySet())
			createAsymmetricBlockCipherPadding(paddingName, engine);
	}

	private void testBlockCipherModes()
	{
		BlockCipher engine8 = createBlockCipherEngine("Blowfish".toUpperCase(Locale.ENGLISH));
		if (engine8 == null)
			throw new IllegalStateException("No 'Blowfish' engine!");

		BlockCipher engine16 = createBlockCipherEngine("AES".toUpperCase(Locale.ENGLISH));
		if (engine16 == null)
			throw new IllegalStateException("No 'AES' engine!");

		for (String modeName : modeName2blockCipherModeClass.keySet())
			createBlockCipherMode(modeName, engine8);

		for (String modeName : modeName2bufferedBlockCipherModeClass.keySet())
			createBufferedBlockCipherMode(modeName, engine8);

		for (String modeName : modeName2aeadBlockCipherModeClass.keySet())
			createAEADBlockCipherMode(modeName, engine16); // Most of these modes require a block-size of 16!
	}

	private <T> T newInstance(Class<T> clazz)
	{
		try {
			return clazz.newInstance();
		} catch (InstantiationException e) {
			throw new RuntimeException(e);
		} catch (IllegalAccessException e) {
			throw new RuntimeException(e);
		}
	}

	/**
	 * @param algorithmName the simple encryption algorithm name (e.g. "AES" or "Twofish") and <b>not</b> the complete transformation.
	 * @return
	 */
	private BlockCipher createBlockCipherEngine(String algorithmName)
	{
		Class<? extends BlockCipher> engineClass = algorithmName2blockCipherEngineClass.get(algorithmName);
		if (engineClass == null)
			return null;

		return newInstance(engineClass);
	}

	private AsymmetricBlockCipher createAsymmetricBlockCipherEngine(String algorithmName)
	{
		Class<? extends AsymmetricBlockCipher> engineClass = algorithmName2asymmetricBlockCipherEngineClass.get(algorithmName);
		if (engineClass == null)
			return null;

		return newInstance(engineClass);
	}

	private StreamCipher createStreamCipherEngine(String algorithmName)
	throws NoSuchAlgorithmException
	{
		Class<? extends StreamCipher> engineClass = algorithmName2streamCipherEngineClass.get(algorithmName);
		if (engineClass == null)
			return null;

		return newInstance(engineClass);
	}

	private BlockCipher createBlockCipherMode(String modeName, BlockCipher engine)
	{
		Class<? extends BlockCipher> modeClass = modeName2blockCipherModeClass.get(modeName);
		if (modeClass == null)
			return null;

		try {
			Constructor<? extends BlockCipher> c = modeClass.getConstructor(BlockCipher.class, String.class);
			return c.newInstance(engine, modeName);
		} catch (NoSuchMethodException x) {
			silentlyIgnore(); // We'll try it with the constructor without mode.
		} catch (Exception e) {
			throw new RuntimeException(e);
		}

		try {
			Constructor<? extends BlockCipher> c = modeClass.getConstructor(BlockCipher.class);
			return c.newInstance(engine);
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	private static final void silentlyIgnore() { }

	private AEADBlockCipher createAEADBlockCipherMode(String modeName, BlockCipher engine)
	{
		Class<? extends AEADBlockCipher> modeClass = modeName2aeadBlockCipherModeClass.get(modeName);
		if (modeClass == null)
			return null;

		try {
			Constructor<? extends AEADBlockCipher> c = modeClass.getConstructor(BlockCipher.class);
			return c.newInstance(engine);
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	private BufferedBlockCipher createBufferedBlockCipherMode(String modeName, BlockCipher engine)
	{
		Class<? extends BufferedBlockCipher> modeClass = modeName2bufferedBlockCipherModeClass.get(modeName);
		if (modeClass == null)
			return null;

		try {
			Constructor<? extends BufferedBlockCipher> c = modeClass.getConstructor(BlockCipher.class);
			return c.newInstance(engine);
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	private BlockCipherPadding createBlockCipherPadding(String paddingName)
	{
		Class<? extends BlockCipherPadding> paddingClass = paddingName2blockCipherPaddingClass.get(paddingName);
		if (paddingClass == null)
			return null;

		try {
			return paddingClass.newInstance();
		} catch (InstantiationException e) {
			throw new RuntimeException(e);
		} catch (IllegalAccessException e) {
			throw new RuntimeException(e);
		}
	}

	private Cipher createCipherForBlockCipherMode(String transformation, BlockCipher modeWithEngine, String engineName, String modeName, String paddingName)
	throws NoSuchPaddingException
	{
		if (paddingName.isEmpty() || "NOPADDING".equals(paddingName))
			return new BufferedBlockCipherWrapper(transformation, new BufferedBlockCipher(modeWithEngine));

		BlockCipherPadding padding = createBlockCipherPadding(paddingName);
		if (padding == null)
			throw new NoSuchPaddingException("There is no block-cipher-padding class registed with the name \"" + paddingName + "\"!");

		return new BufferedBlockCipherWrapper(transformation, new PaddedBufferedBlockCipher(modeWithEngine, padding));
	}

	private Cipher createCipherForBlockCipherMode(String transformation, AEADBlockCipher modeWithEngine, String engineName, String modeName, String paddingName)
	throws NoSuchPaddingException
	{
		// TODO Auto-generated method stub
		throw new UnsupportedOperationException("NYI");
	}

	private Cipher createCipherForBlockCipherMode(String transformation, BufferedBlockCipher modeWithEngine, String engineName, String modeName, String paddingName)
	throws NoSuchPaddingException
	{
		if (paddingName.isEmpty() || "NOPADDING".equals(paddingName))
			return new BufferedBlockCipherWrapper(transformation, modeWithEngine);

		throw new NoSuchPaddingException("The block-cipher-mode \"" + modeName + "\" does not support the padding \"" + paddingName + "\"! Padding must be \"NoPadding\" or an empty string!");
	}

	private Cipher createCipherForBlockCipherEngine(String transformation, BlockCipher engine, String engineName, String modeName, String paddingName)
	throws NoSuchAlgorithmException, NoSuchPaddingException
	{
		if (modeName.isEmpty() || "ECB".equals(modeName))
			return createCipherForBlockCipherMode(transformation, engine, engineName, modeName, paddingName);

		{
			BlockCipher mode = createBlockCipherMode(modeName, engine);
			if (mode != null)
				return createCipherForBlockCipherMode(transformation, mode, engineName, modeName, paddingName);
		}

		{
			BufferedBlockCipher mode = createBufferedBlockCipherMode(modeName, engine);
			if (mode != null)
				return createCipherForBlockCipherMode(transformation, mode, engineName, modeName, paddingName);
		}

		{
			AEADBlockCipher mode = createAEADBlockCipherMode(modeName, engine);
			if (mode != null)
				return createCipherForBlockCipherMode(transformation, mode, engineName, modeName, paddingName);
		}

		throw new NoSuchAlgorithmException("There is no block-cipher-mode-class registered with the modeName \"" + modeName + "\"!");
	}

	private Cipher createCipherForStreamCipherMode(String transformation, StreamCipher modeWithEngine, String engineName, String modeName, String paddingName)
	throws NoSuchPaddingException
	{
		if (paddingName.isEmpty() || "NOPADDING".equals(paddingName))
			return new StreamCipherWrapper(transformation, modeWithEngine);

		throw new NoSuchPaddingException("The stream-cipher-mode \"" + modeName + "\" does not support the padding \"" + paddingName + "\"! Padding must be \"NoPadding\" or an empty string!");
	}

	private Cipher createCipherForStreamCipherEngine(String transformation, StreamCipher engine, String engineName, String modeName, String paddingName)
	throws NoSuchAlgorithmException, NoSuchPaddingException
	{
		if (modeName.isEmpty() || "ECB".equals(modeName))
			return createCipherForStreamCipherMode(transformation, engine, engineName, modeName, paddingName);

		throw new NoSuchAlgorithmException("The stream-cipher does not support the mode \"" + modeName + "\"! Only \"ECB\" or an empty string are allowed as mode!");
	}

	private AsymmetricBlockCipher createAsymmetricBlockCipherPadding(String paddingName, AsymmetricBlockCipher engine)
	{
		Class<? extends AsymmetricBlockCipher> paddingClass = paddingName2asymmetricBlockCipherPaddingClass.get(paddingName);
		if (paddingClass == null)
			return null;

		try {
			Constructor<? extends AsymmetricBlockCipher> c = paddingClass.getConstructor(AsymmetricBlockCipher.class);
			return c.newInstance(engine);
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	private Cipher createCipherForAsymmetricBlockCipherMode(String transformation, AsymmetricBlockCipher modeWithEngine, String engineName, String modeName, String paddingName)
	throws NoSuchPaddingException
	{
		AsymmetricBlockCipher padding;
		if (paddingName.isEmpty() || "NOPADDING".equals(paddingName))
			padding = modeWithEngine;
		else {
			padding = createAsymmetricBlockCipherPadding(paddingName, modeWithEngine);
			if (padding == null)
				throw new NoSuchPaddingException("There is no asymmetric-block-cipher-padding registered with name \"" + paddingName + "\"!");
		}

		return new AsymmetricBlockCipherWrapper(
				transformation,
				new BufferedAsymmetricBlockCipher(padding)
		);
	}

	private Cipher createCipherForAsymmetricBlockCipherEngine(String transformation, AsymmetricBlockCipher engine, String engineName, String modeName, String paddingName)
	throws NoSuchAlgorithmException, NoSuchPaddingException
	{
		if (modeName.isEmpty() || "ECB".equals(modeName))
			return createCipherForAsymmetricBlockCipherMode(transformation, engine, engineName, modeName, paddingName);

		throw new NoSuchAlgorithmException("The asymmetric-block-cipher does not support the mode \"" + modeName + "\"! Only \"ECB\" or an empty string are allowed as mode!");
	}

	/**
	 * <p>
	 * Create a {@link Cipher} instance according to the given transformation.
	 * The transformation is a chain of algorithms containing 1 to 3 elements:
	 * </p>
	 * <ul>
	 * 	<li>encryption algorithm (required)</li>
	 *  <li>mode (optional)</li>
	 *  <li>padding (optional)</li>
	 * </ul>
	 * <p>
	 * For example: "AES", "AES/CBC/PKCS5Padding" or "Twofish/CFB/NoPadding"
	 * </p>
	 *
	 * @param transformation the transformation. This is case-INsensitive. It must not be <code>null</code>.
	 * @return a new <code>Cipher</code> instance.
	 * @throws NoSuchAlgorithmException if there is no encryption engine or no mode registered to suit the given transformation.
	 * @throws NoSuchPaddingException if there is no padding registered to suit the given transformation.
	 */
	public Cipher createCipher(String transformation)
	throws NoSuchAlgorithmException, NoSuchPaddingException
	{
		String[] transformationParts = splitTransformation(transformation);
		String engineName = transformationParts[0].toUpperCase(Locale.ENGLISH);
		String modeName = transformationParts[1].toUpperCase(Locale.ENGLISH);
		String paddingName = transformationParts[2].toUpperCase(Locale.ENGLISH);
		transformationParts = null;

		{
			BlockCipher engine = createBlockCipherEngine(engineName);
			if (engine != null)
				return createCipherForBlockCipherEngine(transformation, engine, engineName, modeName, paddingName);
		}

		{
			AsymmetricBlockCipher engine = createAsymmetricBlockCipherEngine(engineName);
			if (engine != null)
				return createCipherForAsymmetricBlockCipherEngine(transformation, engine, engineName, modeName, paddingName);
		}

		{
			StreamCipher engine = createStreamCipherEngine(engineName);
			if (engine != null)
				return createCipherForStreamCipherEngine(transformation, engine, engineName, modeName, paddingName);
		}

		throw new NoSuchAlgorithmException("There is no cipher-engine-class registered with the algorithmName \"" + engineName + "\"!");
	}

	/**
	 * Split the transformation-<code>String</code> into its parts.
	 * @param transformation the transformation-<code>String</code>.
	 * @return a <code>String</code>-array with exactly 3 elements. None of these is ever <code>null</code>.
	 * If parts are missing in the transformation, the corresponding elements are an empty string.
	 * @throws IllegalArgumentException if the given transformation is <code>null</code> or contains
	 * more than 3 parts (i.e. more than 2 slashes).
	 */
	public static String[] splitTransformation(String transformation)
	throws IllegalArgumentException
	{
		if (transformation == null)
			throw new IllegalArgumentException("transformation == null");

		String[] result = new String[3];
		Arrays.fill(result, "");

		int lastSlashIdx = -1;
		int resultIdx = -1;
		while (true) {
			int slashIdx = transformation.indexOf('/', lastSlashIdx + 1);
			if (slashIdx < 0)
				slashIdx = transformation.length();

			if (++resultIdx > result.length - 1)
				throw new IllegalArgumentException("transformation=\"" + transformation + "\" contains more than " + (result.length - 1) + " slashes!");

			result[resultIdx] = transformation.substring(lastSlashIdx + 1, slashIdx).trim();
			lastSlashIdx = slashIdx;

			if (slashIdx == transformation.length())
				break;
		}

		return result;
	}
}
