package org.cumulus4j.keymanager.front.shared;

import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.annotation.XmlRootElement;

/**
 * @author Marco หงุ่ยตระกูล-Schulze - marco at nightlabs dot de
 */
@XmlRootElement
public class AppServerList
{
	private List<AppServer> appServers = new ArrayList<AppServer>();

	public List<AppServer> getAppServers() {
		return appServers;
	}
	public void setAppServers(List<AppServer> users) {
		this.appServers = users;
	}
}
