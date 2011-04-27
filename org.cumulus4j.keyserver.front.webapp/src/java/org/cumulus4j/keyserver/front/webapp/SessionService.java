package org.cumulus4j.keyserver.front.webapp;

import javax.ws.rs.Consumes;
import javax.ws.rs.FormParam;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

import org.cumulus4j.keystore.LoginException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Path("session")
public class SessionService
{
	private static final Logger logger = LoggerFactory.getLogger(SessionService.class);

	@Context
	private SessionManager sessionManager;

	@Path("open")
	@POST
	@Consumes(MediaType.APPLICATION_FORM_URLENCODED)
	@Produces(MediaType.TEXT_PLAIN)
	public String open_POST(@QueryParam("userName") @FormParam("userName") String userName, @QueryParam("password") @FormParam("password") String password)
	{
		return open(userName, password);
	}

	@Path("open")
	@GET
	@Produces(MediaType.TEXT_PLAIN)
	public String open_GET(@QueryParam("userName") String userName, @QueryParam("password") String password)
	{
		return open(userName, password);
	}

	protected String open(String userName, String password)
	{
		if (userName == null || userName.isEmpty())
			throw new WebApplicationException(Response.status(Status.FORBIDDEN).entity("Parameter 'userName' is missing!").build());

		if (password == null || password.isEmpty())
			throw new WebApplicationException(Response.status(Status.FORBIDDEN).entity("Parameter 'password' is missing!").build());

		logger.debug("open: userName={}", userName);
		logger.debug("open: sessionManager={}", sessionManager);

		Session session;
		try {
			session = sessionManager.openSession(userName, password.toCharArray());
		} catch (LoginException e) {
			throw new WebApplicationException(Response.status(Status.FORBIDDEN).entity(e.getMessage()).build());
		}
		return session.getCryptoSessionID();
	}

	@Path("close/{cryptoSessionID}")
	@POST
	@Consumes(MediaType.APPLICATION_FORM_URLENCODED)
	@Produces(MediaType.TEXT_PLAIN)
	public String close_POST(@PathParam("cryptoSessionID") String cryptoSessionID)
	{
		close(cryptoSessionID);
		return "OK";
	}

	@Path("close/{cryptoSessionID}")
	@GET
	@Produces(MediaType.TEXT_PLAIN)
	public String close_GET(@PathParam("cryptoSessionID") String cryptoSessionID)
	{
		close(cryptoSessionID);
		return "OK";
	}

	protected void close(String cryptoSessionID)
	{
		Session session = sessionManager.getSessionForCryptoSessionID(cryptoSessionID);
		if (session != null)
			session.close();
	}

}
