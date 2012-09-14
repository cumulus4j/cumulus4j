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
import java.util.Arrays;
import java.util.Set;

import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

import org.cumulus4j.keymanager.front.shared.Error;
import org.cumulus4j.keymanager.front.shared.User;
import org.cumulus4j.keymanager.front.shared.UserList;
import org.cumulus4j.keymanager.front.shared.UserWithPassword;
import org.cumulus4j.keystore.AuthenticationException;
import org.cumulus4j.keystore.CannotDeleteLastUserException;
import org.cumulus4j.keystore.KeyStore;
import org.cumulus4j.keystore.UserAlreadyExistsException;
import org.cumulus4j.keystore.UserNotFoundException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * REST service for user management.
 *
 * @author Marco หงุ่ยตระกูล-Schulze - marco at nightlabs dot de
 */
@Path("User")
@Consumes({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
@Produces({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
public class UserService extends AbstractService
{
	private static final Logger logger = LoggerFactory.getLogger(UserService.class);

	/**
	 * Create a new instance.
	 */
	public UserService() {
		logger.info("logger: instantiated UserService");
	}

	/**
	 * Get a {@link KeyStore}'s user identified by the given
	 * <code>keyStoreID</code> and <code>userName</code>.
	 * @param keyStoreID identifier of the {@link KeyStore} to work with.
	 * @param userName the user's name.
	 * @return the desired user or <code>null</code>, if there is no user with the given name
	 * in the specified <code>KeyStore</code>.
	 */
	@GET
	@Path("{keyStoreID}/{userName}")
	public User getUser(@PathParam("keyStoreID") String keyStoreID, @PathParam("userName") String userName)
	{
		logger.debug("getUser: entered");
		Auth auth = getAuth();
		try {
			KeyStore keyStore = keyStoreManager.getKeyStore(keyStoreID);
			if (keyStore.getUsers(auth.getUserName(), auth.getPassword()).contains(userName))
				return new User(userName);
			else
				return null;
		} catch (AuthenticationException e) {
			logger.debug("getUser: " + e, e); // debug, because not an internal error
			throw new WebApplicationException(Response.status(Status.FORBIDDEN).entity(new Error(e)).build());
		} catch (IOException e) {
			logger.error("getUser: " + e, e);
			throw new WebApplicationException(Response.status(Status.INTERNAL_SERVER_ERROR).entity(new Error(e)).build());
		} finally {
			auth.clear();
		}
	}

	/**
	 * Get all users of the {@link KeyStore} identified by <code>keyStoreID</code>.
	 * @param keyStoreID identifier of the {@link KeyStore} to work with.
	 * @return all users of the {@link KeyStore} identified by <code>keyStoreID</code>.
	 */
	@GET
	@Path("{keyStoreID}")
	public UserList getUsers(@PathParam("keyStoreID") String keyStoreID)
	{
		logger.debug("getUsers: entered");
		UserList userList = new UserList();
		Auth auth = getAuth();
		try {
			KeyStore keyStore = keyStoreManager.getKeyStore(keyStoreID);
			Set<String> userNames = keyStore.getUsers(auth.getUserName(), auth.getPassword());
			for (String userName : userNames) {
				userList.getUsers().add(new User(userName));
			}
		} catch (AuthenticationException e) {
			logger.debug("getUsers: " + e, e); // debug, because not an internal error
			throw new WebApplicationException(Response.status(Status.FORBIDDEN).entity(new Error(e)).build());
		} catch (IOException e) {
			logger.error("getUsers: " + e, e);
			throw new WebApplicationException(Response.status(Status.INTERNAL_SERVER_ERROR).entity(new Error(e)).build());
		} finally {
			auth.clear();
		}
		return userList;
	}

//	@PUT
//	@Path("{keyStoreID}/{userName}")
//	public void putUserWithUserNamePath(@PathParam("keyStoreID") String keyStoreID, @PathParam("userName") String userName, UserWithPassword userWithPassword)
//	{
//		logger.debug("putUserWithUserNamePath: entered");
//
//		if (userName == null)
//			throw new IllegalArgumentException("How the hell can userName be null?!");
//
//		if (userWithPassword == null)
//			throw new WebApplicationException(Response.status(Status.BAD_REQUEST).entity(new Error("Missing request-entity!")).build());
//
//		if (userWithPassword.getUserName() == null)
//			userWithPassword.setUserName(userName);
//		else if (!userName.equals(userWithPassword.getUserName()))
//			throw new WebApplicationException(Response.status(Status.BAD_REQUEST).entity(new Error("Path's userName='" + userName + "' does not match entity's userName='" + userWithPassword.getUserName() + "'!")).build());
//
//		putUser(keyStoreID, userWithPassword);
//	}

	/**
	 * Compatibility for clients not supporting <code>PUT</code>. This method does the same as (it delegates to)
	 * {@link #putUser(String, UserWithPassword)}. Ajax-Clients (e.g. jQuery in Firefox) seem
	 * not to support <code>PUT</code>.
	 */
	@POST
	@Path("{keyStoreID}")
	public void postUser(@PathParam("keyStoreID") String keyStoreID, UserWithPassword userWithPassword)
	{
		putUser(keyStoreID, userWithPassword);
	}

	/**
	 * Put a user. If a user with the same {@link UserWithPassword#getUserName() name} already exists,
	 * it is updated, otherwise the new user is added to the {@link KeyStore} identified by <code>keyStoreID</code>.
	 * @param keyStoreID identifier of the {@link KeyStore} to work with.
	 * @param userWithPassword the user's information to be stored.
	 */
	@PUT
	@Path("{keyStoreID}")
	public void putUser(@PathParam("keyStoreID") String keyStoreID, UserWithPassword userWithPassword)
	{
		logger.debug("putUser: entered");

		if (userWithPassword == null)
			throw new WebApplicationException(Response.status(Status.BAD_REQUEST).entity(new Error("Missing request-entity!")).build());

		Auth auth = getAuth();

		try {
			KeyStore keyStore = keyStoreManager.getKeyStore(keyStoreID);
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
			logger.debug("putUser: " + e, e); // debug, because not an internal error
			throw new WebApplicationException(Response.status(Status.FORBIDDEN).entity(new Error(e)).build());
		} catch (IOException e) {
			logger.error("putUser: " + e, e);
			throw new WebApplicationException(Response.status(Status.INTERNAL_SERVER_ERROR).entity(new Error(e)).build());
		} finally {
			// extra safety => overwrite passwords
			auth.clear();

			if (userWithPassword.getPassword() != null)
				Arrays.fill(userWithPassword.getPassword(), (char)0);
		}
	}

	/**
	 * Delete a user.
	 * @param keyStoreID identifier of the {@link KeyStore} to work with.
	 * @param userName the {@link User#getUserName() name} of the user to be deleted.
	 */
	@DELETE
	@Path("{keyStoreID}/{userName}")
	public void deleteUser(@PathParam("keyStoreID") String keyStoreID, @PathParam("userName") String userName)
	{
		logger.debug("deleteUser: entered");

		Auth auth = getAuth();

		try {
			KeyStore keyStore = keyStoreManager.getKeyStore(keyStoreID);
			keyStore.deleteUser(auth.getUserName(), auth.getPassword(), userName);
		} catch (AuthenticationException e) {
			logger.debug("deleteUser: " + e, e); // debug, because not an internal error
			throw new WebApplicationException(Response.status(Status.FORBIDDEN).entity(new Error(e)).build());
		} catch (UserNotFoundException e) {
			// ignore in order to be idempotent - only warn
			logger.warn("deleteUser: " + e);
		} catch (CannotDeleteLastUserException e) {
			logger.debug("deleteUser: " + e, e); // debug, because not an internal error
			throw new WebApplicationException(Response.status(Status.FORBIDDEN).entity(new Error(e)).build());
		} catch (IOException e) {
			logger.error("deleteUser: " + e, e);
			throw new WebApplicationException(Response.status(Status.INTERNAL_SERVER_ERROR).entity(new Error(e)).build());
		} finally {
			// extra safety => overwrite password
			auth.clear();
		}
	}
}
