package org.cumulus4j.keymanager.back.shared;

import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement
public class ErrorResponse extends Response
{
	private static final long serialVersionUID = 1L;

	private String type;
	private String message;

	public ErrorResponse() { }

	public ErrorResponse(Request request, String errorMessage) {
		super(request);
		this.message = errorMessage;
	}

	public ErrorResponse(Request request, Throwable t) {
		super(request);
		this.type = t.getClass().getName();
		this.message = t.getMessage();
	}

	public String getType() {
		return type;
	}
	public void setType(String type) {
		this.type = type;
	}

	public String getMessage() {
		return message;
	}
	public void setMessage(String errorMessage) {
		this.message = errorMessage;
	}
}
