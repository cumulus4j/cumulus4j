package org.cumulus4j.keystore.test;

import java.io.File;

import org.cumulus4j.keystore.DateDependentKeyStrategy;
import org.cumulus4j.keystore.KeyStore;
import org.cumulus4j.testutil.IOUtil;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

public class DateDependentKeyStrategyTest {

	protected static final String USER = "marco";
	protected static final char[] PASSWORD = "test12345".toCharArray();

	private File keyStoreFile;
	private KeyStore keyStore;

	@Before
	public void before()
	throws Exception
	{
		keyStoreFile = File.createTempFile("test-", ".keystore");
		String keyStoreID = IOUtil.getFileNameWithoutExtension(keyStoreFile.getName());
		keyStore = new KeyStore(keyStoreID , keyStoreFile);
	}

	@After
	public void after()
	throws Exception
	{
		keyStore = null;
		File f = keyStoreFile;
		if (f != null)
			f.delete();
	}

	/**
	 * Test for <a href="https://sourceforge.net/tracker/?func=detail&aid=3453405&group_id=517465&atid=2102911">bug 3453405</a>.
	 */
	@Test
	public void initialiseWithManyKeys()
	throws Exception
	{
		// TODO This bug can be solved by switching the result of EncryptedProperty.getEncryptedDataLengthHeaderSize()
		// from 2 to 4. But: We MUST increment the file version number and introduce some file-update-mechanism!!!
		// I have no time for this now and leave the value 2. Marco :-)
		DateDependentKeyStrategy strategy = new DateDependentKeyStrategy(keyStore);
		strategy.init(USER, PASSWORD, 30L * 1000L, 2L * 24L * 3600L * 1000L);
	}
}
