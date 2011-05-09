package org.cumulus4j.keyserver.front.webapp;

import java.net.URL;

import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

import org.cumulus4j.keymanager.AppServer;
import org.cumulus4j.keymanager.AppServerManager;
import org.cumulus4j.keyserver.front.shared.Auth;
import org.cumulus4j.keyserver.front.shared.Error;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Marco หงุ่ยตระกูล-Schulze - marco at nightlabs dot de
 */
@Path("AppServer")
@Consumes({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
@Produces({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
public class AppServerService extends AbstractService
{
	private static final Logger logger = LoggerFactory.getLogger(AppServerService.class);

	public AppServerService() {
		logger.info("logger: instantiated AppServerService");
	}

	@Context
	private AppServerManager appServerManager;

	@GET
	@Path("{appServerID}")
	public org.cumulus4j.keyserver.front.shared.AppServer getAppServer(@PathParam("appServerID") String appServerID)
	{
		logger.debug("getAppServer: entered");
		Auth auth = authenticate();
		try {
			AppServer appServer = appServerManager.getAppServerForAppServerID(appServerID);
			if (appServer == null)
				return null;
			else {
				org.cumulus4j.keyserver.front.shared.AppServer as = new org.cumulus4j.keyserver.front.shared.AppServer();
				as.setAppServerID(appServer.getAppServerID());
				as.setAppServerBaseURL(appServer.getAppServerBaseURL());
				return as;
			}
		} finally {
			auth.clear();
		}
	}

	@GET
	public org.cumulus4j.keyserver.front.shared.AppServerList getAppServers()
	{
		logger.debug("getAppServers: entered");
		org.cumulus4j.keyserver.front.shared.AppServerList appServerList = new org.cumulus4j.keyserver.front.shared.AppServerList();
		Auth auth = authenticate();
		try {
			for (AppServer appServer : appServerManager.getAppServers()) {
				org.cumulus4j.keyserver.front.shared.AppServer as = new org.cumulus4j.keyserver.front.shared.AppServer();
				as.setAppServerID(appServer.getAppServerID());
				as.setAppServerBaseURL(appServer.getAppServerBaseURL());
				appServerList.getAppServers().add(as);
			}
		} finally {
			auth.clear();
		}
		return appServerList;
	}

	@PUT
	@Path("{appServerID}")
	public void putAppServerWithAppServerIDPath(@PathParam("appServerID") String appServerID, org.cumulus4j.keyserver.front.shared.AppServer appServer)
	{
		logger.debug("putAppServerWithAppServerIDPath: entered");

		if (appServerID == null)
			throw new IllegalArgumentException("How the hell can appServerID be null?!");

		if (appServer == null)
			throw new WebApplicationException(Response.status(Status.BAD_REQUEST).entity(new Error("Missing request-entity!")).build());

		if (appServer.getAppServerID() == null || appServer.getAppServerID().isEmpty())
			appServer.setAppServerID(appServerID);
		else if (!appServerID.equals(appServer.getAppServerID()))
			throw new WebApplicationException(Response.status(Status.BAD_REQUEST).entity(new Error("Path's appServerID='" + appServerID + "' does not match entity's appServerID='" + appServer.getAppServerID() + "'!")).build());

		putAppServer(appServer);
	}

	@PUT
	@Produces(MediaType.TEXT_PLAIN)
	public String putAppServer(org.cumulus4j.keyserver.front.shared.AppServer appServer)
	{
		logger.debug("putAppServer: entered");

		if (appServer == null)
			throw new WebApplicationException(Response.status(Status.BAD_REQUEST).entity(new Error("Missing request-entity!")).build());

		if (appServer.getAppServerID() == null || appServer.getAppServerID().isEmpty()) {
			URL url = appServer.getAppServerBaseURL();
//			try {
//				url = new URL(appServer.getAppServerBaseURL());
//			} catch (MalformedURLException e) {
//				throw new WebApplicationException(Response.status(Status.BAD_REQUEST).entity(new Error(e)).build());
//			}

			AppServer oldAppServer;
			String id;
			int index = -1;
			do {
				id = url.getHost();
				if (url.getPort() < 0) {
					if (url.getDefaultPort() >= 0)
						id += '-' + url.getDefaultPort();
				}
				else
					id += '-' + url.getPort();

				if (++index > 0)
					id += '-' + index;

				oldAppServer = appServerManager.getAppServerForAppServerID(id);
			} while (oldAppServer != null && !appServer.getAppServerBaseURL().equals(oldAppServer.getAppServerBaseURL()));

			appServer.setAppServerID(id);
		}

		Auth auth = authenticate();
		try {
			AppServer as = new AppServer(appServerManager, appServer.getAppServerID(), appServer.getAppServerBaseURL());
			appServerManager.putAppServer(as);
			// TODO write AppServers to a file!
			return appServer.getAppServerID();
//		} catch (IOException e) {
//			throw new WebApplicationException(Response.status(Status.INTERNAL_SERVER_ERROR).entity(new Error(e)).build());
		} finally {
			// extra safety => overwrite passwords
			auth.clear();
		}
	}

	@DELETE
	@Path("{appServerID}")
	public void deleteAppServer(@PathParam("appServerID") String appServerID)
	{
		logger.debug("deleteAppServer: entered");

		Auth auth = authenticate();
		try {
			appServerManager.removeAppServer(appServerID);
//		} catch (IOException e) {
//			throw new WebApplicationException(Response.status(Status.INTERNAL_SERVER_ERROR).entity(new Error(e)).build());
		} finally {
			// extra safety => overwrite password
			auth.clear();
		}
	}
}
