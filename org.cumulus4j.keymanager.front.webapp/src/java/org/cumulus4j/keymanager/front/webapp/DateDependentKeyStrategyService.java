package org.cumulus4j.keymanager.front.webapp;

import java.io.IOException;

import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

import org.cumulus4j.keymanager.front.shared.DateDependentKeyStrategyInitParam;
import org.cumulus4j.keymanager.front.shared.Error;
import org.cumulus4j.keystore.DateDependentKeyStrategy;
import org.cumulus4j.keystore.KeyStoreNotEmptyException;

@Path("DateDependentKeyStrategy")
@Consumes({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
@Produces({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
public class DateDependentKeyStrategyService extends AbstractService
{
	@POST
	public void init(DateDependentKeyStrategyInitParam param)
	{
		Auth auth = getAuth();
		try {
			new DateDependentKeyStrategy(keyStore).init(
					auth.getUserName(), auth.getPassword(),
					param.getKeyActivityPeriodMSec(), param.getKeyStorePeriodMSec()
			);
		} catch (KeyStoreNotEmptyException e) {
			throw new WebApplicationException(Response.status(Status.BAD_REQUEST).entity(new Error(e)).build());
		} catch (IOException e) {
			throw new WebApplicationException(Response.status(Status.INTERNAL_SERVER_ERROR).entity(new Error(e)).build());
		} finally {
			auth.clear();
		}
	}
}
