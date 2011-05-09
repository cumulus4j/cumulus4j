/**
 * <p>
 * Property classes for the <code>KeyStore</code>'s property management.
 * </p>
 * <p>
 * The <code>KeyStore</code> supports managing arbitrary properties in the form of
 * name-value-pairs. The names are plain-text, but the values are encrypted.
 * A property-value can be of any type for which a subclass of
 * {@link org.cumulus4j.keystore.prop.Property} exists.
 * </p>
 * <p>
 * See {@link org.cumulus4j.keystore.KeyStore#getProperty(String, char[], Class, String)}
 * and {@link org.cumulus4j.keystore.prop.Property} for further infos.
 * </p>
 */
package org.cumulus4j.keystore.prop;
