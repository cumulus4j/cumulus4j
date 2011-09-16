package org.cumulus4j.benchmark.framework;

import java.util.Properties;

import org.cumulus4j.store.test.framework.TestUtil;

public class PropertyHandler {
	
	public static final int OBJECT_COUNT;
	
	static{
		
		Properties benchmarkProps = TestUtil.loadProperties("benchmark.properties");
		OBJECT_COUNT = Integer.parseInt(benchmarkProps.getProperty("cumulus4j.benchmark.objectCount"));
	}
}
