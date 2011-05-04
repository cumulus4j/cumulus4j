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
	@Produces(MediaType.TEXT_PLAIN)
	public String testPost()
	{
		// TODO create a PersistenceManagerFactory (lazily only once), get a PersistenceManager, perform some operations (in transactions)
		// requiring keys and finally return "OK: ...".










		return "OK: " + this.getClass().getName();
	}

	@Path("test")
	@GET
	@Produces(MediaType.TEXT_PLAIN)
	public String testGet()
	{
		return "OK: " + this.getClass().getName();
	}

}
