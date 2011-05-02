package org.cumulus4j.keyserver.front.shared;

import java.io.Serializable;
import java.net.URL;

import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement
public class AppServer
implements Serializable
{
	private static final long serialVersionUID = 1L;

	public AppServer() { }

	public AppServer(AppServer source) {
		this.appServerID = source.getAppServerID();
		this.appServerBaseURL = source.getAppServerBaseURL();
	}

	private String appServerID;

	private URL appServerBaseURL;

	public String getAppServerID() {
		return appServerID;
	}
	public void setAppServerID(String appServerID) {
		this.appServerID = appServerID;
	}
	public URL getAppServerBaseURL() {
		return appServerBaseURL;
	}
	public void setAppServerBaseURL(URL appServerBaseURL) {
		this.appServerBaseURL = appServerBaseURL;
	}
}
