package org.cumulus4j.keymanager.gwt.client;

import java.util.HashMap;

import com.google.gwt.core.client.JavaScriptObject;
import com.google.gwt.json.client.JSONObject;
import com.google.gwt.user.client.Random;

/**
 * Perform REST requests using jQuery as core library.
 * 
 * @author Jan Morlock - jan dot morlock at googlemail dot com
 */
final class RESTRequestFactory {
	
	/**
	 * Default http method to use for requests.
	 */
	private static final String DEFAULT_HTTP_METHOD = "POST";
	
	/**
	 * RESTRequestFactory singleton.
	 */
	private static final RESTRequestFactory INSTANCE = new RESTRequestFactory();
	
	/**
	 * Container for all requests which are currently running.
	 */
	private final HashMap<String, RESTCallback> callbackMap = new HashMap<String, RESTCallback>();
	
	static {
		createJSFunctions(INSTANCE);
	}
	
	/**
	 * <p>
	 * The jquery_container defined in AxEasyMain.html contains two callback
	 * functions: doRESTCallback and doRESTErrorCallback. In the following
	 * function these are wired with the functions restCallback and
	 * restErrorCallback defined inside the parameter object.
	 * </p>
	 * <p>
	 * <b>Note:</b> This function is implemented via JSNI
	 * </p>
	 * 
	 * @param util
	 *            REST Request factory to wire
	 */
	private static native void createJSFunctions(final RESTRequestFactory util) /*-{
		$wnd.jquery_container.doRESTCallback = function(data, textStatus,
				xmlHttpRequest, id) {
			@com.google.gwt.core.client.GWT::log(Ljava/lang/String;)("RESTilitycreateJSFunctions:doRESTCallback");
			util.@org.cumulus4j.keymanager.gwt.client.RESTRequestFactory::restCallback(Lcom/google/gwt/core/client/JavaScriptObject;Ljava/lang/String;Lcom/google/gwt/core/client/JavaScriptObject;Ljava/lang/String;)(data, textStatus, xmlHttpRequest, id);
		}

		$wnd.jquery_container.doRESTErrorCallback = function(xmlHttpRequest,
				textStatus, errorThrown, id) {
			@com.google.gwt.core.client.GWT::log(Ljava/lang/String;)("RESTility:createJSFunctions:doRESTErrorCallback");
			util.@org.cumulus4j.keymanager.gwt.client.RESTRequestFactory::restErrorCallback(Lcom/google/gwt/core/client/JavaScriptObject;Ljava/lang/String;Ljava/lang/String;Ljava/lang/String;)(xmlHttpRequest, textStatus, errorThrown, id);
		}
	}-*/;
	
	/**
	 * Function called after a successful rest request.
	 * 
	 * @param data
	 *            Response data
	 * @param textStatus
	 *            Describes the request status
	 * @param xmlHttpRequest
	 *            Some request object
	 * @param id
	 *            Key of the corresponding REST callback saved inside the
	 *            callback map.
	 */
	public void restCallback(final JavaScriptObject data,
			final String textStatus, final JavaScriptObject xmlHttpRequest,
			final String id) {
		System.out.println("RESTRequestFactory:restCallback");
		JSONObject json = new JSONObject(data);

		RESTCallback callback = INSTANCE.callbackMap.remove(id);
		assert callback != null;

		callback.onSuccess(json);
	}
	
	/**
	 * Function called if the rest request failed.
	 * 
	 * @param xmlHttpRequest
	 *            Some request object
	 * @param textStatus
	 *            Describes the request status
	 * @param errorThrown
	 *            When an HTTP error occurs, errorThrown receives the textual
	 *            portion of the HTTP status, such as "Not Found" or
	 *            "Internal Server Error."
	 * @param id
	 *            Key of the corresponding REST callback saved inside the
	 *            callback map.
	 */
	public void restErrorCallback(final JavaScriptObject xmlHttpRequest,
			final String textStatus, final String errorThrown, final String id) {
		System.out.println("RESTRequestFactory:restErrorCallback");

		RESTCallback callback = INSTANCE.callbackMap.remove(id);
		assert callback != null;

		callback.onError(textStatus);
	}
	
	/**
	 * <p>
	 * Perform the actual request.
	 * </p>
	 * <p>
	 * <b>Note:</b> This function is implemented via JSNI
	 * </p>
	 * 
	 * @param url
	 *            Uniform resource locator to call
	 * @param data
	 *            Request context information
	 * @param method
	 *            HTTP method to use
	 * @param id
	 *            Key of the corresponding REST callback saved inside the
	 *            callback map.
	 * @param username
	 *            Username to be used for authentication request.
	 * @param password
	 *            Password to be used for authentication request.
	 */
	private static native void doCallREST(final String url, final String data,
			final String method, final String id, final String authUserName,
			final String authPassword)/*-{
		$wnd.jquery_container.callREST(url, data, method, id, authUserName,
				authPassword);
	}-*/;
	
	/**
	 * Perform a rest request.
	 * 
	 * @param request
	 *            Rest request object
	 * @param callback
	 *            Rest callback object
	 */
	public static void callREST(final RESTRequest request,
			final RESTCallback callback) {
		System.out.println("RESTility:callREST");

		// Generate an uid in order to guarantee that different requests at the
		// same time are not confused with each other and let us be a little bit
		// paranoid
		String id = String.valueOf(request.hashCode())
				+ String.valueOf(Random.nextInt());

		INSTANCE.callbackMap.put(id, callback);
		doCallREST(request.getUrl(), request.getData().toString(),
				DEFAULT_HTTP_METHOD, id, request.getAuthUserName(),
				request.getAuthPassword());
	}

}
