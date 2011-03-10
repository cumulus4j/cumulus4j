package org.cumulus4j.core.model;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

/**
 * @author Marco หงุ่ยตระกูล-Schulze - marco at nightlabs dot de
 */
public class IndexValue
{
	private Set<Long> dataEntryIDs = new HashSet<Long>(); // A HashSet is faster than a TreeSet and I don't see a need for the sorting.

	public IndexValue() {
		this(null);
	}

	/**
	 * Create an <code>IndexValue</code> instance from the decrypted (plain-text) byte-array
	 * which is stored in {@link IndexEntry#getIndexValue()}.
	 *
	 * @param indexValueByteArray the plain-text (decrypted) byte-array of {@link IndexEntry#getIndexValue()} or <code>null</code>
	 * (<code>null</code> is equivalent to an empty byte-array). This byte-array is what is created by {@link #toByteArray()}.
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

	/**
	 * Get a byte-array with all {@link #getDataEntryIDs() dataEntryIDs}. It can be passed to
	 * {@link #IndexValue(byte[])} later (e.g. after encrypting, persisting, loading &amp; decrypting).
	 * @return a byte-array holding all dataEntryIDs managed by this instance.
	 */
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

	/**
	 * Get {@link DataEntry#getDataEntryID() dataEntryID}s referencing those {@link DataEntry}s which this <code>IndexValue</code>
	 * (or more precisely the {@link IndexEntry} from which this <code>IndexValue</code> was created) points to.
	 * @return the object-IDs of the <code>DataEntry</code> instances that are referenced by this index entry.
	 */
	public Set<Long> getDataEntryIDs() {
		return Collections.unmodifiableSet(dataEntryIDs);
	}

	public boolean isDataEntryIDsEmpty()
	{
		return dataEntryIDs.isEmpty();
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

//	public static void main(String[] args) {
//		Random random = new Random();
//		IndexValue indexValue1 = new IndexValue();
//		for (int i = 0; i < 100; ++i) {
//			long dataEntryID = random.nextLong();
//			indexValue1.addDataEntryID(dataEntryID);
//		}
//
//		for (Long dataEntryID : indexValue1.getDataEntryIDs()) {
//			System.out.println(dataEntryID);
//		}
//
//		System.out.println();
//		System.out.println();
//		System.out.println();
//
//		byte[] byteArray = indexValue1.toByteArray();
//
//		IndexValue indexValue2 = new IndexValue(byteArray);
//		for (Long dataEntryID : indexValue2.getDataEntryIDs()) {
//			System.out.println(dataEntryID);
//		}
//
//		System.out.println();
//		System.out.println();
//		System.out.println();
//
//		System.out.println(indexValue1.equals(indexValue2));
//	}

}
