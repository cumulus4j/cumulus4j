package org.cumulus4j.keyserver.back.webapp;

import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

import org.cumulus4j.keyserver.back.plugin.RequestResponseBroker;
import org.cumulus4j.keyserver.back.shared.Request;
import org.cumulus4j.keyserver.back.shared.Response;

/**
 * @author Marco หงุ่ยตระกูล-Schulze - marco at nightlabs dot de
 */
@Path("KeyServerChannel")
@Consumes({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
@Produces({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
public class KeyServerChannelService
{
	private RequestResponseBroker requestResponseBroker = RequestResponseBroker.sharedInstance();

	@Path("pushResponse")
	@POST
	public void pushResponse(Response response)
	{
		if (response != null)
			requestResponseBroker.pushResponse(response);
	}

	@Path("nextRequest/{keyServerID}")
	@POST
	public Request nextRequest(@PathParam("keyServerID") String keyServerID, Response response)
	{
		if (keyServerID == null)
			throw new IllegalArgumentException("keyServerID == null");

		if (response != null)
			pushResponse(response);

		Request request = requestResponseBroker.pollRequestForProcessing(keyServerID);
		return request;
	}

}
