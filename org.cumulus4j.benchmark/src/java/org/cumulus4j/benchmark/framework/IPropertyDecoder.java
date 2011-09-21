package org.cumulus4j.benchmark.framework;

public interface IPropertyDecoder {
	
	public String encode(int[] values);

	public int getPropertyIndex(String property);
	
	public String[] decode(String properties);
}
