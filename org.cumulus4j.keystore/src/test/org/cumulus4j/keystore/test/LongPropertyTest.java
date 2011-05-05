package org.cumulus4j.keystore.test;

import java.security.SecureRandom;

import junit.framework.Assert;

import org.cumulus4j.keystore.prop.LongProperty;
import org.junit.Test;

public class LongPropertyTest
{
	@Test
	public void encodeAndDecodeSomeValues()
	{
		encodeAndDecode(0L);
		encodeAndDecode(10L);
		encodeAndDecode(100L);
		encodeAndDecode(200L);
		encodeAndDecode(400L);
		encodeAndDecode(1000L);
		encodeAndDecode(5000L);
		encodeAndDecode(10000L);
		encodeAndDecode(20000L);
		encodeAndDecode(Long.MIN_VALUE);
		encodeAndDecode(Long.MAX_VALUE);

		SecureRandom random = new SecureRandom();
		for (int i = 0; i < 1000; ++i) {
			encodeAndDecode(random.nextLong());
		}
	}

	private static void encodeAndDecode(long value)
	{
		LongProperty prop1 = new LongProperty();
		LongProperty prop2 = new LongProperty();

		prop1.setValue(value);
		Assert.assertEquals(value, prop1.getValue().longValue());

		prop2.setValueEncoded(prop1.getValueEncoded());
		Assert.assertEquals(value, prop2.getValue().longValue());
	}

}
