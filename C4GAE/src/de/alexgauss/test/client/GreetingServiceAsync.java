package de.alexgauss.test.client;

import com.google.gwt.user.client.rpc.AsyncCallback;

/**
 * The async counterpart of <code>GreetingService</code>.
 */
public interface GreetingServiceAsync {
	
	void greetServer(String input, AsyncCallback<String> callback) throws IllegalArgumentException;

	void saveArticle(String article_id, AsyncCallback<Void> callback);
	
	void saveOffer(String offer_id, AsyncCallback<Void> callback);

	void getArticleData(String article_id, AsyncCallback<String> callback);
	
	void getOfferData(AsyncCallback<String> callback);
	
}
