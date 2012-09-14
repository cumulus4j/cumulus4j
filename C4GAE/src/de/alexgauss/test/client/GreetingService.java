package de.alexgauss.test.client;

import com.google.gwt.user.client.rpc.RemoteService;
import com.google.gwt.user.client.rpc.RemoteServiceRelativePath;
/**
 * The client side stub for the RPC service.
 */
@RemoteServiceRelativePath("greet")
public interface GreetingService extends RemoteService {
	String greetServer(String name) throws IllegalArgumentException;
	void saveTestData(String fName, String lName) throws IllegalArgumentException;
	
	String getTestData() throws IllegalArgumentException;
}
