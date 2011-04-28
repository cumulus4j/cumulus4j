package org.cumulus4j.keyserver.front.webapp;

import javax.ws.rs.Consumes;
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
import org.cumulus4j.keyserver.front.shared.OpenSessionRequest;
import org.cumulus4j.keyserver.front.shared.OpenSessionResponse;
import org.cumulus4j.keystore.LoginException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Path("session")
@Consumes({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
@Produces({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
public class SessionService extends AbstractService
{
	private static final Logger logger = LoggerFactory.getLogger(SessionService.class);

	@Context
	private SessionManager sessionManager;

	@Path("open")
	@POST
	public OpenSessionResponse open(OpenSessionRequest openSessionRequest)
	{
		Auth auth = openSessionRequest.getAuth();
		validateAuth(auth);

		try {
			Session session;
			try {
				session = sessionManager.openSession(auth.getUserName(), auth.getPassword());
			} catch (LoginException e) {
				throw new WebApplicationException(Response.status(Status.FORBIDDEN).entity(new Error(e)).build());
			}

			OpenSessionResponse result = new OpenSessionResponse();
			result.setCryptoSessionID(session.getCryptoSessionID());
			return result;
		} finally {
			// extra safety => overwrite password
			auth.clear();
		}
	}

	@Path("close/{cryptoSessionID}")
	@POST
	@Consumes(MediaType.APPLICATION_FORM_URLENCODED)
	@Produces(MediaType.TEXT_PLAIN)
	public void close(@PathParam("cryptoSessionID") String cryptoSessionID)
	{
		Session session = sessionManager.getSessionForCryptoSessionID(cryptoSessionID);
		if (session != null)
			session.close();
	}

}
