package org.cumulus4j.keyserver.front.webapp;

import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

import org.cumulus4j.keyserver.front.shared.Auth;
import org.cumulus4j.keyserver.front.shared.Error;
import org.cumulus4j.keyserver.front.shared.OpenSessionResponse;
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

	@Context
	private AppServerManager appServerManager;

	@Path("{appServerID}/open")
	@GET
	public OpenSessionResponse open_GET(@PathParam("appServerID") String appServerID)
	{
		return open(appServerID);
	}

	@Path("{appServerID}/open")
	@POST
	public OpenSessionResponse open(@PathParam("appServerID") String appServerID)
	{
		Auth auth = getAuth();
		try {
			SessionManager sessionManager = getSessionManager(appServerID);

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

	private SessionManager getSessionManager(String appServerID) {
		AppServer appServer = appServerManager.getAppServerForAppServerID(appServerID);
		if (appServer == null)
			throw new WebApplicationException(Response.status(Status.NOT_FOUND).entity(new Error("There is no AppServer with appServerID=\"" + appServerID + "\"!")).build());

		return appServer.getSessionManager();
	}

	@Path("{appServerID}/unlock/{cryptoSessionID}")
	@GET
	public void unlock_GET(@PathParam("appServerID") String appServerID, @PathParam("cryptoSessionID") String cryptoSessionID)
	{
		unlock(appServerID, cryptoSessionID);
	}

	@Path("{appServerID}/unlock/{cryptoSessionID}")
	@POST
	public void unlock(@PathParam("appServerID") String appServerID, @PathParam("cryptoSessionID") String cryptoSessionID)
	{
		SessionManager sessionManager = getSessionManager(appServerID);
		Session session = sessionManager.getSessionForCryptoSessionID(cryptoSessionID);
		if (session == null)
			throw new WebApplicationException(Response.status(Status.NOT_FOUND).entity(new Error("There is no session with cryptoSessionID='" + cryptoSessionID + "'!")).build());

		session.setLocked(false);
	}

	@Path("{appServerID}/lock/{cryptoSessionID}")
	@GET
	public void lock_GET(@PathParam("appServerID") String appServerID, @PathParam("cryptoSessionID") String cryptoSessionID)
	{
		lock(appServerID, cryptoSessionID);
	}

	@Path("{appServerID}/lock/{cryptoSessionID}")
	@POST
	public void lock(@PathParam("appServerID") String appServerID, @PathParam("cryptoSessionID") String cryptoSessionID)
	{
		SessionManager sessionManager = getSessionManager(appServerID);
		Session session = sessionManager.getSessionForCryptoSessionID(cryptoSessionID);
		if (session == null)
			throw new WebApplicationException(Response.status(Status.NOT_FOUND).entity(new Error("There is no session with cryptoSessionID='" + cryptoSessionID + "'!")).build());

		session.setLocked(true);
	}

	@Path("{appServerID}/{cryptoSessionID}")
	@DELETE
	public void close(@PathParam("appServerID") String appServerID, @PathParam("cryptoSessionID") String cryptoSessionID)
	{
		logger.debug("close: appServerID='{}' cryptoSessionID='{}'", appServerID, cryptoSessionID);

		SessionManager sessionManager = getSessionManager(appServerID);
		Session session = sessionManager.getSessionForCryptoSessionID(cryptoSessionID);
		if (session != null)
			session.close();
	}
}
