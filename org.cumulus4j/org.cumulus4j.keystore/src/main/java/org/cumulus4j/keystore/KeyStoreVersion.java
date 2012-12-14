package org.cumulus4j.keystore;

import java.util.Collections;
import java.util.SortedSet;
import java.util.TreeSet;

public final class KeyStoreVersion {

	private KeyStoreVersion() { }

	public static final int[] VERSION_SUPPORTED = { 1, 2 };
	public static final SortedSet<Integer> VERSION_SUPPORTED_SET;
	static {
		SortedSet<Integer> s = new TreeSet<Integer>();
		for (int ver : VERSION_SUPPORTED)
			s.add(ver);

		VERSION_SUPPORTED_SET = Collections.unmodifiableSortedSet(s);
	}

	public static final int VERSION_CURRENT = 2;

}
