package org.cumulus4j.integrationtest.gwt.client;

import com.google.gwt.core.client.EntryPoint;
import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.json.client.JSONObject;
import com.google.gwt.json.client.JSONString;
import com.google.gwt.json.client.JSONValue;
import com.google.gwt.user.client.Random;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.FlexTable;
import com.google.gwt.user.client.ui.Panel;
import com.google.gwt.user.client.ui.PasswordTextBox;
import com.google.gwt.user.client.ui.RootPanel;
import com.google.gwt.user.client.ui.TextArea;
import com.google.gwt.user.client.ui.TextBox;

/**
 * Entry point classes define <code>onModuleLoad()</code>.
 *
 * @author Jan Morlock - jan dot morlock at googlemail dot com
 */
public class Sample implements EntryPoint {

	/**
	 * Text box containing the user name.
	 */
	private TextBox usernameTextBox;

	/**
	 * Text box containing the password.
	 */
	private PasswordTextBox passwordTextBox;

	/**
	 * Start test button.
	 */
	private Button button;

	/**
	 * Text area used for displaying the status.
	 */
	private TextArea textArea;

	/**
	 * Constant used to generate the keystore id.
	 */
	private static final int RANDOM_UPPER_BOUND = 900000000;

	/**
	 * Constant used to generate the keystore id.
	 */
	private static final int RANDOM_OFFSET = 100000000;

	/**
	 * Dummy keystore id.
	 */
	private String keyStoreId = "keyStoreID."
			+ String.valueOf(Random.nextInt(RANDOM_UPPER_BOUND) + RANDOM_OFFSET);

	/**
	 * Url of the key manager.
	 */
	private String keyManagerUrl = "http://localhost:8686/org.cumulus4j.keymanager.front.webapp";

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

	/**
	 * Application service.
	 */
	private MovieServiceAsync movieService = GWT.create(MovieService.class);

	/**
	 * This request is directed to the key server. It is the initial request.
	 * Its general purpose is to init the keystore.
	 *
	 * @return New request object
	 */
	private RESTRequest getInitRequest() {
		RESTRequest request = new RESTRequest();

		JSONObject data = new JSONObject();

		JSONValue keyActivityPeriodMSecVal = new JSONString(
				keyActivityPeriodMSec);
		JSONValue keyStorePeriodMSecVal = new JSONString(keyStorePeriodMSec);
		data.put("keyActivityPeriodMSec", keyActivityPeriodMSecVal);
		data.put("keyStorePeriodMSec", keyStorePeriodMSecVal);

		request.setData(data);
		request.setUsername(usernameTextBox.getValue());
		request.setPassword(passwordTextBox.getValue());

		request.setUrl(keyManagerUrl + "/DateDependentKeyStrategy/"
				+ keyStoreId + "/init");

		return request;
	}

	/**
	 * This request is directed to the key server. It is the second request. Its
	 * general purpose is to tell the keyserver the application server's key
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
		request.setUsername(usernameTextBox.getValue());
		request.setPassword(passwordTextBox.getValue());

		request.setUrl(keyManagerUrl + "/AppServer/" + keyStoreId);

		return request;
	}

	/**
	 * Create the final lock request.
	 *
	 * @return New request object
	 */
	private RESTRequest getLockRequest() {
		RESTRequest request = new RESTRequest();

		request.setData(new JSONObject());
		request.setUsername(usernameTextBox.getValue());
		request.setPassword(passwordTextBox.getValue());

		request.setUrl(keyManagerUrl + "/CryptoSession/" + keyStoreId + "/"
				+ appServerID + "/" + cryptoSessionID + "/release");

		return request;
	}

	/**
	 * Create the crypto session rest request.
	 *
	 * @return New request object
	 */
	private RESTRequest getCryptoSessionRequest() {
		RESTRequest request = new RESTRequest();

		request.setUsername(usernameTextBox.getValue());
		request.setPassword(passwordTextBox.getValue());

		// I don't like the following line
		// However if we send null, the request fails somewhere
		request.setData(new JSONObject());

		request.setUrl(keyManagerUrl + "/CryptoSession/" + keyStoreId + "/"
				+ appServerID + "/acquire");

		return request;
	}

	/**
	 * Row ordering inside the control panel.
	 */
	enum ROWS {

		/**
		 * Row ordering.
		 */
		USERNAME, PASSWORD, BUTTON, TEXTAREA
	};

	/**
	 * Create the control panel.
	 *
	 * @return New control panel
	 */
	private Panel getControlPanel() {
		usernameTextBox = new TextBox();
		usernameTextBox.setValue("devil");

		passwordTextBox = new PasswordTextBox();
		passwordTextBox.setValue("testtesttest");

		button = new Button("Start Test");

		textArea = new TextArea();
		textArea.setEnabled(false);

		FlexTable flexTable = new FlexTable();

		flexTable.setText(ROWS.USERNAME.ordinal(), 0, "Username:");
		flexTable.setWidget(ROWS.USERNAME.ordinal(), 1, usernameTextBox);

		flexTable.setText(ROWS.PASSWORD.ordinal(), 0, "Password:");
		flexTable.setWidget(ROWS.PASSWORD.ordinal(), 1, passwordTextBox);

		flexTable.getFlexCellFormatter()
				.setColSpan(ROWS.BUTTON.ordinal(), 0, 2);
		flexTable.setWidget(ROWS.BUTTON.ordinal(), 0, button);
		flexTable.getFlexCellFormatter().setColSpan(ROWS.TEXTAREA.ordinal(), 0,
				2);
		flexTable.setWidget(ROWS.TEXTAREA.ordinal(), 0, textArea);

		return flexTable;
	}

	/**
	 * Append a new message in the text area.
	 *
	 * @param message
	 *            Message to append
	 */
	private void log(final String message) {
		String currentText = textArea.getText();
		if (currentText.equals("")) {
			currentText = message;
		} else {
			currentText += "\n" + message;
		}
		textArea.setText(currentText);
	}

	/**
	 * Perform the crypto session request. In case of success call the
	 * application server request.
	 */
	private void doCryptoSessionRequest() {
		RESTRequest request = getCryptoSessionRequest();
		log("POST " + request.getUrl());

		RESTRequestFactory.callREST(request, new RESTCallback() {
			@Override
			public void onSuccess(final JSONObject val) {
				JSONValue value = val.get("cryptoSessionID");
				cryptoSessionID = value.isString().stringValue();
				log("Success: " + val);

				log("Doing actual work (app server request) now...");
				doAppServerRequest();
			}

			@Override
			public void onError(final String errorResponse) {
				log("Error: " + errorResponse);
			}
		});
	}

	/**
	 * Perform the app server request.
	 */
	protected final void doAppServerRequest() {
		log("app server service call (cryptoSessionID = " + cryptoSessionID
				+ ")");
		movieService.fetchSomeMovies(cryptoSessionID,
				new AsyncCallback<String>() {

					@Override
					public void onSuccess(final String result) {
						log("Success: " + result);
						log("Actual work (app server request) done.");
						doLockRequest();
					}

					@Override
					public void onFailure(final Throwable caught) {
						log("Error: " + caught.toString());
					}
				});
	}

	/**
	 * Perform the second request: tell the key server which application server
	 * interface to use.
	 */
	private void doNotificationRequest() {
		RESTRequest request = getNotificationRequest();
		log("POST " + request.getUrl());

		RESTRequestFactory.callREST(request, new RESTCallback() {
			@Override
			public void onSuccess(final JSONObject val) {
				JSONValue value = val.get("appServerID");
				appServerID = value.isString().stringValue();
				log("Success: " + val);
				doCryptoSessionRequest();
			}

			@Override
			public void onError(final String errorResponse) {
				log("Error: " + errorResponse);
			}
		});
	}

	/**
	 * Perform the init request.
	 */
	private void doInitRequest() {
		RESTRequest request = getInitRequest();

		log("Starting with username " + request.getUsername());
		log("POST " + request.getUrl());

		RESTRequestFactory.callREST(request, new RESTCallback() {
			@Override
			public void onSuccess(final JSONObject val) {
				log("Success: " + val);
				doNotificationRequest();
			}

			@Override
			public void onError(final String errorResponse) {
				log("Error: " + errorResponse);
			}
		});
	}

	/**
	 * Perform the lock request.
	 */
	private void doLockRequest() {
		RESTRequest request = getLockRequest();

		log("POST " + request.getUrl());

		RESTRequestFactory.callREST(request, new RESTCallback() {
			@Override
			public void onSuccess(final JSONObject val) {
				log("Success: " + val);
				log("Session is locked.");
				log("All done successfully.");
			}

			@Override
			public void onError(final String errorResponse) {
				log("Error: " + errorResponse);
			}
		});
	}

	/**
	 * Construct the control panel.
	 */
	@Override
	public final void onModuleLoad() {
		RootPanel.get("contentPanel").add(getControlPanel());

		button.addClickHandler(new ClickHandler() {

			@Override
			public void onClick(final ClickEvent event) {
				doInitRequest();
			}
		});
	}
}
