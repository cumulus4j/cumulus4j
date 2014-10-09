package org.cumulus4j.store.test.datatype;

import java.io.Serializable;
import java.util.Collection;
import java.util.Map;
import java.util.UUID;

import javax.jdo.annotations.IdentityType;
import javax.jdo.annotations.Join;
import javax.jdo.annotations.PersistenceCapable;
import javax.jdo.annotations.PrimaryKey;

import org.cumulus4j.testutil.Util;

/**
 * Entity with all types that should be supported according to the JDO standard.
 * Though, the supported types include {@link Collection}, {@link Map} and other 1-n-relation-types,
 * this class contains simple 1-to-1-fields and arrays of these simple types, only.
 */
@PersistenceCapable(identityType=IdentityType.APPLICATION, objectIdClass=StandardOneToOneTypesEntityPK.class)
public class StandardOneToOneTypesEntity
implements Serializable
{
	private static final long serialVersionUID = 1L;

	/**
	 * Though {@link UUID}-support is a DN extension, it is IMHO so basic and common, that
	 * we need to test it here! And as PK even ;-)
	 */
	@PrimaryKey
	private UUID uuid;

	private boolean booleanPrimitive;
	private byte bytePrimitive;
	private char charPrimitive;
	private double doublePrimitive;
	private float floatPrimitive;
	private int intPrimitive;
	private long longPrimitive;
	private short shortPrimitive;
	@Join
	private boolean[] booleanPrimitiveArray;
	@Join
	private byte[] bytePrimitiveArray;
	@Join
	private char[] charPrimitiveArray;
	@Join
	private double[] doublePrimitiveArray;
	@Join
	private float[] floatPrimitiveArray;
	@Join
	private int[] intPrimitiveArray;
	@Join
	private long[] longPrimitiveArray;
	@Join
	private short[] shortPrimitiveArray;
	private Boolean booleanObject;
	private Byte byteObject;
	private Character charObject;
	private Double doubleObject;
	private Float floatObject;
	private Integer intObject;
	private Long longObject;
	private Short shortObject;
	private Boolean[] booleanObjectArray;
	private Byte[] byteObjectArray;
	private Character[] charObjectArray;
	private Double[] doubleObjectArray;
	private Float[] floatObjectArray;
	private Integer[] intObjectArray;
	private Long[] longObjectArray;
	private Short[] shortObjectArray;

//	Number // TODO later!


	private String string;
	@Join
	private String[] stringArray;
	private java.math.BigDecimal bigDecimal;
	private java.math.BigInteger bigInteger;
	@Join
	private java.math.BigDecimal[] bigDecimalArray;
	@Join
	private java.math.BigInteger[] bigIntegerArray;
	private java.sql.Date sqlDate;
	private java.sql.Time sqlTime;
	private java.sql.Timestamp sqlTimestamp;
	private java.util.BitSet bitSet;
	private java.util.Currency currency;
	private java.util.Date date;
	@Join
	private java.util.Date[] dateArray;
	private java.util.Locale locale;
	@Join
	private java.util.Locale[] localeArray;
	private java.util.TimeZone timeZone;
	private java.io.Serializable serializable;
	private javax.jdo.spi.PersistenceCapable persistenceCapable;
	@Join
	private javax.jdo.spi.PersistenceCapable[] persistenceCapableArray;
	private SizeEnum sizeEnum;
	@Join
	private SizeEnum[] sizeEnumArray;

	public UUID getUuid() {
		return uuid;
	}
	public void setUuid(UUID uuid) {
		this.uuid = uuid;
	}

	public boolean isBooleanPrimitive() {
		return booleanPrimitive;
	}
	public void setBooleanPrimitive(boolean booleanPrimitive) {
		this.booleanPrimitive = booleanPrimitive;
	}
	public byte getBytePrimitive() {
		return bytePrimitive;
	}
	public void setBytePrimitive(byte bytePrimitive) {
		this.bytePrimitive = bytePrimitive;
	}
	public char getCharPrimitive() {
		return charPrimitive;
	}
	public void setCharPrimitive(char charPrimitive) {
		this.charPrimitive = charPrimitive;
	}
	public double getDoublePrimitive() {
		return doublePrimitive;
	}
	public void setDoublePrimitive(double doublePrimitive) {
		this.doublePrimitive = doublePrimitive;
	}
	public float getFloatPrimitive() {
		return floatPrimitive;
	}
	public void setFloatPrimitive(float floatPrimitive) {
		this.floatPrimitive = floatPrimitive;
	}
	public int getIntPrimitive() {
		return intPrimitive;
	}
	public void setIntPrimitive(int intPrimitive) {
		this.intPrimitive = intPrimitive;
	}
	public long getLongPrimitive() {
		return longPrimitive;
	}
	public void setLongPrimitive(long longPrimitive) {
		this.longPrimitive = longPrimitive;
	}
	public short getShortPrimitive() {
		return shortPrimitive;
	}
	public void setShortPrimitive(short shortPrimitive) {
		this.shortPrimitive = shortPrimitive;
	}
	public boolean[] getBooleanPrimitiveArray() {
		return booleanPrimitiveArray;
	}
	public void setBooleanPrimitiveArray(boolean[] booleanPrimitiveArray) {
		this.booleanPrimitiveArray = booleanPrimitiveArray;
	}
	public byte[] getBytePrimitiveArray() {
		return bytePrimitiveArray;
	}
	public void setBytePrimitiveArray(byte[] bytePrimitiveArray) {
		this.bytePrimitiveArray = bytePrimitiveArray;
	}
	public char[] getCharPrimitiveArray() {
		return charPrimitiveArray;
	}
	public void setCharPrimitiveArray(char[] charPrimitiveArray) {
		this.charPrimitiveArray = charPrimitiveArray;
	}
	public double[] getDoublePrimitiveArray() {
		return doublePrimitiveArray;
	}
	public void setDoublePrimitiveArray(double[] doublePrimitiveArray) {
		this.doublePrimitiveArray = doublePrimitiveArray;
	}
	public float[] getFloatPrimitiveArray() {
		return floatPrimitiveArray;
	}
	public void setFloatPrimitiveArray(float[] floatPrimitiveArray) {
		this.floatPrimitiveArray = floatPrimitiveArray;
	}
	public int[] getIntPrimitiveArray() {
		return intPrimitiveArray;
	}
	public void setIntPrimitiveArray(int[] intPrimitiveArray) {
		this.intPrimitiveArray = intPrimitiveArray;
	}
	public long[] getLongPrimitiveArray() {
		return longPrimitiveArray;
	}
	public void setLongPrimitiveArray(long[] longPrimitiveArray) {
		this.longPrimitiveArray = longPrimitiveArray;
	}
	public short[] getShortPrimitiveArray() {
		return shortPrimitiveArray;
	}
	public void setShortPrimitiveArray(short[] shortPrimitiveArray) {
		this.shortPrimitiveArray = shortPrimitiveArray;
	}
	public Boolean getBooleanObject() {
		return booleanObject;
	}
	public void setBooleanObject(Boolean booleanObject) {
		this.booleanObject = booleanObject;
	}
	public Byte getByteObject() {
		return byteObject;
	}
	public void setByteObject(Byte byteObject) {
		this.byteObject = byteObject;
	}
	public Character getCharObject() {
		return charObject;
	}
	public void setCharObject(Character charObject) {
		this.charObject = charObject;
	}
	public Double getDoubleObject() {
		return doubleObject;
	}
	public void setDoubleObject(Double doubleObject) {
		this.doubleObject = doubleObject;
	}
	public Float getFloatObject() {
		return floatObject;
	}
	public void setFloatObject(Float floatObject) {
		this.floatObject = floatObject;
	}
	public Integer getIntObject() {
		return intObject;
	}
	public void setIntObject(Integer intObject) {
		this.intObject = intObject;
	}
	public Long getLongObject() {
		return longObject;
	}
	public void setLongObject(Long longObject) {
		this.longObject = longObject;
	}
	public Short getShortObject() {
		return shortObject;
	}
	public void setShortObject(Short shortObject) {
		this.shortObject = shortObject;
	}
	public Boolean[] getBooleanObjectArray() {
		return booleanObjectArray;
	}
	public void setBooleanObjectArray(Boolean[] booleanObjectArray) {
		this.booleanObjectArray = booleanObjectArray;
	}
	public Byte[] getByteObjectArray() {
		return byteObjectArray;
	}
	public void setByteObjectArray(Byte[] byteObjectArray) {
		this.byteObjectArray = byteObjectArray;
	}
	public Character[] getCharObjectArray() {
		return charObjectArray;
	}
	public void setCharObjectArray(Character[] charObjectArray) {
		this.charObjectArray = charObjectArray;
	}
	public Double[] getDoubleObjectArray() {
		return doubleObjectArray;
	}
	public void setDoubleObjectArray(Double[] doubleObjectArray) {
		this.doubleObjectArray = doubleObjectArray;
	}
	public Float[] getFloatObjectArray() {
		return floatObjectArray;
	}
	public void setFloatObjectArray(Float[] floatObjectArray) {
		this.floatObjectArray = floatObjectArray;
	}
	public Integer[] getIntObjectArray() {
		return intObjectArray;
	}
	public void setIntObjectArray(Integer[] intObjectArray) {
		this.intObjectArray = intObjectArray;
	}
	public Long[] getLongObjectArray() {
		return longObjectArray;
	}
	public void setLongObjectArray(Long[] longObjectArray) {
		this.longObjectArray = longObjectArray;
	}
	public Short[] getShortObjectArray() {
		return shortObjectArray;
	}
	public void setShortObjectArray(Short[] shortObjectArray) {
		this.shortObjectArray = shortObjectArray;
	}
	public String getString() {
		return string;
	}
	public void setString(String string) {
		this.string = string;
	}
	public String[] getStringArray() {
		return stringArray;
	}
	public void setStringArray(String[] stringArray) {
		this.stringArray = stringArray;
	}
	public java.math.BigDecimal getBigDecimal() {
		return bigDecimal;
	}
	public void setBigDecimal(java.math.BigDecimal bigDecimal) {
		this.bigDecimal = bigDecimal;
	}
	public java.math.BigInteger getBigInteger() {
		return bigInteger;
	}
	public void setBigInteger(java.math.BigInteger bigInteger) {
		this.bigInteger = bigInteger;
	}
	public java.math.BigDecimal[] getBigDecimalArray() {
		return bigDecimalArray;
	}
	public void setBigDecimalArray(java.math.BigDecimal[] bigDecimalArray) {
		this.bigDecimalArray = bigDecimalArray;
	}
	public java.math.BigInteger[] getBigIntegerArray() {
		return bigIntegerArray;
	}
	public void setBigIntegerArray(java.math.BigInteger[] bigIntegerArray) {
		this.bigIntegerArray = bigIntegerArray;
	}
	public java.sql.Date getSqlDate() {
		return sqlDate;
	}
	public void setSqlDate(java.sql.Date sqlDate) {
		this.sqlDate = sqlDate;
	}
	public java.sql.Time getSqlTime() {
		return sqlTime;
	}
	public void setSqlTime(java.sql.Time sqlTime) {
		this.sqlTime = sqlTime;
	}
	public java.sql.Timestamp getSqlTimestamp() {
		return sqlTimestamp;
	}
	public void setSqlTimestamp(java.sql.Timestamp sqlTimestamp) {
		this.sqlTimestamp = sqlTimestamp;
	}
	public java.util.BitSet getBitSet() {
		return bitSet;
	}
	public void setBitSet(java.util.BitSet bitSet) {
		this.bitSet = bitSet;
	}
	public java.util.Currency getCurrency() {
		return currency;
	}
	public void setCurrency(java.util.Currency currency) {
		this.currency = currency;
	}
	public java.util.Date getDate() {
		return date;
	}
	public void setDate(java.util.Date date) {
		this.date = date;
	}
	public java.util.Date[] getDateArray() {
		return dateArray;
	}
	public void setDateArray(java.util.Date[] dateArray) {
		this.dateArray = dateArray;
	}
	public java.util.Locale getLocale() {
		return locale;
	}
	public void setLocale(java.util.Locale locale) {
		this.locale = locale;
	}
	public java.util.Locale[] getLocaleArray() {
		return localeArray;
	}
	public void setLocaleArray(java.util.Locale[] localeArray) {
		this.localeArray = localeArray;
	}
	public java.util.TimeZone getTimeZone() {
		return timeZone;
	}
	public void setTimeZone(java.util.TimeZone timeZone) {
		this.timeZone = timeZone;
	}
	public java.io.Serializable getSerializable() {
		return serializable;
	}
	public void setSerializable(java.io.Serializable serializable) {
		this.serializable = serializable;
	}
	public javax.jdo.spi.PersistenceCapable getPersistenceCapable() {
		return persistenceCapable;
	}
	public void setPersistenceCapable(
			javax.jdo.spi.PersistenceCapable persistenceCapable) {
		this.persistenceCapable = persistenceCapable;
	}
	public javax.jdo.spi.PersistenceCapable[] getPersistenceCapableArray() {
		return persistenceCapableArray;
	}
	public void setPersistenceCapableArray(javax.jdo.spi.PersistenceCapable[] persistenceCapableArray) {
		this.persistenceCapableArray = persistenceCapableArray;
	}
	public SizeEnum getSizeEnum() {
		return sizeEnum;
	}
	public void setSizeEnum(SizeEnum sizeEnum) {
		this.sizeEnum = sizeEnum;
	}
	public SizeEnum[] getSizeEnumArray() {
		return sizeEnumArray;
	}
	public void setSizeEnumArray(SizeEnum[] sizeEnumArray) {
		this.sizeEnumArray = sizeEnumArray;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((uuid == null) ? 0 : uuid.hashCode());
		return result;
	}
	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		StandardOneToOneTypesEntity other = (StandardOneToOneTypesEntity) obj;
		return Util.equals(this.uuid, other.uuid);
	}
}
