/*
 * Cumulus4j - Securing your data in the cloud - http://cumulus4j.org
 * Copyright (C) 2011 NightLabs Consulting GmbH
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.cumulus4j.keymanager.front.webapp;

import java.io.IOException;

import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

import org.cumulus4j.keymanager.AppServer;
import org.cumulus4j.keymanager.AppServerManager;
import org.cumulus4j.keymanager.front.shared.Error;
import org.cumulus4j.keymanager.front.shared.PutAppServerResponse;
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

	@GET
	@Path("{keyStoreID}/{appServerID}")
	public org.cumulus4j.keymanager.front.shared.AppServer getAppServer(
			@PathParam("keyStoreID") String keyStoreID,
			@PathParam("appServerID") String appServerID
	)
	{
		logger.debug("getAppServer: entered");
		Auth auth = authenticate(keyStoreID);
		try {
			AppServerManager appServerManager = keyStoreManager.getAppServerManager(keyStoreID);
			AppServer appServer = appServerManager.getAppServerForAppServerID(appServerID);
			if (appServer == null)
				return null;
			else {
				org.cumulus4j.keymanager.front.shared.AppServer as = new org.cumulus4j.keymanager.front.shared.AppServer();
				as.setAppServerID(appServer.getAppServerID());
				as.setAppServerBaseURL(appServer.getAppServerBaseURL());
				return as;
			}
		} catch (IOException e) {
			throw new WebApplicationException(Response.status(Status.INTERNAL_SERVER_ERROR).entity(new Error(e)).build());
		} finally {
			auth.clear();
		}
	}

	@GET
	@Path("{keyStoreID}")
	public org.cumulus4j.keymanager.front.shared.AppServerList getAppServers(@PathParam("keyStoreID") String keyStoreID)
	{
		logger.debug("getAppServers: entered");
		org.cumulus4j.keymanager.front.shared.AppServerList appServerList = new org.cumulus4j.keymanager.front.shared.AppServerList();
		Auth auth = authenticate(keyStoreID);
		try {
			AppServerManager appServerManager = keyStoreManager.getAppServerManager(keyStoreID);
			for (AppServer appServer : appServerManager.getAppServers()) {
				org.cumulus4j.keymanager.front.shared.AppServer as = new org.cumulus4j.keymanager.front.shared.AppServer();
				as.setAppServerID(appServer.getAppServerID());
				as.setAppServerBaseURL(appServer.getAppServerBaseURL());
				appServerList.getAppServers().add(as);
			}
		} catch (IOException e) {
			throw new WebApplicationException(Response.status(Status.INTERNAL_SERVER_ERROR).entity(new Error(e)).build());
		} finally {
			auth.clear();
		}
		return appServerList;
	}

	/**
	 * @deprecated This service method is not used by the unified key manager API. Shall we remove it?! It exists solely for
	 * reasons of REST-ful service consistency. But maybe we should better remove it and provide ONE single way to handle things. Marco :-)
	 */
	@Deprecated
	@PUT
	@Path("{keyStoreID}/{appServerID}")
	public void putAppServerWithAppServerIDPath(
			@PathParam("keyStoreID") String keyStoreID,
			@PathParam("appServerID") String appServerID,
			org.cumulus4j.keymanager.front.shared.AppServer appServer
	)
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

		putAppServer(keyStoreID, appServer);
	}

	@PUT
	@Path("{keyStoreID}")
	public PutAppServerResponse putAppServer(
			@PathParam("keyStoreID") String keyStoreID,
			org.cumulus4j.keymanager.front.shared.AppServer appServer
	)
	{
		logger.debug("putAppServer: entered");

		if (appServer == null)
			throw new WebApplicationException(Response.status(Status.BAD_REQUEST).entity(new Error("Missing request-entity!")).build());

		Auth auth = authenticate(keyStoreID);
		try {
			AppServerManager appServerManager = keyStoreManager.getAppServerManager(keyStoreID);
			AppServer as = new AppServer(appServerManager, appServer.getAppServerID(), appServer.getAppServerBaseURL());
			appServerManager.putAppServer(as); // This will assign appServer.appServerID, if that property is null.

			if (as.getAppServerID() == null) // sanity check.
				throw new WebApplicationException(Response.status(Status.INTERNAL_SERVER_ERROR).entity(new Error(new IllegalStateException("appServer.appServerID is null after registration of appServer!"))).build());

			// TODO write AppServers to a file!
			return new PutAppServerResponse(as.getAppServerID());
		} catch (IOException e) {
			throw new WebApplicationException(Response.status(Status.INTERNAL_SERVER_ERROR).entity(new Error(e)).build());
		} finally {
			// extra safety => overwrite passwords
			auth.clear();
		}
	}

	@DELETE
	@Path("{keyStoreID}/{appServerID}")
	public void deleteAppServer(@PathParam("keyStoreID") String keyStoreID, @PathParam("appServerID") String appServerID)
	{
		logger.debug("deleteAppServer: entered");

		Auth auth = authenticate(keyStoreID);
		try {
			AppServerManager appServerManager = keyStoreManager.getAppServerManager(keyStoreID);
			appServerManager.removeAppServer(appServerID);
		} catch (IOException e) {
			throw new WebApplicationException(Response.status(Status.INTERNAL_SERVER_ERROR).entity(new Error(e)).build());
		} finally {
			// extra safety => overwrite password
			auth.clear();
		}
	}
}
