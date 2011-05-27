package org.cumulus4j.keystore.prop;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.Map;
import java.util.SortedMap;
import java.util.TreeMap;

/**
 * {@link Property} implementation for the value type {@link SortedMap}
 * with both key and value being {@link Long}s.
 * <p>
 * Used for example by the date-dependent key-strategy to store it's key-is-valid-from-dates.
 * </p>
 *
 * @author Marco หงุ่ยตระกูล-Schulze - marco at nightlabs dot de
 */
public class Long2LongSortedMapProperty
extends Property<SortedMap<Long, Long>>
{

	@Override
	public SortedMap<Long, Long> getValue()
	{
		if (super.getValue() == null)
			setValue(new TreeMap<Long, Long>());

		return super.getValue();
	}

	@Override
	public byte[] getValueEncoded()
	{
		SortedMap<Long, Long> map = getValue();

		if (map == null || map.isEmpty())
			return null;

		ByteArrayOutputStream bout = new ByteArrayOutputStream();
		DataOutputStream out = new DataOutputStream(bout);
		try {
			out.writeInt(map.size());
			for (Map.Entry<Long, Long> me : map.entrySet()) {
				out.writeLong(me.getKey());
				out.writeLong(me.getValue());
			}
			out.close(); // Might be necessary for the flushing of the DataOutputStream even though the ByteArrayOutputStream doesn't need it.
		} catch (IOException e) {
			throw new RuntimeException(e);
		}

		return bout.toByteArray();
	}

	@Override
	public void setValueEncoded(byte[] encodedValue)
	throws IllegalArgumentException
	{
		if (encodedValue == null) {
			setValue(null);
			return;
		}

		DataInputStream in = new DataInputStream(new ByteArrayInputStream(encodedValue));
		try {
			int mapSize = in.readInt();
			SortedMap<Long, Long> map = new TreeMap<Long, Long>();
			for (int i = 0; i < mapSize; ++i) {
				long key = in.readLong();
				long value = in.readLong();
				map.put(key, value);
			}
			setValue(map);
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}

}
