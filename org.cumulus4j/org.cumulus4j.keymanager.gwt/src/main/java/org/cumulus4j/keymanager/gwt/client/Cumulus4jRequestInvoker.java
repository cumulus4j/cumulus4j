package org.cumulus4j.keymanager.gwt.client;

import com.google.gwt.json.client.JSONObject;
import com.google.gwt.json.client.JSONString;
import com.google.gwt.json.client.JSONValue;

public final class Cumulus4jRequestInvoker {
	
	/**
	 * User name used for authentication with the key server.
	 */
	private String authUserName;
	
	/**
	 * Password used for authentication with the key server.
	 */
	private String authPassword;
	
	/**
	 * Key store id.
	 */	
	private String keyStoreId;
		
	/**
	 * URL of the key manager.
	 */
	private String keyManagerURL = "http://localhost:8686/org.cumulus4j.keymanager.front.webapp";

	/**
	 * Location of the key manager interface of the application server.
	 */
	private String appServerBaseURL = "http://localhost:8888/org.cumulus4j.keymanager.back.webapp";
		
	/**
	 * Key activity period in microseconds.
	 */
	private String keyActivityPeriodMSec = "3600000";

	/**
	 * Key store period in microseconds.
	 */
	private String keyStorePeriodMSec = "86400000";

	/**
	 * Application server id.
	 */
	private String appServerID;
	
	/**
	 * Crypto session id.
	 */
	private String cryptoSessionID;
	
	
	public void setAuthUserName(String authUserName) {
		this.authUserName = authUserName;
	}	

	public void setAuthPassword(String authPassword) {
		this.authPassword = KeyServerPasswordCreator.modifyPassword(authPassword);
	}
	
	public void setKeyStoreId(String keyStoreId) {
		this.keyStoreId = keyStoreId;
	}
	
	public void setAppServerID(String appServerID) {
		this.appServerID = appServerID;
	}

	public void setCryptoSessionID(String cryptoSessionID) {
		this.cryptoSessionID = cryptoSessionID;
	}
	
	public void setAppServerBaseUrl(String appServerBaseURL) {
		this.appServerBaseURL = appServerBaseURL;
	}
	
	public void setKeyManagerURL(String keyManagerURL) {
		this.keyManagerURL = keyManagerURL;
	}

	/**
	 * This request is directed to the key server. It is the initial request.
	 * Its general purpose is to initialize the key store.
	 * 
	 * @param authUserName User name used for authentication with the key server.
	 * @param authPassword Password used for authentication with the key server.
	 * @return New request object
	 */
	private RESTRequest getKeyStoreInitRequest(final String authUserName,
			final String authPassword) {
		
		RESTRequest request = new RESTRequest();

		JSONObject data = new JSONObject();

		JSONValue keyActivityPeriodMSecVal = new JSONString(keyActivityPeriodMSec);
		JSONValue keyStorePeriodMSecVal = new JSONString(keyStorePeriodMSec);
		data.put("keyActivityPeriodMSec", keyActivityPeriodMSecVal);
		data.put("keyStorePeriodMSec", keyStorePeriodMSecVal);

		request.setData(data);
		request.setAuthUserName(authUserName);
		request.setAuthPassword(KeyServerPasswordCreator.modifyPassword(authPassword));
		
		request.setUrl(keyManagerURL + "/DateDependentKeyStrategy/"
				+ keyStoreId + "/init");

		return request;
		
	}
	
	/**
	 * Create a new user in the key store or update an existing one.
	 * 
	 * @param userName  User name of the user to be created in the key store.
	 * @param password Password of the user to be created in the key store.
	 * @return New request object
	 */	
	private RESTRequest getAddUserToKeyStoreRequest(final String userName,
			final String password) {
		
		RESTRequest request = new RESTRequest();
		
		JSONObject data = new JSONObject();
		
		JSONValue userNameVal = new JSONString(userName);
		JSONValue passwordVal = new JSONString(KeyServerPasswordCreator
				.modifyPassword(password));
		
		data.put("userName", userNameVal);
		data.put("password", passwordVal);
		
		request.setData(data);
		request.setAuthUserName(authUserName);
		request.setAuthPassword(authPassword);
		
		request.setUrl(keyManagerURL + "/User/" + keyStoreId);
		
		return request;		
	}
	
	/**
	 * This request is directed to the key server. Its general purpose 
	 * is to tell the key server the application server's key
	 * management interface (a rest interface).
	 * 
	 * @return New request object
	 */
	private RESTRequest getNotificationRequest() {
		
		RESTRequest request = new RESTRequest();

		JSONObject data = new JSONObject();
		
		JSONValue appServerBaseURLVal = new JSONString(appServerBaseURL);

		data.put("appServerBaseURL", appServerBaseURLVal);
		request.setData(data);
		
		request.setAuthUserName(authUserName);
		request.setAuthPassword(authPassword);

		request.setUrl(keyManagerURL + "/AppServer/" + keyStoreId);

		return request;
	}
		
	/**
	 * Create the crypto session rest request.
	 * 
	 * @return New request object
	 */
	private RESTRequest getAcquireRequest() {
		
		RESTRequest request = new RESTRequest();

		request.setData(new JSONObject());
		
		request.setAuthUserName(authUserName);
		request.setAuthPassword(authPassword);

		request.setUrl(keyManagerURL + "/CryptoSession/" + keyStoreId + "/"
				+ appServerID + "/acquire");

		return request;
	}
	
	/**
	 * Perform the crypto session request. 
	 */
	public void doAcquireRequest(RESTCallback callback) {

		RESTRequest request = getAcquireRequest();

		System.out.println("Acquiring crypto session....");
		System.out.println("POST " + request.getUrl());

		RESTRequestFactory.callREST(request, callback);
	}
	/**
	 * Create the final lock request.
	 * 
	 * @return New request object
	 */
	private RESTRequest getReleaseRequest() {
		
		RESTRequest request = new RESTRequest();
		
		request.setData(new JSONObject());
		
		request.setAuthUserName(authUserName);
		request.setAuthPassword(authPassword);

		request.setUrl(keyManagerURL + "/CryptoSession/" + keyStoreId + "/"
				+ appServerID + "/" + cryptoSessionID + "/release");

		return request;
	}
	
	/**
	 * Perform the key store initialization request.
	 * 
	 * @param authUserName User name used for authentication with the key server.
	 * @param authPassword Password used for authentication with the key server.
	 */
	public void doKeyStoreInitRequest(final String authUserName, final String authPassword, RESTCallback callback) {
		
		RESTRequest request = getKeyStoreInitRequest(authUserName, authPassword);

		System.out.println("Key store initialization.....");
		System.out.println("Starting with username " + request.getAuthUserName());
		System.out.println("POST " + request.getUrl());
		
		RESTRequestFactory.callREST(request, callback);
		
	}
	
	/**
	 * Perform the user creating/updating  request.
	 * 
	 * @param userName  User name of the user to be created in the key store.
	 * @param password Password of the user to be created in the key store.
	 */	
	public void doAddUserToKeyStoreRequest(final String userName, final String password) {
		
		RESTRequest request = getAddUserToKeyStoreRequest(userName, password);
		
		System.out.println("Creating/updating user .....");
		System.out.println("POST " + request.getUrl());
		
		RESTRequestFactory.callREST(request, new RESTCallback() {
			public void onSuccess(final JSONObject val) {
				System.out.println("Success: " + val);
			}

			public void onError(final String errorResponse) {
				System.out.println("Error: " + errorResponse);
			}
		});
		
	}
	
	/**
	 * Perform the notification request (on login): tell the key server which application server
	 * interface to use.
	 */
	public void doNotificationRequest(RESTCallback callback) {
		
		RESTRequest request = getNotificationRequest();
		
		System.out.println("Logging in....");
		System.out.println("Starting with username " + request.getAuthUserName());
		System.out.println("POST " + request.getUrl());
		
		RESTRequestFactory.callREST(request, callback);
		
	}
	
		
	/**
	 * Perform the lock request.
	 */
	public void doReleaseRequest() {
		
		RESTRequest request = getReleaseRequest();

		System.out.println("Releasing crypto session....");
		System.out.println("POST " + request.getUrl());

		RESTRequestFactory.callREST(request, new RESTCallback() {
			public void onSuccess(final JSONObject val) {
				System.out.println("Success: " + val);
				System.out.println("Session is locked.");
				System.out.println("All done successfully.");
			}

			public void onError(final String errorResponse) {
				System.out.println("Error: " + errorResponse);
			}
		});
		
	}
	
}
