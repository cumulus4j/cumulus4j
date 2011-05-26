package org.cumulus4j.store.crypto.keymanager.rest;

import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

import org.cumulus4j.keymanager.back.shared.Request;
import org.cumulus4j.keymanager.back.shared.Response;
import org.cumulus4j.store.crypto.keymanager.messagebroker.ActiveKeyManagerChannelRegistration;
import org.cumulus4j.store.crypto.keymanager.messagebroker.MessageBroker;
import org.cumulus4j.store.crypto.keymanager.messagebroker.MessageBrokerRegistry;

/**
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
