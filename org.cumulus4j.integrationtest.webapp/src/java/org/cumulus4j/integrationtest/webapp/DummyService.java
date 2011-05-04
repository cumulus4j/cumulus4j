package org.cumulus4j.integrationtest.webapp;

import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

@Path("Dummy")
@Consumes({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
@Produces({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
public class DummyService
{

	@Path("test")
	@POST
	public void testPost()
	{

	}

	@Path("test")
	@GET
	@Produces(MediaType.TEXT_PLAIN)
	public String testGet()
	{
		return "Yabbadabbadoo: " + this.getClass().getName();
	}

}
