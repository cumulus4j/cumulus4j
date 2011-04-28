package org.cumulus4j.keyserver.front.webapp;

import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

import org.cumulus4j.keyserver.front.shared.Auth;
import org.cumulus4j.keyserver.front.shared.Error;

public abstract class AbstractService
{
	protected void validateAuth(Auth auth)
	{
		if (auth == null)
			throw new WebApplicationException(Response.status(Status.FORBIDDEN).entity(new Error("auth is missing!")).build());

		if (auth.getUserName() == null)
			throw new WebApplicationException(Response.status(Status.FORBIDDEN).entity(new Error("auth.userName is missing!")).build());

		if (auth.getPassword() == null)
			throw new WebApplicationException(Response.status(Status.FORBIDDEN).entity(new Error("auth.password is missing!")).build());
	}

}
