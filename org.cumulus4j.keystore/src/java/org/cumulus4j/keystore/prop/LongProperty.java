package org.cumulus4j.keystore.prop;

/**
 * @author Marco หงุ่ยตระกูล-Schulze - marco at nightlabs dot de
 */
public class LongProperty extends Property<Long>
{

	@Override
	public byte[] getValueEncoded() {
		Long value = getValue();
		if (value == null)
			return null;
		else {
			long val = value;
			byte[] result = new byte[8];
			for (int i = 0; i < result.length; ++ i)
				result[i] = (byte)(val >>> (i * 8));

			return result;
		}
	}

	@Override
	public void setValueEncoded(byte[] encodedValue) {
		if (encodedValue == null)
			setValue(null);
		else {
			if (encodedValue.length != 8)
				throw new IllegalArgumentException("encodedValue.length != 8 :: encodedValue must either be null or an array with the correct length!");

			long val = 0;
			for (int i = 0; i < encodedValue.length; ++ i)
				val |= ((long)encodedValue[i] & 0xff) << (i * 8);

			setValue(val);
		}
	}

}
