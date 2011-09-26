package org.cumulus4j.benchmark.framework;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

import org.cumulus4j.benchmark.person.PersonDAO;

/**
 *
 * @author Jan Mortensen - jmortensen at nightlabs dot de
 *
 */
public abstract class BaseService {

	public static final String WARMUP = "warmup";
	public static final String NEXT_CONFIGURATION = "nextConfiguration";
	public static final String GET_RESULTS = "getResults";

	@GET
	@Path(NEXT_CONFIGURATION)
	@Produces(MediaType.TEXT_PLAIN)
	public String nextConfiguration(){

		PersonDAO.sharedInstance().nextConfiguration();

		if(PersonDAO.sharedInstance().currentConfiguration() == null)
			return "Cumulus4j disabled";
		else
			return PersonDAO.sharedInstance().currentConfiguration().toString();
	}

	@GET
	@Path(GET_RESULTS)
	@Produces(MediaType.TEXT_PLAIN)
	public String getResults()
	{
		throw new UnsupportedOperationException("NYI");
//		return Per;
	}

	public abstract String warmup(String cryptoManagerID, String cryptiSessionID);
}
