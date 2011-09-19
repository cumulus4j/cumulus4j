package org.cumulus4j.benchmark.framework;

public interface IReport {
	
//	public void setWarmupReport(String report);
//	
//	public void setStoreReport(String report);
//	
//	public void setReadReport(String report);
	
	public void addReport(String report);
	
	public String getFullReport();
}
