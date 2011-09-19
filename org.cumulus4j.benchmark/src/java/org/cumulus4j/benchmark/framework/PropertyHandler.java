package org.cumulus4j.benchmark.framework;

import java.util.Properties;

import org.cumulus4j.store.test.framework.TestUtil;

public class PropertyHandler {
	
	public static final int ENCRYPTION_ALGORITHM = 0;
	public static final int MODE = 1;
	public static final int PADDING = 2;
	public static final int MAC_ALGORITHM = 3;
	
	public static final int TOTAL_OBJECTS;
	
	public static final int WARMUP_OBJECTS;
	
	public static final int TEST_OBJECTS;
	
//	public static final String[] ENCRYPTION_ALGORITHMS;
	
	static{
		
		Properties benchmarkProps = TestUtil.loadProperties("benchmark.properties");
		
		TOTAL_OBJECTS = Integer.parseInt(benchmarkProps.getProperty("cumulus4j.benchmark.totalObjects"));
		WARMUP_OBJECTS = Integer.parseInt(benchmarkProps.getProperty("cumulus4j.benchmark.warmupObjects"));
		
		if(TOTAL_OBJECTS < WARMUP_OBJECTS)
			throw new RuntimeException("Total object count must be equal of bigger than warumup object count!");
		
		TEST_OBJECTS = TOTAL_OBJECTS - WARMUP_OBJECTS;
		
//		ENCRYPTION_ALGORITHMS = calculateAlgorithms(benchmarkProps);
	}
	
//	private static String[] calculateAlgorithms(Properties props){
//		
//	}
	
//	public String[] getNextProperty(){
//		
//	}
}
