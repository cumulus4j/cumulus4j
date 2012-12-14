package org.polepos.reporters;

import java.io.File;

import org.polepos.Settings;

public class Cumulus4jReporterFactory {

	public static Reporter[] getCumulus4jReporters() {

		if (Settings.DEBUG)
			System.out.println("Output path: " + getOutputPath());

		return new Reporter[]{
				new CustomBarPDFReporter(getOutputPath()),
				new HTMLReporter(subfolderPath(getOutputPath(), "html")),
		};
	}

	public static String getOutputPath() {
		return new File(System.getProperty("polepos.result.dir", "doc/results")).getAbsolutePath();
	}

	public static String subfolderPath(String root, String subfolder) {
		return new File(new File(root), subfolder).getAbsolutePath();
	}
}
