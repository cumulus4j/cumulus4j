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
import org.cumulus4j.keymanager.front.shared.AcquireCryptoSessionResponse;
import org.cumulus4j.keymanager.front.shared.Error;
import org.cumulus4j.keystore.AuthenticationException;
import org.cumulus4j.keystore.KeyStore;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * <p>
 * REST service for session management.
 * </p><p>
 * Whenever the app-server wants to read or write data, it requires access to keys. The keys
 * are sent to the app-server, held in memory temporarily, and forgotten after a while.
 * </p><p>
 * In order
 * to make it impossible to ask a key-server for keys without being authorised to do so, the
 * key-server manages crypto-sessions. Only someone knowing a valid crypto-session's ID can query
 * keys. This should already exclude everyone except for the app-server who is told the crypto-session-ID
 * (originating from the client).
 * </p><p>
 * But to make things even more secure, each crypto-session can additionally be locked and unlocked.
 * Most of the time, a session
 * is locked and thus prevents keys from being read. Only in those moments when the client delegates
 * work to the app-server (and the app-server thus requires key-access to fulfill the client's command), the
 * corresponding crypto-session is unlocked.
 * </p>
 *
 * @author Marco หงุ่ยตระกูล-Schulze - marco at nightlabs dot de
 */
@Path("CryptoSession")
@Consumes({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
@Produces({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
public class CryptoSessionService extends AbstractService
{
	private static final Logger logger = LoggerFactory.getLogger(CryptoSessionService.class);

	/**
	 * <p>
	 * Acquire a session.
	 * </p><p>
	 * Even if there exists already a session for the combination of <code>keyStoreID</code> and
	 * <code>appServerID</code>, a new session might be created. Old sessions are only re-used and refreshed,
	 * if they are currently in the 'released' state.
	 * </p><p>
	 * The session can be explicitely {@link #delete(String, String, String)deleted} or automatically disappears
	 * after a {@link AcquireCryptoSessionResponse#getExpiry() certain time}. Thus, refreshing it is necessary to keep
	 * it "alive".
	 * </p>
	 * @param keyStoreID identifier of the {@link KeyStore} to work with.
	 * @param appServerID identifier of the (logical) app-server (who will access the key-store on behalf of the client).
	 */
	@Path("{keyStoreID}/{appServerID}/acquire")
	@POST
	public AcquireCryptoSessionResponse acquire(@PathParam("keyStoreID") String keyStoreID, @PathParam("appServerID") String appServerID)
	{
		Auth auth = getAuth();
		try {
			SessionManager sessionManager = getSessionManager(keyStoreID, appServerID);

			Session session;
			try {
				session = sessionManager.acquireSession(auth.getUserName(), auth.getPassword());
			} catch (AuthenticationException e) {
				throw new WebApplicationException(Response.status(Status.FORBIDDEN).entity(new Error(e)).build());
			}

			logger.debug("open: authUserName='{}' cryptoSessionID='{}'", auth.getUserName(), session.getCryptoSessionID());

			AcquireCryptoSessionResponse result = new AcquireCryptoSessionResponse();
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
			logger.error("getSessionManager: " + e, e);
			throw new WebApplicationException(Response.status(Status.INTERNAL_SERVER_ERROR).entity(new Error(e)).build());
		}

		AppServer appServer = appServerManager.getAppServerForAppServerID(appServerID);
		if (appServer == null) {
			String message = "There is no AppServer with appServerID=\"" + appServerID + "\"!";
			logger.debug("getSessionManager: " + message);
			throw new WebApplicationException(Response.status(Status.NOT_FOUND).entity(new Error(message)).build());
		}

		return appServer.getSessionManager();
	}

	/**
 	 * Refresh (reacquire) an already acquired crypto-session.
 	 * Prevent it from being automatically released+deleted due to timeout.
 	 *
	 * @param keyStoreID identifier of the {@link KeyStore} to work with.
	 * @param appServerID identifier of the (logical) app-server (who will access the key-store on behalf of the client).
	 * @param cryptoSessionID identifier of the crypto-session to refresh (generated by {@link #acquire(String, String)}).
	 */
	@Path("{keyStoreID}/{appServerID}/{cryptoSessionID}/reacquire")
	@POST
	public AcquireCryptoSessionResponse reacquire(@PathParam("keyStoreID") String keyStoreID, @PathParam("appServerID") String appServerID, @PathParam("cryptoSessionID") String cryptoSessionID)
	{
		SessionManager sessionManager = getSessionManager(keyStoreID, appServerID);
		Session session = sessionManager.getSessionForCryptoSessionID(cryptoSessionID);
		if (session == null) {
			String message = "There is no session with cryptoSessionID='" + cryptoSessionID + "'!";
			logger.debug("reacquire: " + message);
			throw new WebApplicationException(Response.status(Status.NOT_FOUND).entity(new Error(message)).build());
		}

		try {
			session.reacquire();
		} catch (Exception x) {
			logger.debug("reacquire: " + x, x);
			throw new WebApplicationException(Response.status(Status.NOT_FOUND).entity(new Error(x)).build());
		}

		AcquireCryptoSessionResponse result = new AcquireCryptoSessionResponse();
		result.setCryptoSessionID(session.getCryptoSessionID());
		result.setExpiry(session.getExpiry());
		return result;
	}

//	@Path("{keyStoreID}/{appServerID}/{cryptoSessionID}/unlock")
//	@GET
//	public void unlock_GET(@PathParam("keyStoreID") String keyStoreID, @PathParam("appServerID") String appServerID, @PathParam("cryptoSessionID") String cryptoSessionID)
//	{
//		unlock(keyStoreID, appServerID, cryptoSessionID);
//	}

//	/**
//	 * Unlock a crypto-session (grant access to keys).
//	 *
//	 * @param keyStoreID identifier of the {@link KeyStore} to work with.
//	 * @param appServerID identifier of the (logical) app-server (who will access the key-store on behalf of the client).
//	 * @param cryptoSessionID identifier of the crypto-session to unlock (generated by {@link #open(String, String)}).
//	 */
//	@Path("{keyStoreID}/{appServerID}/{cryptoSessionID}/unlock")
//	@POST
//	public void unlock(@PathParam("keyStoreID") String keyStoreID, @PathParam("appServerID") String appServerID, @PathParam("cryptoSessionID") String cryptoSessionID)
//	{
//		SessionManager sessionManager = getSessionManager(keyStoreID, appServerID);
//		Session session = sessionManager.getSessionForCryptoSessionID(cryptoSessionID);
//		if (session == null)
//			throw new WebApplicationException(Response.status(Status.NOT_FOUND).entity(new Error("There is no session with cryptoSessionID='" + cryptoSessionID + "'!")).build());
//
//		session.setLocked(false);
//	}

//	@Path("{keyStoreID}/{appServerID}/{cryptoSessionID}/lock")
//	@GET
//	public void lock_GET(@PathParam("keyStoreID") String keyStoreID, @PathParam("appServerID") String appServerID, @PathParam("cryptoSessionID") String cryptoSessionID)
//	{
//		lock(keyStoreID, appServerID, cryptoSessionID);
//	}

	/**
 	 * Release a crypto-session (prevent further access to keys).
 	 *
	 * @param keyStoreID identifier of the {@link KeyStore} to work with.
	 * @param appServerID identifier of the (logical) app-server (who will access the key-store on behalf of the client).
	 * @param cryptoSessionID identifier of the crypto-session to lock (generated by {@link #acquire(String, String)}).
	 */
	@Path("{keyStoreID}/{appServerID}/{cryptoSessionID}/release")
	@POST
	public void release(@PathParam("keyStoreID") String keyStoreID, @PathParam("appServerID") String appServerID, @PathParam("cryptoSessionID") String cryptoSessionID)
	{
		SessionManager sessionManager = getSessionManager(keyStoreID, appServerID);
		Session session = sessionManager.getSessionForCryptoSessionID(cryptoSessionID);
		if (session == null) {
			String message = "There is no session with cryptoSessionID='" + cryptoSessionID + "'!";
			logger.debug("release: " + message);
			throw new WebApplicationException(Response.status(Status.NOT_FOUND).entity(new Error(message)).build());
		}

		session.release();
	}

	/**
	 * Destroy a crypto-session. No further key-exchange will be possible within the scope
	 * of this session. This is similar to {@link #release(String, String, String)}, but
	 * instead of only locking the session (setting a boolean state), it removes the session completely
	 * and thus releases any memory and other resources allocated.
	 *
	 * @param keyStoreID identifier of the {@link KeyStore} to work with.
	 * @param appServerID identifier of the (logical) app-server (who will access the key-store on behalf of the client).
	 * @param cryptoSessionID identifier of the crypto-session to be closed (generated by {@link #acquire(String, String)}).
	 */
	@Path("{keyStoreID}/{appServerID}/{cryptoSessionID}")
	@DELETE
	public void delete(@PathParam("keyStoreID") String keyStoreID, @PathParam("appServerID") String appServerID, @PathParam("cryptoSessionID") String cryptoSessionID)
	{
		logger.debug("delete: appServerID='{}' cryptoSessionID='{}'", appServerID, cryptoSessionID);

		SessionManager sessionManager = getSessionManager(keyStoreID, appServerID);
		Session session = sessionManager.getSessionForCryptoSessionID(cryptoSessionID);
		if (session != null)
			session.destroy();
	}
}
