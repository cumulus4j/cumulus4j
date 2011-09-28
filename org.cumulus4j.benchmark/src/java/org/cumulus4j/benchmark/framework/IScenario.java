package org.cumulus4j.benchmark.framework;

import java.util.List;


/**
*
* @author Jan Mortensen - jmortensen at nightlabs dot de
*
*/
public interface IScenario {

	public static final String WARMUP = "warmup";

	public static final String STORE_SINGLE_OBJECT = "storeSingleObject";

	public static final String LOAD_SINGLE_OBJECT = "loadSingleObject";

	public static final String LOAD_ALL_OBJECTS = "loadAllObjects";

	public static final String BULK_LOAD_OBJECTS = "bulkLoadObjects";

	public static final String BULK_STORE_OBJECTS = "bulkStoreObjetcts";



	public String warmup(String cryptoManagerID, String cryptoSessionID);

	public String storeSingleObject(String cryptoManagerID, String cryptoSessionID);

	public String loadSingleObject(String cryptoManagerID, String cryptoSessionID);

	public String bulkLoadObjects(String cryptoManagerID, String cryptoSessionID);

	public String bulkStoreObjects(String cryptoManagerID, String cryptoSessionID);

	public String loadAllObjects(String cryptoManagerID, String cryptoSessionID);

	public List<String> getResults();

}
