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
	private SessionManager sessionManager;

	@Path("open")
	@GET
	public OpenSessionResponse open_GET()
	{
		return open();
	}

	@Path("open")
	@POST
	public OpenSessionResponse open()
	{
		Auth auth = getAuth();
		try {
			Session session;
			try {
				session = sessionManager.openSession(auth.getUserName(), auth.getPassword());
			} catch (AuthenticationException e) {
				throw new WebApplicationException(Response.status(Status.FORBIDDEN).entity(new Error(e)).build());
			}

			logger.debug("open: authUserName='{}' cryptoSessionID='{}'", auth.getUserName(), session.getCryptoSessionID());

			OpenSessionResponse result = new OpenSessionResponse();
			result.setKeyServerID(sessionManager.getKeyServerID());
			result.setCryptoSessionID(session.getCryptoSessionID());
			result.setExpiry(session.getExpiry());
			return result;
		} finally {
			// extra safety => overwrite password
			auth.clear();
		}
	}


	@Path("unlock/{cryptoSessionID}")
	@GET
	public void unlock_GET(@PathParam("cryptoSessionID") String cryptoSessionID)
	{
		unlock(cryptoSessionID);
	}

	@Path("unlock/{cryptoSessionID}")
	@POST
	public void unlock(@PathParam("cryptoSessionID") String cryptoSessionID)
	{
		Session session = sessionManager.getSessionForCryptoSessionID(cryptoSessionID);
		if (session == null)
			throw new WebApplicationException(Response.status(Status.NOT_FOUND).entity(new Error("There is no session with cryptoSessionID='" + cryptoSessionID + "'!")).build());

		session.setLocked(false);
	}


	@Path("lock/{cryptoSessionID}")
	@GET
	public void lock_GET(@PathParam("cryptoSessionID") String cryptoSessionID)
	{
		lock(cryptoSessionID);
	}

	@Path("lock/{cryptoSessionID}")
	@POST
	public void lock(@PathParam("cryptoSessionID") String cryptoSessionID)
	{
		Session session = sessionManager.getSessionForCryptoSessionID(cryptoSessionID);
		if (session == null)
			throw new WebApplicationException(Response.status(Status.NOT_FOUND).entity(new Error("There is no session with cryptoSessionID='" + cryptoSessionID + "'!")).build());

		session.setLocked(true);
	}


	@Path("{cryptoSessionID}")
	@DELETE
	public void close(@PathParam("cryptoSessionID") String cryptoSessionID)
	{
		logger.debug("close: cryptoSessionID='{}'", cryptoSessionID);

		Session session = sessionManager.getSessionForCryptoSessionID(cryptoSessionID);
		if (session != null)
			session.close();
	}
}
