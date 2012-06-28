package org.cumulus4j.jee.test.ejb;

import javax.ejb.Stateless;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Stateless
public class SimpleTestBean implements SimpleTestRemote {
	private static final Logger logger = LoggerFactory.getLogger(SimpleTestBean.class);

	@Override
	public String test(String input) {
		logger.info("test: input='{}'", input);
		String output = input == null ? null : input.toUpperCase();
		logger.info("test: output='{}'", output);
		return output;
	}

}
