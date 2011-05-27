package org.cumulus4j.keystore.test;

import java.io.File;
import java.util.Map;

import junit.framework.Assert;

import org.cumulus4j.keystore.KeyStore;
import org.cumulus4j.keystore.prop.Long2LongSortedMapProperty;
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

	@Test
	public void testLong2LongSortedMapProperty()
	throws Exception
	{
		String propertyName = "Long2LongSortedMapProperty1";
		Long2LongSortedMapProperty property = keyStore.getProperty(USER, PASSWORD, Long2LongSortedMapProperty.class, propertyName);
		property.getValue().put(System.currentTimeMillis(), 1L);
		Long exampleMapKey = System.currentTimeMillis() + 24 * 3600 * 1000;
		Long exampleMapValue = 113344L;
		property.getValue().put(exampleMapKey, exampleMapValue);
		property.getValue().put(System.currentTimeMillis() + 2 * 24 * 3600 * 1000, 375438972L);
		keyStore.setProperty(USER, PASSWORD, property);

		keyStore = new KeyStore(keyStoreFile);
		Long2LongSortedMapProperty property2 = keyStore.getProperty(USER, PASSWORD, Long2LongSortedMapProperty.class, propertyName);
		if (property.getValue().size() != property2.getValue().size())
			Assert.fail("Map sizes are not equal");

		Assert.assertEquals(3, property.getValue().size());

		for (Map.Entry<Long, Long> me : property.getValue().entrySet()) {
			Long prop2Val = property2.getValue().get(me.getKey());
			Assert.assertEquals("Map-entry's values are different!", me.getValue(), prop2Val);
		}

		Assert.assertEquals(exampleMapValue, property2.getValue().get(exampleMapKey));
	}

}
