package org.polepos.reporters;

import java.io.File;

import org.polepos.Settings;

public class Cumulus4jReporterFactory {

	public static Reporter[] getCumulus4jReporters() {

		if (Settings.DEBUG)
			System.out.println("Output path: " + getOutputPath());

		return new Reporter[]{
				new CustomBarPDFReporter(getOutputPath()),
				new HTMLReporter(getOutputPath())
		};
	}

	public static String getOutputPath() {
		return new File("").getAbsolutePath();
	}

}
