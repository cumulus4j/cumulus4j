package org.cumulus4j.benchmark.framework;

/**
 * Implementation of {@link IReport} which provides an output 
 * of the benchmark results on the console.
 * 
 * @author Jan Mortensen - jmortensen at nightlabs dot de
 */
public class ConsoleReport implements IReport<String> {
	
//	private List<String> reports;
	private StringBuilder fullReport;
	
	public ConsoleReport(){
		
//		reports = new ArrayList<String>();
		fullReport = new StringBuilder("\n###############################BENCHMARK RESULTS###############################\n");
		fullReport.append("-------------------------------------------------------------------------------\n");
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
	public void newStory(){
		fullReport.append("-------------------------------------------------------------------------------\n");
		fullReport.append("-------------------------------------------------------------------------------\n");
	}
}
