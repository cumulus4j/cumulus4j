package org.cumulus4j.keystore.prop;

import java.nio.charset.Charset;

/**
 * @author Marco หงุ่ยตระกูล-Schulze - marco at nightlabs dot de
 */
public class StringProperty extends Property<String>
{
	private static final Charset UTF8 = Charset.forName("UTF-8");

	@Override
	public byte[] getValueEncoded()
	{
		String value = getValue();
		if (value == null)
			return null;

		return value.getBytes(UTF8);
	}

	@Override
	public void setValueEncoded(byte[] encodedValue) {
		if (encodedValue == null)
			setValue(null);
		else
			setValue(new String(encodedValue, UTF8));
	}
}
