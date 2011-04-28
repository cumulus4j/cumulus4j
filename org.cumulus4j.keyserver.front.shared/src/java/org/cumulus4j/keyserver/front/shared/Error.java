package org.cumulus4j.keyserver.front.shared;

import java.io.Serializable;

import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement
public class Error implements Serializable
{
	private static final long serialVersionUID = 1L;

	private String rootType;
	private String rootMessage;

	private String type;
	private String message;

	public Error() { }

	public Error(Throwable t) {
		this.type = t.getClass().getName();
		this.message = t.getMessage();

		Throwable r = t;
		while (r.getCause() != null)
			r = r.getCause();

		if (r != t) {
			this.rootType = r.getClass().getName();
			this.rootMessage = r.getMessage();
		}
	}

	public Error(String message) {
		this.message = message;
	}

	public String getRootType() {
		return rootType;
	}
	public void setRootType(String rootType) {
		this.rootType = rootType;
	}
	public String getRootMessage() {
		return rootMessage;
	}
	public void setRootMessage(String rootMessage) {
		this.rootMessage = rootMessage;
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
	public void setMessage(String message) {
		this.message = message;
	}
}
