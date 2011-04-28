package org.cumulus4j.keyserver.front.webapp;

import java.io.IOException;
import java.util.Arrays;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;
import javax.ws.rs.core.SecurityContext;

import org.cumulus4j.keyserver.front.shared.Auth;
import org.cumulus4j.keyserver.front.shared.DeleteUserRequest;
import org.cumulus4j.keyserver.front.shared.Error;
import org.cumulus4j.keyserver.front.shared.PutUserRequest;
import org.cumulus4j.keyserver.front.shared.User;
import org.cumulus4j.keystore.CannotDeleteLastUserException;
import org.cumulus4j.keystore.KeyStore;
import org.cumulus4j.keystore.LoginException;
import org.cumulus4j.keystore.UserAlreadyExistsException;
import org.cumulus4j.keystore.UserDoesNotExistException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Path("user")
@Consumes({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
@Produces({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
public class UserService extends AbstractService
{
	private static final Logger logger = LoggerFactory.getLogger(UserService.class);

	@Context
	private KeyStore keyStore;

	@GET
	public User getUser(@Context ServletContext servletContext, @Context HttpServletRequest request, @Context SecurityContext sc)
	{
		// TODO how to implement this? Use sub-path? We cannot pass an entity in a GET request, AFAIK => use POST + sub-path? pass authUserName and authPassword via query arguments?
		// Search for a nice general solution and keep the whole API consistent.
		return null;
	}

	@PUT
	public void putUser(PutUserRequest putUserRequest)
	{
		Auth auth = putUserRequest.getAuth();
		validateAuth(auth);

		try {
			try {
				keyStore.createUser(
						auth.getUserName(), auth.getPassword(),
						putUserRequest.getUserName(), putUserRequest.getPassword()
				);
			} catch (UserAlreadyExistsException e) {
				try {
					keyStore.changeUserPassword(
							auth.getUserName(), auth.getPassword(),
							putUserRequest.getUserName(), putUserRequest.getPassword()
					);
				} catch (UserDoesNotExistException e1) {
					logger.error("Why does it not exist? Has the user just been deleted?!", e1);
					throw new WebApplicationException(Response.status(Status.INTERNAL_SERVER_ERROR).build());
				}
			}
		} catch (LoginException e) {
			throw new WebApplicationException(Response.status(Status.FORBIDDEN).entity(new Error(e)).build());
		} catch (IOException e) {
			throw new WebApplicationException(Response.status(Status.INTERNAL_SERVER_ERROR).entity(new Error(e)).build());
		} finally {
			// extra safety => overwrite passwords
			auth.clear();

			if (putUserRequest.getPassword() != null)
				Arrays.fill(putUserRequest.getPassword(), (char)0);
		}
	}

	@DELETE
	public void deleteUser(DeleteUserRequest deleteUserRequest)
	{
		Auth auth = deleteUserRequest.getAuth();
		validateAuth(auth);

		try {
			keyStore.deleteUser(auth.getUserName(), auth.getPassword(), deleteUserRequest.getUserName());
		} catch (LoginException e) {
			throw new WebApplicationException(Response.status(Status.FORBIDDEN).entity(new Error(e)).build());
		} catch (UserDoesNotExistException e) {
			// ignore in order to be idempotent - only warn
			logger.warn("deleteUser: " + e);
		} catch (CannotDeleteLastUserException e) {
			throw new WebApplicationException(Response.status(Status.FORBIDDEN).entity(new Error(e)).build());
		} catch (IOException e) {
			throw new WebApplicationException(Response.status(Status.INTERNAL_SERVER_ERROR).entity(new Error(e)).build());
		} finally {
			// extra safety => overwrite password
			auth.clear();
		}
	}
}
