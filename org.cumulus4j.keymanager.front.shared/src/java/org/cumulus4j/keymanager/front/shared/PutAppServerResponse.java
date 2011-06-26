package org.cumulus4j.keymanager.front.shared;

import java.io.Serializable;

import javax.xml.bind.annotation.XmlRootElement;

/**
 * @author Marco หงุ่ยตระกูล-Schulze - marco at nightlabs dot de
 */
@XmlRootElement
public class PutAppServerResponse implements Serializable
{
	private static final long serialVersionUID = 1L;

	private String appServerID;

	public PutAppServerResponse() { }

	public PutAppServerResponse(String appServerID) {
		this.appServerID = appServerID;
	}

	public String getAppServerID() {
		return appServerID;
	}
	public void setAppServerID(String appServerID) {
		this.appServerID = appServerID;
	}
}
