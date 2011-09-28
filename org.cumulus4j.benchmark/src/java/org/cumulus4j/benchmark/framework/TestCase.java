package org.cumulus4j.benchmark.framework;

import java.util.ArrayList;
import java.util.List;

public class TestCase {

	private String serviceName;

	private List<String> invocationList;

	public TestCase(){

		invocationList = new ArrayList<String>();
		invocationList.add(IScenario.WARMUP);
	}

	public void setServiceName(String serviceName){

		this.serviceName = serviceName;
	}

	public String getServiceName(){

		return serviceName;
	}

	public void addInvocation(String invocation){
		invocationList.add(invocation);
	}

	public List<String> getInvokationList(){

		return invocationList;
	}
}
