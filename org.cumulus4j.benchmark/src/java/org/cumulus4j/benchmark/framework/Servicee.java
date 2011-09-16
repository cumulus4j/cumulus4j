package org.cumulus4j.benchmark.framework;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

public abstract class Servicee<T> {
	
//	@GET
//	@Path("getAllPersons")
//	@Produces(MediaType.TEXT_PLAIN)
	public abstract T get(String cryptoManagerID, String cryptoSessionID);
	
	public abstract void getAll(String cryptoManagerID, String cryptoSessionID);

}
