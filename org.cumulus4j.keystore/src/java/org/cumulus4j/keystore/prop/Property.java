package org.cumulus4j.keystore.prop;

import java.util.Collection;
import java.util.UUID;

import org.cumulus4j.keystore.KeyStore;

/**
 * <p>
 * Base class for all properties.
 * </p>
 * <p>
 * The <code>KeyStore</code> supports managing arbitrary properties in the form of
 * name-value-pairs. The names are plain-text, but the values are encrypted.
 * A property-value can be of any type for which a subclass of
 * {@link org.cumulus4j.keystore.prop.Property} exists.
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
implements Comparable<Property<?>>
{
	private String name;

	private UUID xxx;

	/**
	 * <p>
	 * Get the property's unique name.
	 * </p>
	 * <p>
	 * This name is used as key to uniquely identify a property in the key store.
	 * </p>
	 *
	 * @return the property's name.
	 */
	public String getName() {
		return name;
	}

	/**
	 * <p>
	 * Set the property's unique name.
	 * </p>
	 * <p>
	 * You should never call this method directly. The name is set by {@link KeyStore#getProperty(String, char[], Class, String)}.
	 * </p>
	 *
	 * @param name the property's name.
	 */
	public void setName(String name)
	{
		if (this.name != null && !this.name.equals(name))
			throw new IllegalStateException("The name of a property cannot be changed after it has been assigned once!");

		this.name = name;
	}

	private T value;

	/**
	 * Get the property's value.
	 * @return the value or <code>null</code>.
	 * @see #setValue(Object)
	 */
	public T getValue() {
		return value;
	}
	/**
	 * Set the property's value.
	 * @param value the value or <code>null</code>.
	 * @see #getValue()
	 */
	public void setValue(T value)
	{
		this.value = value;
	}

	/**
	 * <p>
	 * Get the property's {@link #getValue() value} encoded as byte-array or <code>null</code>, if the
	 * property is empty. Note, that this might be <code>null</code>, even though {@link #getValue()} returns
	 * a non-<code>null</code> value; for example an empty {@link Collection} might cause this.
	 * </p>
	 * <p>
	 * This method must encode the value in a way that can be decoded by {@link #setValueEncoded(byte[])}.
	 * </p>
	 * @return the byte-array-representation of the property-value or <code>null</code>.
	 * @see #setValueEncoded(byte[])
	 */
	public abstract byte[] getValueEncoded();

	/**
	 * <p>
	 * Set the property's {@link #getValue() value} encoded as byte-array or <code>null</code>,
	 * if the property shall be empty.
	 * </p>
	 * <p>
	 * This method must be symmetric to {@link #getValueEncoded()}, i.e. every possible result of <code>getValueEncoded()</code>
	 * must be understood by this method. A byte-array that is not understood should cause an {@link IllegalArgumentException}.
	 * </p>
	 * @param encodedValue the byte-array-representation of the property-value or <code>null</code>.
	 * @throws IllegalArgumentException if the <code>encodedValue</code> cannot be parsed.
	 * @see #getValueEncoded()
	 */
	public abstract void setValueEncoded(byte[] encodedValue)
	throws IllegalArgumentException;

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

	@Override
	public int compareTo(Property<?> o)
	{
		if (o == null)
			return 1;

		if (this.getName() == null) {
			if (o.getName() == null)
				return 0;
			else
				return -1;
		}

		if (o.getName() == null)
			return 1;
		else
			return this.getName().compareTo(o.getName());
	}

	@Override
	public int hashCode()
	{
		return name == null ? 0 : name.hashCode();
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) return true;
		if (obj == null) return false;
		if (getClass() != obj.getClass()) return false;
		Property<?> other = (Property<?>) obj;
		return (
				this.getName() == other.getName() ||
				(this.getName() != null && this.getName().equals(other.getName()))
		);
	}
}
