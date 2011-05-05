package org.cumulus4j.keystore.prop;

import java.util.UUID;

import org.cumulus4j.keystore.KeyStore;

/**
 * <p>
 * Base class for all properties.
 * </p>
 * <p>
 * <b>Important:</b> Do not instantiate properties yourself! Use {@link KeyStore#getProperty(String, char[], Class, String)}
 * instead!
 * </p>
 *
 * @author Marco หงุ่ยตระกูล-Schulze - marco at nightlabs dot de
 *
 * @param <T> the type of the property-value.
 */
public abstract class Property<T>
{
	private String name;

	private UUID xxx;

	public String getName() {
		return name;
	}

	public void setName(String name)
	{
		if (this.name != null && !this.name.equals(name))
			throw new IllegalStateException("The name of a property cannot be changed after it has been assigned once!");

		this.name = name;
	}

	private T value;

	public T getValue() {
		return value;
	}
	public void setValue(T value)
	{
		this.value = value;
	}

	public abstract byte[] getValueEncoded();

	public abstract void setValueEncoded(byte[] encodedValue);

	@Override
	public String toString() {
		return super.toString() + '[' + getName() + ',' + getValue() + ']';
	}

	/**
	 * <p>Internal value used to detect improper usage of the API.</p>
	 * <p>
	 * <b>Important:</b> This method is not part of the API! <b>Do not use this method!</b>
	 * </p>
	 * @return the internal value.
	 * @see KeyStore#getProperty(String, char[], Class, String)
	 */
	public UUID getXxx() {
		return xxx;
	}
	/**
	 * <p>Internal value used to detect improper usage of the API.</p>
	 * <p>
	 * <b>Important:</b> This method is not part of the API! <b>Do not use this method!</b>
	 * </p>
	 * @param xxx the internal value.
	 * @see KeyStore#getProperty(String, char[], Class, String)
	 */
	public void setXxx(UUID xxx) {
		this.xxx = xxx;
	}
}
