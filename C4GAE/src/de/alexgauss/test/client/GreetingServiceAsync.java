package de.alexgauss.test.client;

import com.google.gwt.user.client.rpc.AsyncCallback;

/**
 * The async counterpart of <code>GreetingService</code>.
 */
public interface GreetingServiceAsync {
	void greetServer(String input, AsyncCallback<String> callback)
			throws IllegalArgumentException;
	void saveTestData(String fName, String lName, AsyncCallback<Void> callback);

	void getTestData(AsyncCallback<String> callback);
}
