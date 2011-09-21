package org.cumulus4j.benchmark.framework;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Queue;

import org.cumulus4j.store.test.framework.TestUtil;

/**
 * 
 * @author Jan Mortensen - jmortensen at nightlabs dot de
 *
 */
public class PropertyHandler {
	
	public static final int TOTAL_OBJECTS;
	public static final int WARMUP_OBJECTS;
	public static final int TEST_OBJECTS;
	
	public static final boolean HTML_OUTPUT_ENABLED;
	public static final boolean XML_OUTPUT_ENABLED;
	public static final boolean CONSOLE_OUTPUT_ENABLED;
	
	private static final Queue<Map<String, String>> encryptionAlgorithms;
	
	static{
		
		Properties benchmarkProps = TestUtil.loadProperties("benchmark.properties");
		
		TOTAL_OBJECTS = Integer.parseInt(benchmarkProps.getProperty("cumulus4j.benchmark.totalObjects"));
		WARMUP_OBJECTS = Integer.parseInt(benchmarkProps.getProperty("cumulus4j.benchmark.warmupObjects"));
		
		if(TOTAL_OBJECTS < WARMUP_OBJECTS)
			throw new RuntimeException("Total object count must be equal of bigger than warumup object count!");
		
		TEST_OBJECTS = TOTAL_OBJECTS - WARMUP_OBJECTS;
		
		encryptionAlgorithms = calculateAlgorithms(benchmarkProps);
		
		HTML_OUTPUT_ENABLED = Boolean.parseBoolean(benchmarkProps.getProperty("cumulus4j.benchmark.HTMLOutput"));
		XML_OUTPUT_ENABLED = Boolean.parseBoolean(benchmarkProps.getProperty("cumulus4j.benchmark.XMLOutput"));
		CONSOLE_OUTPUT_ENABLED = Boolean.parseBoolean(benchmarkProps.getProperty("cumulus4j.benchmark.ConsoleOutput"));
	}
	
	private static Queue<Map<String, String>> calculateAlgorithms(Properties props){
		
		int counter = 1;
		
		List<String> encryptionAlgorithms = new ArrayList<String>();
		List<String> macAlgorithms = new ArrayList<String>();
		
		while(props.get("cumulus4j.benchmark.encryptionAlgorithm" + counter) != null){
			encryptionAlgorithms.add("cumulus4j.benchmark.encryptionAlgorithm" + counter);
			counter++;
		}
		
		counter = 1;
		
		while(props.get("cumulus4j.benchmark.macAlgorithm" + counter) != null){
			macAlgorithms.add("cumulus4j.benchmark.macAlgorithm" + counter);
			counter++;
		}
		
		Queue<Map<String, String>> result = new LinkedList<Map<String, String>>();
		Map<String, String> configuration;
		
		for(String encryptionAlgorithm : encryptionAlgorithms){
			for(String macAlgorithm : macAlgorithms){
				configuration = new HashMap<String, String>();
				configuration.put("cumulus4j.benchmark.encryptionAlgorithm", props.getProperty(encryptionAlgorithm));
				configuration.put("cumulus4j.benchmark.macAlgorithm", props.getProperty(macAlgorithm));
				result.add(configuration);
			}
		}
		
		return result;
	}
	
	public static Map<String, String> nextConfiguration(){
		return encryptionAlgorithms.poll();

	}
	
	public static boolean hasNext(){
		return !encryptionAlgorithms.isEmpty();
	}
}
