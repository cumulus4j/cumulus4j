package org.cumulus4j.keymanager.cli.resource;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;

public class ResourceHelper {

	public static InputStream openLicenceAsStream()
	{
		String resourceName = "LICENCE.txt";
		InputStream inputStream = ResourceHelper.class.getResourceAsStream(resourceName);
		if (inputStream == null)
			throw new IllegalStateException("Resource \"" + resourceName + "\" could not be found!");

		return inputStream;
	}

	public static BufferedReader openLicenceAsBufferedReader()
	{
		try {
			return new BufferedReader(new InputStreamReader(openLicenceAsStream(), "UTF-8"));
		} catch (UnsupportedEncodingException e) {
			throw new RuntimeException(e); // should never happen, that "UTF-8" is unsupported!
		}
	}
}
