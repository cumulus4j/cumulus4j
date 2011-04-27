package org.cumulus4j.keyserver.front.webapp;

import java.util.UUID;

import javax.ws.rs.Consumes;
import javax.ws.rs.FormParam;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Path("session")
public class SessionService
{
	private static final Logger logger = LoggerFactory.getLogger(SessionService.class);

	@Context
	SessionManager sessionManager;

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
		logger.debug("open: userName={}", userName);
		logger.debug("open: sessionManager={}", sessionManager);

		return UUID.randomUUID().toString();
	}

}
