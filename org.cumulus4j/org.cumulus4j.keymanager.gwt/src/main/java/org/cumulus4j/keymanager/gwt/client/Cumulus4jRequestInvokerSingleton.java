package org.cumulus4j.keymanager.gwt.client;

public final class Cumulus4jRequestInvokerSingleton {
	
	private static final Cumulus4jRequestInvoker cumulus4jRequestInvokerInstance =
			new Cumulus4jRequestInvoker();
	
	private Cumulus4jRequestInvokerSingleton() {
		
	}
	
	public static Cumulus4jRequestInvoker getCumulus4jRequestInvoker() {
		return cumulus4jRequestInvokerInstance;
	}
}
