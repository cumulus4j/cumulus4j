package org.cumulus4j.crypto;

import java.security.NoSuchAlgorithmException;
import java.util.HashMap;
import java.util.Map;

import org.bouncycastle.crypto.AsymmetricBlockCipher;
import org.bouncycastle.crypto.BlockCipher;
import org.bouncycastle.crypto.StreamCipher;
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
import org.bouncycastle.crypto.engines.RSABlindingEngine;
import org.bouncycastle.crypto.engines.RSAEngine;
import org.bouncycastle.crypto.engines.RijndaelEngine;
import org.bouncycastle.crypto.engines.SEEDEngine;
import org.bouncycastle.crypto.engines.Salsa20Engine;
import org.bouncycastle.crypto.engines.SerpentEngine;
import org.bouncycastle.crypto.engines.SkipjackEngine;
import org.bouncycastle.crypto.engines.TEAEngine;
import org.bouncycastle.crypto.engines.TwofishEngine;
import org.bouncycastle.crypto.engines.XTEAEngine;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Marco หงุ่ยตระกูล-Schulze - marco at nightlabs dot de
 */
public final class CryptoRegistry
{
	private static final Logger logger = LoggerFactory.getLogger(CryptoRegistry.class);
	private static CryptoRegistry sharedInstance = new CryptoRegistry();

	public static CryptoRegistry sharedInstance()
	{
		return sharedInstance;
	}

	private Map<String, Class<? extends AsymmetricBlockCipher>> algorithmName2asymmetricBlockCipherEngineClass = new HashMap<String, Class<? extends AsymmetricBlockCipher>>();
	private Map<String, Class<? extends BlockCipher>> algorithmName2blockCipherEngineClass = new HashMap<String, Class<? extends BlockCipher>>();
	private Map<String, Class<? extends StreamCipher>> algorithmName2streamCipherEngineClass = new HashMap<String, Class<? extends StreamCipher>>();

	private void registerBlockCipherEngineClass(Class<? extends BlockCipher> engineClass)
	{
		try {
			BlockCipher engine = engineClass.newInstance();
			String algorithmName = engine.getAlgorithmName();
			logger.debug("registerSymmetricEngineClass: algorithmName=\"{}\" engineClass=\"{}\"", algorithmName, engineClass.getName());
			algorithmName2blockCipherEngineClass.put(algorithmName, engineClass);
		} catch (InstantiationException e) {
			throw new RuntimeException(e);
		} catch (IllegalAccessException e) {
			throw new RuntimeException(e);
		}
	}

	private void registerAsymmetricBlockCipherEngineClass(String algorithmName, Class<? extends AsymmetricBlockCipher> engineClass)
	{
		try {
			engineClass.newInstance(); // for testing, if the default constructor can be used, only - instance is not used
			logger.debug("registerAsymmetricEngineClass: algorithmName=\"{}\" engineClass=\"{}\"", algorithmName, engineClass.getName());
			algorithmName2asymmetricBlockCipherEngineClass.put(algorithmName, engineClass);
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
			algorithmName2streamCipherEngineClass.put(algorithmName, engineClass);
		} catch (InstantiationException e) {
			throw new RuntimeException(e);
		} catch (IllegalAccessException e) {
			throw new RuntimeException(e);
		}
	}

	private CryptoRegistry() {
		// *** BEGIN AsymmetricBlockCipher engines ***
		registerAsymmetricBlockCipherEngineClass("ElGamal", ElGamalEngine.class);
		registerAsymmetricBlockCipherEngineClass("NaccacheStern", NaccacheSternEngine.class);
		registerAsymmetricBlockCipherEngineClass("RSABlinded", RSABlindedEngine.class);
		registerAsymmetricBlockCipherEngineClass("RSABlinding", RSABlindingEngine.class);
		registerAsymmetricBlockCipherEngineClass("RSA", RSAEngine.class);
		// *** END AsymmetricBlockCipher engines ***


		// *** BEGIN BlockCipher engines ***
		registerBlockCipherEngineClass(AESEngine.class);
		registerBlockCipherEngineClass(AESFastEngine.class);
		registerBlockCipherEngineClass(AESLightEngine.class);

		registerBlockCipherEngineClass(BlowfishEngine.class);
		registerBlockCipherEngineClass(CamelliaEngine.class);
		registerBlockCipherEngineClass(CamelliaLightEngine.class);
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

	}

	public BlockCipher createBlockCipher(String algorithmName)
	throws NoSuchAlgorithmException
	{
		Class<? extends BlockCipher> engineClass = algorithmName2blockCipherEngineClass.get(algorithmName);
		if (engineClass == null)
			throw new NoSuchAlgorithmException("There is no BlockCipher engine class registered for the algorithmName \"" + algorithmName + "\"!");

		try {
			return engineClass.newInstance();
		} catch (InstantiationException e) {
			throw new RuntimeException(e);
		} catch (IllegalAccessException e) {
			throw new RuntimeException(e);
		}
	}

}
