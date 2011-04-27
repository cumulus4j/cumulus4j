package org.cumulus4j.keyserver.front.webapp;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.core.Context;

import org.cumulus4j.keystore.KeyStore;

@Path("user")
public class UserService
{
	@Context
	private KeyStore keyStore;

	@GET
	public String test()
	{
		return keyStore == null ? "Injection failed!" : "OK";
	}
}
