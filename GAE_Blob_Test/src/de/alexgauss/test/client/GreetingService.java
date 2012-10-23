package de.alexgauss.test.client;

import com.google.gwt.user.client.rpc.RemoteService;
import com.google.gwt.user.client.rpc.RemoteServiceRelativePath;
/**
 * The client side stub for the RPC service.
 */
@RemoteServiceRelativePath("greet")
public interface GreetingService extends RemoteService {
	
	String greetServer(String name) throws IllegalArgumentException;

	void saveArticle(String article_id) throws IllegalArgumentException;
	
	void saveOffer(String offer_id) throws IllegalArgumentException;
	
	String getArticleData(String article_id) throws IllegalArgumentException;
	
	String getOfferData() throws IllegalArgumentException;
	
}
