package org.cumulus4j.keyserver.front.webapp;

import java.io.IOException;
import java.util.Arrays;
import java.util.Set;

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

import org.cumulus4j.keyserver.front.shared.Auth;
import org.cumulus4j.keyserver.front.shared.Error;
import org.cumulus4j.keyserver.front.shared.User;
import org.cumulus4j.keyserver.front.shared.UserList;
import org.cumulus4j.keyserver.front.shared.UserWithPassword;
import org.cumulus4j.keystore.CannotDeleteLastUserException;
import org.cumulus4j.keystore.KeyStore;
import org.cumulus4j.keystore.AuthenticationException;
import org.cumulus4j.keystore.UserAlreadyExistsException;
import org.cumulus4j.keystore.UserNotFoundException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Marco หงุ่ยตระกูล-Schulze - marco at nightlabs dot de
 */
@Path("user")
@Consumes({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
@Produces({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
public class UserService extends AbstractService
{
	private static final Logger logger = LoggerFactory.getLogger(UserService.class);

	public UserService() {
		logger.info("logger: instantiated UserService");
	}

	@Context
	private KeyStore keyStore;

	@GET
	@Path("{userName}")
	public User getUser(@PathParam("userName") String userName)
	{
		logger.debug("getUser: entered");
		Auth auth = getAuth();
		try {
			if (keyStore.getUsers(auth.getUserName(), auth.getPassword()).contains(userName))
				return new User(userName);
			else
				return null;
		} catch (AuthenticationException e) {
			throw new WebApplicationException(Response.status(Status.FORBIDDEN).entity(new Error(e)).build());
		} finally {
			auth.clear();
		}
	}

	@GET
	public UserList getUsers()
	{
		logger.debug("getUsers: entered");
		UserList userList = new UserList();
		Auth auth = getAuth();
		try {
			Set<String> userNames = keyStore.getUsers(auth.getUserName(), auth.getPassword());
			for (String userName : userNames) {
				userList.getUsers().add(new User(userName));
			}
		} catch (AuthenticationException e) {
			throw new WebApplicationException(Response.status(Status.FORBIDDEN).entity(new Error(e)).build());
		} finally {
			auth.clear();
		}
		return userList;
	}

	@PUT
	@Path("{userName}")
	public void putUserWithUserNamePath(@PathParam("userName") String userName, UserWithPassword userWithPassword)
	{
		logger.debug("putUserWithUserNamePath: entered");

		if (userName == null)
			throw new IllegalArgumentException("How the hell can userName be null?!");

		if (userWithPassword == null)
			throw new WebApplicationException(Response.status(Status.BAD_REQUEST).entity(new Error("Missing request-entity!")).build());

		if (!userName.equals(userWithPassword.getUserName()))
			throw new WebApplicationException(Response.status(Status.BAD_REQUEST).entity(new Error("Path's userName='" + userName + "' does not match entity's userName='" + userWithPassword.getUserName() + "'!")).build());

		putUser(userWithPassword);
	}

	@PUT
	public void putUser(UserWithPassword userWithPassword)
	{
		logger.debug("putUser: entered");

		if (userWithPassword == null)
			throw new WebApplicationException(Response.status(Status.BAD_REQUEST).entity(new Error("Missing request-entity!")).build());

		Auth auth = getAuth();

		try {
			try {
				keyStore.createUser(
						auth.getUserName(), auth.getPassword(),
						userWithPassword.getUserName(), userWithPassword.getPassword()
				);
			} catch (UserAlreadyExistsException e) {
				try {
					keyStore.changeUserPassword(
							auth.getUserName(), auth.getPassword(),
							userWithPassword.getUserName(), userWithPassword.getPassword()
					);
				} catch (UserNotFoundException e1) {
					logger.error("Why does it not exist? Has the user just been deleted?!", e1);
					throw new WebApplicationException(Response.status(Status.INTERNAL_SERVER_ERROR).build());
				}
			}
		} catch (AuthenticationException e) {
			throw new WebApplicationException(Response.status(Status.FORBIDDEN).entity(new Error(e)).build());
		} catch (IOException e) {
			throw new WebApplicationException(Response.status(Status.INTERNAL_SERVER_ERROR).entity(new Error(e)).build());
		} finally {
			// extra safety => overwrite passwords
			auth.clear();

			if (userWithPassword.getPassword() != null)
				Arrays.fill(userWithPassword.getPassword(), (char)0);
		}
	}

	@DELETE
	@Path("{userName}")
	public void deleteUser(@PathParam("userName") String userName)
	{
		logger.debug("deleteUser: entered");

		Auth auth = getAuth();

		try {
			keyStore.deleteUser(auth.getUserName(), auth.getPassword(), userName);
		} catch (AuthenticationException e) {
			throw new WebApplicationException(Response.status(Status.FORBIDDEN).entity(new Error(e)).build());
		} catch (UserNotFoundException e) {
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
