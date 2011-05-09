package org.cumulus4j.keystore.prop;

import java.nio.charset.Charset;

/**
 * {@link Property} implementation for the value type {@link String}.
 *
 * @author Marco หงุ่ยตระกูล-Schulze - marco at nightlabs dot de
 */
public class StringProperty extends Property<String>
{
	private static final Charset UTF8 = Charset.forName("UTF-8");

	/**
	 * {@inheritDoc}
	 * <p>
	 * The implementation in <code>StringProperty</code> returns either <code>null</code>
	 * or a UTF-8-encoded <code>String</code>-representation.
	 * </p>
	 */
	@Override
	public byte[] getValueEncoded()
	{
		String value = getValue();
		if (value == null)
			return null;

		return value.getBytes(UTF8);
	}

	/**
	 * {@inheritDoc}
	 * @param encodedValue a UTF-8-encoded <code>String</code>-representation or <code>null</code>.
	 */
	@Override
	public void setValueEncoded(byte[] encodedValue) {
		if (encodedValue == null)
			setValue(null);
		else
			setValue(new String(encodedValue, UTF8));
	}
}
