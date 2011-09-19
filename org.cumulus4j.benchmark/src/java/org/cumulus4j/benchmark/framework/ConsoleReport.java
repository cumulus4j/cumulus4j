package org.cumulus4j.benchmark.framework;

import java.util.ArrayList;

public class ConsoleReport implements IReport {
	
	private ArrayList<String> reports;
	
	public ConsoleReport(){
		
		reports = new ArrayList<String>();
	}
	
//	private String warmupReport;
//	
//	private String totalReport;
//	
//	private String readReport;
//	
//	@Override
//	public void setWarmupReport(String report){
//	
//		warmupReport = report;
//	}
//	
//	@Override
//	public void setStoreReport(String report){
//		
//		totalReport = report;
//	}
//	
//	@Override
//	public void setReadReport(String report){
//		
//		readReport = report;
//	}
	
	
	@Override
	public void addReport(String report){
		reports.add(report);
	}
	
	@Override
	public String getFullReport(){
		
		String result = "\n###############################BENCHMARK RESULTS###############################\n";
		
		for(String report : reports)
			result += report;
		
		result += "###############################################################################";
		
		return result;
//		return "\n###############################BENCHMARK RESULTS###############################\n" 
//			+ warmupReport + totalReport + readReport
//			+ "###############################################################################";
	}
}
