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

package org.cumulus4j.store.crypto.keymanager.rest;

import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

import org.cumulus4j.keymanager.back.shared.NullResponse;
import org.cumulus4j.keymanager.back.shared.Request;
import org.cumulus4j.keymanager.back.shared.Response;
import org.cumulus4j.store.crypto.keymanager.messagebroker.ActiveKeyManagerChannelRegistration;
import org.cumulus4j.store.crypto.keymanager.messagebroker.MessageBroker;
import org.cumulus4j.store.crypto.keymanager.messagebroker.MessageBrokerRegistry;

/**
 * REST service for the communication channel between key-manager and app-server.
 *
 * @author Marco หงุ่ยตระกูล-Schulze - marco at nightlabs dot de
 */
@Path("KeyManagerChannel")
@Consumes({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
@Produces({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
public class KeyManagerChannelService
{
//	private static final Logger logger = LoggerFactory.getLogger(KeyManagerChannelService.class);
	private MessageBroker messageBroker = MessageBrokerRegistry.sharedInstance().getActiveMessageBroker();

//	@Context
//	private HttpServletRequest httpServletRequest;

	private ActiveKeyManagerChannelRegistration registerActiveKeyManagerChannel(String cryptoSessionIDPrefix)
	{
		return null;
//		if (logger.isDebugEnabled()) {
//			logger.debug("registerKeyManagerChannelUrl: httpServletRequest.getRequestURL() = {}", httpServletRequest.getRequestURL());
//			logger.debug("registerKeyManagerChannelUrl: httpServletRequest.getRequestURI() = {}", httpServletRequest.getRequestURI());
//			logger.debug("registerKeyManagerChannelUrl: httpServletRequest.getLocalName() = {}", httpServletRequest.getLocalName());
//			logger.debug("registerKeyManagerChannelUrl: httpServletRequest.getLocalAddr() = {}", httpServletRequest.getLocalAddr());
//			logger.debug("registerKeyManagerChannelUrl: httpServletRequest.getLocalPort() = {}", httpServletRequest.getLocalPort());
//		}
//
//		String keyManagerChannelURLPart = "/KeyManagerChannel/";
//		String requestURI = httpServletRequest.getRequestURI();
//		int idx = requestURI.indexOf(keyManagerChannelURLPart);
//		if (idx < 0)
//			throw new IllegalStateException("\"" + keyManagerChannelURLPart + "\" does not occur in requestURI: " + requestURI);
//
//		String keyManagerChannelURI = requestURI.substring(0, idx + keyManagerChannelURLPart.length());
//
//		String internalKeyManagerChannelProtocol = "http"; // TODO make configurable via system property
//		String internalKeyManagerChannelHost = httpServletRequest.getLocalName(); // TODO allow overriding (instead of auto-detection) via system property
//		int internalKeyManagerChannelPort = httpServletRequest.getLocalPort(); // TODO allow overriding (instead of auto-detection) via system property
//
//		String internalKeyManagerChannelURL = (
//				internalKeyManagerChannelProtocol + "://"
//				+ internalKeyManagerChannelHost + ':' + internalKeyManagerChannelPort
//				+ keyManagerChannelURI
//		);
//
//		ActiveKeyManagerChannelRegistration registration = messageBroker.registerActiveKeyManagerChannel(cryptoSessionIDPrefix, internalKeyManagerChannelURL);
//		return registration;
	}

	private void unregisterActiveKeyManagerChannel(ActiveKeyManagerChannelRegistration registration)
	{
//		if (registration != null)
//			messageBroker.unregisterActiveKeyManagerChannel(registration);
	}

	/**
	 * Test method to allow an administrator to verify the URL in a browser.
	 * @return a string beginning with "OK:".
	 */
	@Path("test")
	@GET
	@Produces(MediaType.TEXT_PLAIN)
	public String testGet()
	{
		ActiveKeyManagerChannelRegistration registration = registerActiveKeyManagerChannel("test");
		unregisterActiveKeyManagerChannel(registration);

		return "OK: " + this.getClass().getName();
	}

//	@Path("pushResponse")
//	@POST
//	public void pushResponse(Response response)
//	{
//		registerLocalClusterNodeKeyManagerChannelUrl();
//
//		if (response == null)
//			return;
//
//		// The NullResponse can either be a filler without request and thus needs to be discarded here,
//		// or it can be a response to a specific request. Hence, we check whether the NullResponse.requestID is null.
//		if ((response instanceof NullResponse) && response.getRequestID() == null)
//			return;
//
//		messageBroker.pushResponse(response);
//	}

	/**
	 * Upload a {@link Response} and fetch the next {@link Request}. If there is no <code>Response</code>, yet,
	 * the key-manager must upload a {@link NullResponse}. This method forwards the <code>response</code>
	 * to {@link MessageBroker#pushResponse(Response)} and then {@link MessageBroker#pollRequest(String)
	 * polls the next request}.
	 * @param cryptoSessionIDPrefix the prefix used by the key-manager, i.e. the unique identifier of the key-manager.
	 * This is used for efficient routing of requests, i.e. by {@link MessageBroker#pollRequest(String)}.
	 * @param response the last response or an instance of {@link NullResponse} (without <code>requestID</code>)
	 * if there is no last request to be replied.
	 * @return the next polled request or <code>null</code> if none popped up before the timeout.
	 */
	@Path("nextRequest/{cryptoSessionIDPrefix}")
	@POST
	public Request nextRequest(@PathParam("cryptoSessionIDPrefix") String cryptoSessionIDPrefix, Response response)
	{
		if (cryptoSessionIDPrefix == null)
			throw new IllegalArgumentException("cryptoSessionIDPrefix == null");

		ActiveKeyManagerChannelRegistration registration = registerActiveKeyManagerChannel(cryptoSessionIDPrefix);
		try {
			if (response != null) {
				// The NullResponse can either be a filler without request and thus needs to be discarded here,
				// or it can be a response to a specific request. Hence, we check whether the Response.requestID is null
				// and don't care about the type at all.
				if (response.getRequestID() != null)
					messageBroker.pushResponse(response);
			}

			Request request = messageBroker.pollRequest(cryptoSessionIDPrefix);
			return request;
		} finally {
			unregisterActiveKeyManagerChannel(registration);
		}
	}

}
