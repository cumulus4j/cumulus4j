package de.alexgauss.test.client;

import com.google.gwt.core.client.EntryPoint;
import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.HasHorizontalAlignment;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.Label;
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
		Button saveOfferButton = new Button("save offer");
		Button saveArticleButton = new Button("save article");
		Button loadOfferButton = new Button("load offer");
		Button loadArticleButton = new Button("load article");
		final TextBox firstName = new TextBox();
		final TextBox lastName = new TextBox();
		
		
		final TextBox offer_id_textbox = new TextBox();
		Label offer_id_label = new Label();
		offer_id_label.setText("Angebots-ID:");
		
		HorizontalPanel offer_id_panel = new HorizontalPanel();
		offer_id_panel.add(offer_id_label);
		offer_id_panel.add(offer_id_textbox);
		
		final TextBox article_id_textbox = new TextBox();
		Label article_id_label = new Label();
		article_id_label.setText("Artikel-ID:");
		
		HorizontalPanel article_id_panel = new HorizontalPanel();
		article_id_panel.add(article_id_label);
		article_id_panel.add(article_id_textbox);
		
		VerticalPanel buttonPanel = new VerticalPanel();
		buttonPanel.add(saveArticleButton);
		buttonPanel.add(saveOfferButton);
		buttonPanel.add(loadArticleButton);
		buttonPanel.add(loadOfferButton);
		
		VerticalPanel labelPanel = new VerticalPanel();
		labelPanel.setHorizontalAlignment(HasHorizontalAlignment.ALIGN_RIGHT);
		labelPanel.add(article_id_panel);
		labelPanel.add(offer_id_panel);
		
		saveOfferButton.addClickHandler(new ClickHandler(){
			@Override
			public void onClick(ClickEvent event) {
				//Check, if something was typed in.
				if(offer_id_textbox.getText().equals("")){
					Window.alert("Bitte eine Angebots-ID eingeben!");
					return;
				}
				
				greetingService.saveOffer(offer_id_textbox.getText(), new AsyncCallback<Void>(){

					@Override
					public void onFailure(Throwable caught) {
						Window.alert(caught.getMessage());
					}

					@Override
					public void onSuccess(Void result) {
						Window.alert("Speichern erfolgreich");
					}});
			}});
		
		saveArticleButton.addClickHandler(new ClickHandler(){
			@Override
			public void onClick(ClickEvent event) {
				//Check, if something was typed in.
				if(article_id_textbox.getText().equals("")){
					Window.alert("Bitte eine Artikel-ID eingeben!");
					return;
				}
				
				greetingService.saveArticle(article_id_textbox.getText(), new AsyncCallback<Void>(){

					@Override
					public void onFailure(Throwable caught) {
						Window.alert(caught.getMessage());
					}

					@Override
					public void onSuccess(Void result) {
						Window.alert("Speichern erfolgreich");
					}});
			}});
		
		loadArticleButton.addClickHandler(new ClickHandler(){

			@Override
			public void onClick(ClickEvent event) {
				//Check, if something was typed in.
				if(article_id_textbox.getText().equals("")){
					Window.alert("Bitte eine Artikel-ID eingeben!");
					return;
				}
				
				greetingService.getArticleData(article_id_textbox.getText(), new AsyncCallback<String>(){

					@Override
					public void onFailure(Throwable caught) {
						Window.alert(caught.getMessage());
					}

					@Override
					public void onSuccess(String result) {
						Window.alert(result);
					}});
			}});
		
		loadOfferButton.addClickHandler(new ClickHandler(){
			@Override
			public void onClick(ClickEvent event) {
				//Check, if something was typed in.
				if(offer_id_textbox.getText().equals("")){
					Window.alert("Bitte eine Angebots-ID eingeben!");
					return;
				}
				
				greetingService.getOfferData(offer_id_textbox.getText(), new AsyncCallback<String>(){

					@Override
					public void onFailure(Throwable caught) {
						Window.alert(caught.getMessage());
					}

					@Override
					public void onSuccess(String result) {
						Window.alert(result);
					}});
			}});
		
		RootPanel.get("dataContainer").add(labelPanel);
		RootPanel.get("buttonContainer").add(buttonPanel);
	}
}
