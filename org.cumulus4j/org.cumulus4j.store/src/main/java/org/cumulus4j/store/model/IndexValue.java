/*
 * Cumulus4j - Securing your data in the cloud - http://cumulus4j.org
 * Copyright (C) 2011 NightLabs Consulting GmbH
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.cumulus4j.store.model;

import java.io.ByteArrayOutputStream;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

/**
 * Helper for en- &amp; decoding the decrypted (plain) contents of
 * {@link IndexEntry#getIndexValue() IndexEntry.indexValue}. This byte-array holds
 * references to {@link DataEntry#getDataEntryID() DataEntry.dataEntryID}s.
 *
 * @author Marco หงุ่ยตระกูล-Schulze - marco at nightlabs dot de
 */
public class IndexValue
{
	private static final boolean OPTIMIZED_ENCODING = true;
	private Set<Long> dataEntryIDs;

	/**
	 * Create an empty instance of <code>IndexValue</code>. This is equivalent to
	 * calling {@link #IndexValue(byte[])} with a <code>null</code> or an empty argument.
	 */
	public IndexValue() {
		this(null);
	}

	/**
	 * Create an <code>IndexValue</code> instance from the decrypted (plain) byte-array
	 * which is stored in {@link IndexEntry#getIndexValue() IndexEntry.indexValue}.
	 *
	 * @param indexValueByteArray the plain (decrypted) byte-array of {@link IndexEntry#getIndexValue()} or <code>null</code>
	 * (<code>null</code> is equivalent to an empty byte-array). This byte-array is what is created by {@link #toByteArray()}.
	 */
	public IndexValue(byte[] indexValueByteArray) {
		if (indexValueByteArray == null)
			dataEntryIDs = new HashSet<Long>(); // A HashSet is faster than a TreeSet and I don't see a need for the sorting.
		else {
			if (OPTIMIZED_ENCODING) {
//				dataEntryIDs = new HashSet<Long>(); // A HashSet is faster than a TreeSet and I don't see a need for the sorting.
				dataEntryIDs = new HashSet<Long>(indexValueByteArray.length / 4);
				if (indexValueByteArray.length > 0) {
					// The first byte (index 0) is the version. Currently only version 1 is supported.
					int version = (indexValueByteArray[0] & 0xff);
					switch (version) {
						case 1: {
							for (int i = 1; i < indexValueByteArray.length; ++i) {
								// take the first 3 bits and shift them to the right; add 1 (because we have 1 to 8 following - not 0 to 7)
								final int bytesFollowing = (7 & (indexValueByteArray[i] >>> 5)) + 1;
								// take all but the first 3 bits (if 8 bytes follow, we don't need this, because these 8 bytes are a full long, already).
								final int payloadFromFirstByte = bytesFollowing == 8 ? 0 : (indexValueByteArray[i] & 0x1f);
								long dataEntryID = payloadFromFirstByte;
								for (int n = 0; n < bytesFollowing; ++n) {
									dataEntryID = (dataEntryID << 8) | (indexValueByteArray[++i] & 0xff);
								}
								dataEntryIDs.add(dataEntryID);
							}
							break;
						}
						default:
							throw new IllegalArgumentException("Unsupported version: " + version);
					}
				}
			}
			else {
				if ((indexValueByteArray.length % 8) != 0)
					throw new IllegalArgumentException("indexValueByteArray.length is not dividable by 8!");

				dataEntryIDs = new HashSet<Long>(indexValueByteArray.length / 8);

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
	}

	/**
	 * Get a byte-array with all {@link #getDataEntryIDs() dataEntryIDs}. It can be passed to
	 * {@link #IndexValue(byte[])} later (e.g. after encrypting, persisting, loading &amp; decrypting).
	 * @return a byte-array holding all dataEntryIDs managed by this instance.
	 */
	public byte[] toByteArray()
	{
		if (OPTIMIZED_ENCODING) {
			ByteArrayOutputStream out = new ByteArrayOutputStream(dataEntryIDs.size() * 8);
			// version 1
			out.write(1);

			// write dataEntryIDs
			for (long dataEntryID : dataEntryIDs) {
				if (dataEntryID < 0)
					throw new IllegalStateException("dataEntryID < 0!!!");

// first implementation (replaced by a slightly faster one below)
//				byte[] va = new byte[8];
//				int firstNonNullIndex = -1;
//				int m = 8;
//				for (int n = 0; n < 8; ++n) {
//					--m;
//					va[n] = (byte)(dataEntryID >>> (8 * m));
//					if (firstNonNullIndex < 0 && va[n] != 0)
//						firstNonNullIndex = n;
//				}
//				if (firstNonNullIndex < 0)
//					firstNonNullIndex = 7;
//
//				int idx;
//				int firstByte;
//				if (firstNonNullIndex < 7 && (0xff & va[firstNonNullIndex]) <= 0x1f) {
//					// Merge first non-null byte with meta-byte.
//					idx = firstNonNullIndex + 1;
//					firstByte = va[firstNonNullIndex];
//				}
//				else {
//					// Value too high or minimum of 1 following byte.
//					// NOT merge first non-null byte with meta-byte!
//					idx = firstNonNullIndex;
//					firstByte = 0;
//				}
//
//				int bytesFollowingMinus1 = 7 - idx;
//				firstByte |= bytesFollowingMinus1 << 5;
//				out.write(firstByte);
//				while (idx < 8) {
//					out.write(va[idx++]);
//				}

				// slightly faster implementation (harder to read, though)
				int firstNonNullIndex = -1;
				int m = 8;
				for (int n = 0; n < 8; ++n) {
					--m;
					int v = ((byte)(dataEntryID >>> (8 * m))) & 0xff;
					if (firstNonNullIndex >= 0 || v != 0 || n == 7) {
						if (firstNonNullIndex < 0 || (firstNonNullIndex < 0 && n == 7)) {
							firstNonNullIndex = n;
							// Meta-byte was not yet written - handle size header now.

							int bytesFollowingMinus1 = 7 - firstNonNullIndex;
							if (firstNonNullIndex < 7 && v <= 0x1f) {
								--bytesFollowingMinus1;
								// Merge first non-null byte with meta-byte.
								v |= bytesFollowingMinus1 << 5;
								out.write(v);
							}
							else {
								// Value too high or minimum of 1 following byte.
								// NOT merge first non-null byte with meta-byte!
								out.write(bytesFollowingMinus1 << 5);
								out.write(v);
							}
						}
						else {
							// Meta-byte was already written - simply write payload
							out.write(v);
						}
					}
				}

			}
			return out.toByteArray();
		}
		else {
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
