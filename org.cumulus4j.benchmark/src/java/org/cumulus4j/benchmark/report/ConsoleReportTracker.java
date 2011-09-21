package org.cumulus4j.benchmark.report;

import org.cumulus4j.benchmark.framework.IReportTracker;

/**
 * Implementation of {@link IReportTracker} which provides an output 
 * of the benchmark results on the console.
 * 
 * @author Jan Mortensen - jmortensen at nightlabs dot de
 */
public class ConsoleReportTracker implements IReportTracker<String> {
	
	private StringBuilder fullReport;
	
	public ConsoleReportTracker(){
		
		fullReport = new StringBuilder("\n###############################BENCHMARK RESULTS###############################\n");
//		fullReport.append("-------------------------------------------------------------------------------\n");
	}
	
	@Override
	public void addReport(String report){
//		reports.add(fullReport);
		this.fullReport.append(report);
		this.fullReport.append("\n\n");
	}
	
	@Override
	public String getFullReport(){

		fullReport.append("-------------------------------------------------------------------------------\n");
		fullReport.append("###############################################################################");
		return fullReport.toString();
//		StringBuilder result 
//		= new StringBuilder("\n###############################BENCHMARK RESULTS###############################\n");
//		
//		for(String fullReport : reports)
//			result.append(fullReport);
//		
//		result.append("###############################################################################");
//		
//		return result.toString();
	}
	
	@Override
	public void newStory(String storyName){
		//TODO handle case: more than 71 character in the story name
		fullReport.append("-------------------------------------------------------------------------------\n");
		fullReport.append("Story: " + storyName);// + "-------------------------------------------------------------------------------\n");
		for(int i = 0; i < 71 - storyName.length(); i++)
			fullReport.append("-");
		fullReport.append("\n");
//		fullReport.append("-------------------------------------------------------------------------------\n");
	}
}
