package org.cumulus4j.test.model;

import java.util.Collections;
import java.util.Random;
import java.util.Set;
import java.util.TreeSet;

public class IndexValue
{
	private Set<Long> dataEntryIDs = new TreeSet<Long>();

	public IndexValue() {
		this(null);
	}

	/**
	 * @param indexValueByteArray the plain-text (decrypted) byte-array of {@link IndexEntry#getIndexValue()}.
	 */
	public IndexValue(byte[] indexValueByteArray) {
		if (indexValueByteArray != null) {
			if ((indexValueByteArray.length % 8) != 0)
				throw new IllegalArgumentException("indexValueByteArray.length is not dividable by 8!");

			for (int i = 0; i < indexValueByteArray.length / 8; ++i) {
				long dataEntryID =
					(((long)indexValueByteArray[i * 8 + 0] & 0xff) << 56) +
					(((long)indexValueByteArray[i * 8 + 1] & 0xff) << 48) +
					(((long)indexValueByteArray[i * 8 + 2] & 0xff) << 40) +
					(((long)indexValueByteArray[i * 8 + 3] & 0xff) << 32) +
					(((long)indexValueByteArray[i * 8 + 4] & 0xff) << 24) +
					(((long)indexValueByteArray[i * 8 + 5] & 0xff) << 16) +
					(((long)indexValueByteArray[i * 8 + 6] & 0xff) <<  8) +
					(indexValueByteArray[i * 8 + 7] & 0xff)
				;
				dataEntryIDs.add(dataEntryID);
			}

		}
	}

	public byte[] toByteArray()
	{
		byte[] result = new byte[dataEntryIDs.size() * 8];
		int i = -1;
		for (Long dataEntryID : dataEntryIDs) {
			long v = dataEntryID;
			result[++i] = (byte)(v >>> 56);
			result[++i] = (byte)(v >>> 48);
			result[++i] = (byte)(v >>> 40);
			result[++i] = (byte)(v >>> 32);
			result[++i] = (byte)(v >>> 24);
			result[++i] = (byte)(v >>> 16);
			result[++i] = (byte)(v >>> 8);
			result[++i] = (byte)v;
		}
		return result;
	}

	public Set<Long> getDataEntryIDs() {
		return Collections.unmodifiableSet(dataEntryIDs);
	}

	public boolean addDataEntryID(long dataEntryID)
	{
		return dataEntryIDs.add(dataEntryID);
	}

	public boolean removeDataEntryID(long dataEntryID)
	{
		return dataEntryIDs.remove(dataEntryID);
	}

	@Override
	public int hashCode() {
		return dataEntryIDs.hashCode();
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) return true;
		if (obj == null) return false;
		if (getClass() != obj.getClass()) return false;
		IndexValue other = (IndexValue) obj;
		return this.dataEntryIDs.equals(other.dataEntryIDs);
	}

	public static void main(String[] args) {
		Random random = new Random();
		IndexValue indexValue1 = new IndexValue();
		for (int i = 0; i < 100; ++i) {
			long dataEntryID = random.nextLong();
			indexValue1.addDataEntryID(dataEntryID);
		}

		for (Long dataEntryID : indexValue1.getDataEntryIDs()) {
			System.out.println(dataEntryID);
		}

		System.out.println();
		System.out.println();
		System.out.println();

		byte[] byteArray = indexValue1.toByteArray();

		IndexValue indexValue2 = new IndexValue(byteArray);
		for (Long dataEntryID : indexValue2.getDataEntryIDs()) {
			System.out.println(dataEntryID);
		}

		System.out.println();
		System.out.println();
		System.out.println();

		System.out.println(indexValue1.equals(indexValue2));
	}

}
