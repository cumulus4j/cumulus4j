package de.alexgauss.test.client;

import com.google.gwt.core.client.EntryPoint;
import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.RootPanel;
import com.google.gwt.user.client.ui.TextBox;
import com.google.gwt.user.client.ui.VerticalPanel;

/**
 * Entry point classes define <code>onModuleLoad()</code>.
 */
public class C4GAE implements EntryPoint {
	/**
	 * The message displayed to the user when the server cannot be reached or
	 * returns an error.
	 */
	private static final String SERVER_ERROR = "An error occurred while "
			+ "attempting to contact the server. Please check your network "
			+ "connection and try again.";

	/**
	 * Create a remote service proxy to talk to the server-side Greeting service.
	 */
	private final GreetingServiceAsync greetingService = GWT
			.create(GreetingService.class);

	/**
	 * This is the entry point method.
	 */
	public void onModuleLoad() {
		Button saveButton = new Button("Speichere");
		Button loadButton = new Button("Lade");
		final TextBox firstName = new TextBox();
		final TextBox lastName = new TextBox();
		
		VerticalPanel mainPanel = new VerticalPanel();
		mainPanel.add(firstName);
		mainPanel.add(lastName);
		mainPanel.add(saveButton);
		mainPanel.add(loadButton);
		
		saveButton.addClickHandler(new ClickHandler(){
			@Override
			public void onClick(ClickEvent event) {
				//Check, if something was typed in.
				if(firstName.getText().equals("")){
					Window.alert("Bitte das erste Textfeld befüllen");
					return;
				}
				if(lastName.getText().equals("")){
					Window.alert("Bitte das zweite Textfeld befüllen");
					return;
				}
				greetingService.saveTestData(firstName.getText(), lastName.getText(), new AsyncCallback<Void>(){

					@Override
					public void onFailure(Throwable caught) {
						Window.alert(caught.getMessage());
					}

					@Override
					public void onSuccess(Void result) {
						Window.alert("Speichern erfolgreich");
					}});
			}});
		
		loadButton.addClickHandler(new ClickHandler(){
			@Override
			public void onClick(ClickEvent event) {
				greetingService.getTestData(new AsyncCallback<String>(){

					@Override
					public void onFailure(Throwable caught) {
						Window.alert(caught.getMessage());
					}

					@Override
					public void onSuccess(String result) {
						Window.alert(result);
					}});
			}});
		
		
		RootPanel.get("nameFieldContainer").add(mainPanel);
	}
}
