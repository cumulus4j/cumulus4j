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

/**
 * @author Marco หงุ่ยตระกูล-Schulze - marco at nightlabs dot de
 */
@Path("KeyServerChannel")
@Consumes({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
@Produces({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
public class KeyServerChannelService
{
	private MessageBroker messageBroker = MessageBroker.sharedInstance();

	@Path("test")
	@GET
	@Produces(MediaType.TEXT_PLAIN)
	public String testGet()
	{
		return "OK: " + this.getClass().getName();
	}

	@Path("pushResponse")
	@POST
	public void pushResponse(Response response)
	{
		if (response == null)
			return;

		// The NullResponse can either be a filler without request and thus needs to be discarded here,
		// or it can be a response to a specific request. Hence, we check whether the NullResponse.requestID is null.
		if ((response instanceof NullResponse) && response.getRequestID() == null)
			return;

		messageBroker.pushResponse(response);
	}

	@Path("nextRequest/{cryptoSessionIDPrefix}")
	@POST
	public Request nextRequest(@PathParam("cryptoSessionIDPrefix") String cryptoSessionIDPrefix, Response response)
	{
		if (cryptoSessionIDPrefix == null)
			throw new IllegalArgumentException("cryptoSessionIDPrefix == null");

		if (response != null)
			pushResponse(response);

		Request request = messageBroker.pollRequestForProcessing(cryptoSessionIDPrefix);
		return request;
	}

}
