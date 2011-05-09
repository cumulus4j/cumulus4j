package org.cumulus4j.keymanager.front.webapp;

import java.io.ByteArrayInputStream;
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
 * Base class for
 *
 * @author Marco หงุ่ยตระกูล-Schulze - marco at nightlabs dot de
 */
public abstract class AbstractService
{
	private static final Logger logger = LoggerFactory.getLogger(AbstractService.class);

	protected @Context HttpServletRequest request;

	protected @Context KeyStore keyStore;

	/**
	 * Get the authentication information. This method does <b>not</b> verify, if the given authentication information
	 * is correct! It merely checks, if the client sent a 'Basic' authentication header. If it did not,
	 * this method throws a {@link WebApplicationException} with {@link Status#UNAUTHORIZED}. If it did, it extracts
	 * the information and puts it into an {@link Auth} instance.
	 * @return the {@link Auth} instance extracted from the client's headers. Never <code>null</code>.
	 */
	protected Auth getAuth()
	{
		String authorizationHeader = request.getHeader("Authorization");
		if (authorizationHeader == null || authorizationHeader.isEmpty()) {
			logger.debug("authenticate: There is no 'Authorization' header. Replying with a Status.UNAUTHORIZED response asking for 'Basic' authentication.");

			throw new WebApplicationException(Response.status(Status.UNAUTHORIZED).header("WWW-Authenticate", "Basic realm=\"Cumulus4jKeyServer\"").build());
		}

		logger.debug("authenticate: 'Authorization' header: {}", authorizationHeader);

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

	protected Auth authenticate()
	{
		Auth auth = getAuth();
		try {
			keyStore.getKey(auth.getUserName(), auth.getPassword(), Long.MAX_VALUE);
		} catch (AuthenticationException e) {
			throw new WebApplicationException(Response.status(Status.FORBIDDEN).entity(new Error(e)).build());
		} catch (KeyNotFoundException e) {
			// ignore this - it's expected
			doNothing(); // Remove warning from PMD report: http://cumulus4j.org/pmd.html
		}
		return auth;
	}

	private static final void doNothing() { }
}
