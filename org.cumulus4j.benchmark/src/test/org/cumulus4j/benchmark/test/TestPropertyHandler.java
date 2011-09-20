package org.cumulus4j.benchmark.test;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.cumulus4j.benchmark.framework.PropertyHandler;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class TestPropertyHandler {
	
	private static final Logger logger = LoggerFactory
	.getLogger(TestPropertyHandler.class);

	@Test
	public void testCalculateAlgorithms(){
		
		List<Map<String, String>> configurations = new ArrayList<Map<String, String>>();
		
		while(PropertyHandler.hasNext()){
			configurations.add(PropertyHandler.nextConfiguration());
		}
		
		logger.debug("Configurations:\n" + configurations.toString());
	}
}
