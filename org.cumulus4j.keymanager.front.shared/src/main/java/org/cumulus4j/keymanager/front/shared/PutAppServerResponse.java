package org.cumulus4j.keymanager.front.shared;

import java.io.Serializable;

import javax.xml.bind.annotation.XmlRootElement;

/**
 * <p>
 * Response sent as confirmation when an {@link AppServer} was PUT into the key-server.
 * </p><p>
 * The <code>AppServer</code>'s {@link AppServer#getAppServerID() ID} might be <code>null</code> when the ID
 * should be assigned by the key-server (recommended), this response tells the client the app-server's ID
 * (no matter, if the client already assigned it and thus already knows it or whether the server assigned it).
 * </p><p>
 * The server sends this object instead of a simple {@link String} to (1) make later extension easier (e.g.
 * include additional meta-data like an expiry) and (2) to keep the API consistent. Furthermore it makes
 * using the Jersey client API easier, as it can always expect an XML (or JSON) result, no matter if it was
 * successful (appServerID returned as text/plain) or an exception occured (an {@link Error} is sent in XML
 * (or JSON) form).
 * </p>
 *
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
