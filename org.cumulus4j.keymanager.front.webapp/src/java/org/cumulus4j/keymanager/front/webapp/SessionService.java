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
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

import org.cumulus4j.keymanager.AppServer;
import org.cumulus4j.keymanager.AppServerManager;
import org.cumulus4j.keymanager.Session;
import org.cumulus4j.keymanager.SessionManager;
import org.cumulus4j.keymanager.front.shared.Error;
import org.cumulus4j.keymanager.front.shared.OpenSessionResponse;
import org.cumulus4j.keystore.AuthenticationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Marco หงุ่ยตระกูล-Schulze - marco at nightlabs dot de
 */
@Path("Session")
@Consumes({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
@Produces({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
public class SessionService extends AbstractService
{
	private static final Logger logger = LoggerFactory.getLogger(SessionService.class);

	@Path("{keyStoreID}/{appServerID}/open")
	@GET
	public OpenSessionResponse open_GET(@PathParam("keyStoreID") String keyStoreID, @PathParam("appServerID") String appServerID)
	{
		return open(keyStoreID, appServerID);
	}

	@Path("{keyStoreID}/{appServerID}/open")
	@POST
	public OpenSessionResponse open(@PathParam("keyStoreID") String keyStoreID, @PathParam("appServerID") String appServerID)
	{
		Auth auth = getAuth();
		try {
			SessionManager sessionManager = getSessionManager(keyStoreID, appServerID);

			Session session;
			try {
				session = sessionManager.openSession(auth.getUserName(), auth.getPassword());
			} catch (AuthenticationException e) {
				throw new WebApplicationException(Response.status(Status.FORBIDDEN).entity(new Error(e)).build());
			}

			logger.debug("open: authUserName='{}' cryptoSessionID='{}'", auth.getUserName(), session.getCryptoSessionID());

			OpenSessionResponse result = new OpenSessionResponse();
			result.setCryptoSessionID(session.getCryptoSessionID());
			result.setExpiry(session.getExpiry());
			return result;
		} finally {
			// extra safety => overwrite password
			auth.clear();
		}
	}

	private SessionManager getSessionManager(String keyStoreID, String appServerID)
	{
		AppServerManager appServerManager;
		try {
			appServerManager = keyStoreManager.getAppServerManager(keyStoreID);
		} catch (IOException e) {
			throw new WebApplicationException(Response.status(Status.INTERNAL_SERVER_ERROR).entity(new Error(e)).build());
		}

		AppServer appServer = appServerManager.getAppServerForAppServerID(appServerID);
		if (appServer == null)
			throw new WebApplicationException(Response.status(Status.NOT_FOUND).entity(new Error("There is no AppServer with appServerID=\"" + appServerID + "\"!")).build());

		return appServer.getSessionManager();
	}

	@Path("{keyStoreID}/{appServerID}/{cryptoSessionID}/unlock")
	@GET
	public void unlock_GET(@PathParam("keyStoreID") String keyStoreID, @PathParam("appServerID") String appServerID, @PathParam("cryptoSessionID") String cryptoSessionID)
	{
		unlock(keyStoreID, appServerID, cryptoSessionID);
	}

	@Path("{keyStoreID}/{appServerID}/{cryptoSessionID}/unlock")
	@POST
	public void unlock(@PathParam("keyStoreID") String keyStoreID, @PathParam("appServerID") String appServerID, @PathParam("cryptoSessionID") String cryptoSessionID)
	{
		SessionManager sessionManager = getSessionManager(keyStoreID, appServerID);
		Session session = sessionManager.getSessionForCryptoSessionID(cryptoSessionID);
		if (session == null)
			throw new WebApplicationException(Response.status(Status.NOT_FOUND).entity(new Error("There is no session with cryptoSessionID='" + cryptoSessionID + "'!")).build());

		session.setLocked(false);
	}

	@Path("{keyStoreID}/{appServerID}/{cryptoSessionID}/lock")
	@GET
	public void lock_GET(@PathParam("keyStoreID") String keyStoreID, @PathParam("appServerID") String appServerID, @PathParam("cryptoSessionID") String cryptoSessionID)
	{
		lock(keyStoreID, appServerID, cryptoSessionID);
	}

	@Path("{keyStoreID}/{appServerID}/{cryptoSessionID}/lock")
	@POST
	public void lock(@PathParam("keyStoreID") String keyStoreID, @PathParam("appServerID") String appServerID, @PathParam("cryptoSessionID") String cryptoSessionID)
	{
		SessionManager sessionManager = getSessionManager(keyStoreID, appServerID);
		Session session = sessionManager.getSessionForCryptoSessionID(cryptoSessionID);
		if (session == null)
			throw new WebApplicationException(Response.status(Status.NOT_FOUND).entity(new Error("There is no session with cryptoSessionID='" + cryptoSessionID + "'!")).build());

		session.setLocked(true);
	}

	@Path("{keyStoreID}/{appServerID}/{cryptoSessionID}")
	@DELETE
	public void close(@PathParam("keyStoreID") String keyStoreID, @PathParam("appServerID") String appServerID, @PathParam("cryptoSessionID") String cryptoSessionID)
	{
		logger.debug("close: appServerID='{}' cryptoSessionID='{}'", appServerID, cryptoSessionID);

		SessionManager sessionManager = getSessionManager(keyStoreID, appServerID);
		Session session = sessionManager.getSessionForCryptoSessionID(cryptoSessionID);
		if (session != null)
			session.close();
	}
}
