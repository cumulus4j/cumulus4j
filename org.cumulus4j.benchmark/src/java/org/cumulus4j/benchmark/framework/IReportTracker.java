package org.cumulus4j.benchmark.framework;

/**
 * 
 * @author Jan Mortensen - jmortensen at nightlabs dot de
 *
 */
public interface IReportTracker<T> {
	
	/**
	 * Adds a new report to the list of reports.
	 * 
	 * @param report The report which should be added.
	 */
	public void addReport(T report);
	
	/**
	 * Returns a human readable String which contains all the reports added. 
	 * 
	 * @return The full report.
	 */
	public String getFullReport();
	
	/**
	 * To separate different parts of the benchmark you can call this method, 
	 * to introduce a new part.
	 */
	public void newStory(T storyName);
}
