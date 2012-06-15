package org.cumulus4j.jee.test.ejb;

import javax.ejb.Stateless;

@Stateless
public class SimpleTestBean implements SimpleTestRemote {

	@Override
	public String test(String input) {
		System.out.println(String.format("SimpleTestBean.test: input='%s'", input));
		String output = input == null ? null : input.toUpperCase();
		System.out.println(String.format("SimpleTestBean.test: output='%s'", output));
		return output;
	}

}
