package org.cumulus4j.crypto.test;

import org.cumulus4j.crypto.CipherEngineType;
import org.cumulus4j.crypto.CryptoRegistry;
import org.junit.Test;

public class ShowSupportedAlgosForDocumentation
{
	private static CryptoRegistry cryptoRegistry = CryptoRegistry.sharedInstance();

	@Test
	public void showSupportedCipherEngines() {
		for (CipherEngineType cipherEngineType : CipherEngineType.values()) {
			System.out.println(">>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>");
			System.out.println("*** CipherEngine/" + cipherEngineType + " ***");
			for (String cipherEngine : cryptoRegistry.getSupportedCipherEngines(cipherEngineType))
				System.out.println(cipherEngine);

			System.out.println("<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<");
		}
	}

	@Test
	public void showSupportedBlockCipherModes() {
			System.out.println(">>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>");
			System.out.println("*** BlockCipherMode ***");
			for (String mode : cryptoRegistry.getSupportedCipherModes(null))
				System.out.println(mode);

			System.out.println("<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<");
	}

	@Test
	public void showSupportedPaddings() {
		for (CipherEngineType cipherEngineType : CipherEngineType.values()) {
			System.out.println(">>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>");
			System.out.println("*** Padding/" + cipherEngineType + " ***");
			for (String padding : cryptoRegistry.getSupportedCipherPaddings(cipherEngineType))
				System.out.println(padding);

			System.out.println("<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<");
		}
	}

	@Test
	public void showSupportedMACs() {

	}

}
