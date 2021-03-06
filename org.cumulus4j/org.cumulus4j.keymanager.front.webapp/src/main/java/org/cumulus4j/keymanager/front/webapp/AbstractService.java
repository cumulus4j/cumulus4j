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

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.nio.CharBuffer;
import java.util.Arrays;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

import org.cumulus4j.keymanager.front.shared.Error;
import org.cumulus4j.keystore.AuthenticationException;
import org.cumulus4j.keystore.KeyNotFoundException;
import org.cumulus4j.keystore.KeyStore;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sun.jersey.core.util.Base64;

/**
 * Abstract base class for all REST services of the key-server.
 *
 * @author Marco หงุ่ยตระกูล-Schulze - marco at nightlabs dot de
 */
public abstract class AbstractService
{
	private static final Logger logger = LoggerFactory.getLogger(AbstractService.class);

	protected @Context HttpServletRequest request;

//	protected @Context KeyStore keyStore;

	protected @Context KeyStoreManager keyStoreManager;

	/**
	 * Get the authentication information. This method does <b>not</b> verify, if the given authentication information
	 * is correct! It merely checks, if the client sent a 'Basic' authentication header. If it did not,
	 * this method throws a {@link WebApplicationException} with {@link Status#UNAUTHORIZED} or {@link Status#FORBIDDEN}.
	 * If it did, it extracts the information and puts it into an {@link Auth} instance.
	 * @return the {@link Auth} instance extracted from the client's headers. Never <code>null</code>.
	 * @throws WebApplicationException with {@link Status#UNAUTHORIZED}, if the client did not send an 'Authorization' header;
	 * with {@link Status#FORBIDDEN}, if there is an 'Authorization' header, but no 'Basic' authentication header (other authentication modes, like e.g. 'Digest'
	 * are not supported).
	 */
	protected Auth getAuth()
	throws WebApplicationException
	{
		String authorizationHeader = request.getHeader("Authorization");
		if (authorizationHeader == null || authorizationHeader.isEmpty()) {
			logger.debug("getAuth: There is no 'Authorization' header. Replying with a Status.UNAUTHORIZED response asking for 'Basic' authentication.");

			throw new WebApplicationException(Response.status(Status.UNAUTHORIZED).header("WWW-Authenticate", "Basic realm=\"Cumulus4jKeyServer\"").build());
		}

		logger.debug("getAuth: 'Authorization' header: {}", authorizationHeader);

		if (!authorizationHeader.startsWith("Basic"))
			throw new WebApplicationException(Response.status(Status.FORBIDDEN).entity(new Error("Only 'Basic' authentication is supported!")).build());

		String basicAuthEncoded = authorizationHeader.substring("Basic".length()).trim();
		byte[] basicAuthDecodedBA = Base64.decode(basicAuthEncoded);
		StringBuilder userNameSB = new StringBuilder();
		char[] password = null;

		ByteArrayInputStream in = new ByteArrayInputStream(basicAuthDecodedBA);
		CharBuffer cb = CharBuffer.allocate(basicAuthDecodedBA.length + 1);
		try {
			Reader r = new InputStreamReader(in, "UTF-8");
			int charsReadTotal = 0;
			int charsRead;
			do {
				charsRead = r.read(cb);

				if (charsRead > 0)
					charsReadTotal += charsRead;
			} while (charsRead >= 0);
			cb.position(0);

			while (cb.position() < charsReadTotal) {
				char c = cb.get();
				if (c == ':')
					break;

				userNameSB.append(c);
			}

			if (cb.position() < charsReadTotal) {
				password = new char[charsReadTotal - cb.position()];
				int idx = 0;
				while (cb.position() < charsReadTotal)
					password[idx++] = cb.get();
			}
		} catch (Exception e) {
			throw new WebApplicationException(Response.status(Status.INTERNAL_SERVER_ERROR).entity(new Error(e)).build());
		} finally {
			// For extra safety: Overwrite all sensitive memory with 0.
			Arrays.fill(basicAuthDecodedBA, (byte)0);

			cb.position(0);
			for (int i = 0; i < cb.capacity(); ++i)
				cb.put((char)0);
		}

		Auth auth = new Auth();
		auth.setUserName(userNameSB.toString());
		auth.setPassword(password);
		return auth;
	}

	/**
	 * Get the {@link Auth} information via {@link #getAuth()} and verify, if they are valid. The validity is checked
	 * by trying to access the key-store.
	 * @param keyStoreID identifier of the key-store to work with.
	 * @return the {@link Auth} information via {@link #getAuth()}; never <code>null</code>.
	 * @throws WebApplicationException with {@link Status#UNAUTHORIZED}, if the client did not send an 'Authorization' header
	 * or if user-name / password is wrong;
	 * with {@link Status#FORBIDDEN}, if there is an 'Authorization' header, but no 'Basic' authentication header (other authentication modes, like e.g. 'Digest'
	 * are not supported); with {@link Status#INTERNAL_SERVER_ERROR}, if there was an {@link IOException}.
	 */
	protected Auth authenticate(String keyStoreID)
	throws WebApplicationException
	{
		Auth auth = getAuth();
		try {
			KeyStore keyStore = keyStoreManager.getKeyStore(keyStoreID);
			keyStore.getKey(auth.getUserName(), auth.getPassword(), Long.MAX_VALUE);
		} catch (IOException e) {
			throw new WebApplicationException(Response.status(Status.INTERNAL_SERVER_ERROR).entity(new Error(e)).build());
		} catch (AuthenticationException e) {
			throw new WebApplicationException(Response.status(Status.UNAUTHORIZED).entity(new Error(e)).build());
		} catch (KeyNotFoundException e) {
			// ignore this - it's expected
			doNothing(); // Remove warning from PMD report: http://cumulus4j.org/pmd.html
		}
		return auth;
	}

	private static final void doNothing() { }
}
