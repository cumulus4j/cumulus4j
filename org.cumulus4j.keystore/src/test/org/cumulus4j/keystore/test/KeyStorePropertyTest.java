package org.cumulus4j.keystore.test;

import java.io.File;

import junit.framework.Assert;

import org.cumulus4j.keystore.KeyStore;
import org.cumulus4j.keystore.prop.LongProperty;
import org.cumulus4j.keystore.prop.StringProperty;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class KeyStorePropertyTest
{
	private static final Logger logger = LoggerFactory.getLogger(KeyStorePropertyTest.class);

	protected static final String USER = "marco";
	protected static final char[] PASSWORD = "test12345".toCharArray();

	private File keyStoreFile;
	private KeyStore keyStore;

	@Before
	public void before()
	throws Exception
	{
		keyStoreFile = File.createTempFile("test-", ".keystore");
		keyStore = new KeyStore(keyStoreFile);
		keyStore.createUser(USER, PASSWORD, USER, PASSWORD);
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

	@Test(expected=IllegalStateException.class)
	public void testIllegalPropertyOperation_setName()
	throws Exception
	{
		String propertyName = "property1";
		StringProperty property = keyStore.getProperty(USER, PASSWORD, StringProperty.class, propertyName);
		property.setName("xyz");
	}

	private static StringProperty safeCreateStringProperty(String propertyName)
	{
		try {
			StringProperty property = new StringProperty();
			property.setName(propertyName);
			return property;
		} catch (Exception x) {
			logger.error(x.toString(), x);
			return null;
		}
	}

	@Test(expected=IllegalArgumentException.class)
	public void testIllegalPropertyOperation_setManuallyInstantiatedProperty()
	throws Exception
	{
		StringProperty property = safeCreateStringProperty("property1");
		property.setValue("test");
		keyStore.setProperty(USER, PASSWORD, property);
	}

	@Test
	public void testLongProperty()
	throws Exception
	{
		String propertyName = "LongProperty1";
		LongProperty property = keyStore.getProperty(USER, PASSWORD, LongProperty.class, propertyName);
		Long value = 7823647234L;
		property.setValue(value);
		keyStore.setProperty(USER, PASSWORD, property);

		keyStore = new KeyStore(keyStoreFile);
		property = keyStore.getProperty(USER, PASSWORD, LongProperty.class, propertyName);
		Assert.assertEquals(value, property.getValue());
	}

	@Test
	public void testStringProperty()
	throws Exception
	{
		String propertyName = "StringProperty1";
		StringProperty property = keyStore.getProperty(USER, PASSWORD, StringProperty.class, propertyName);
		String value = "test value - bla bla bla";
		property.setValue(value);
		keyStore.setProperty(USER, PASSWORD, property);

		keyStore = new KeyStore(keyStoreFile);
		property = keyStore.getProperty(USER, PASSWORD, StringProperty.class, propertyName);
		Assert.assertEquals(value, property.getValue());
	}

}
