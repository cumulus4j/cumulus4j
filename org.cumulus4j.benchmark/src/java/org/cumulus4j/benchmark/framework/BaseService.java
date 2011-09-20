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
	
	public static final String CUMULUS4J_NOT_ACTIVATED = "Cumulus4j not activated!";
	
	@GET
	@Path("getConfiguration")
	@Produces(MediaType.TEXT_PLAIN)
	public String getConfiguration()
	{
		
		StringBuilder result = new StringBuilder("Current properties:\n");
		
		if(PersonDAO.sharedInstance().currentConfiguration() == null)
			return result.append(CUMULUS4J_NOT_ACTIVATED).toString();
		else
			return result.append(PersonDAO.sharedInstance().currentConfiguration()).toString();
	}

	@GET
	@Path("nextConfiguration")
	@Produces(MediaType.TEXT_PLAIN)
	public String nextConfiguration(){
		PersonDAO.sharedInstance().nextConfiguration();
		
		StringBuilder result = new StringBuilder("Current properties:\n");
		
		if(PersonDAO.sharedInstance().currentConfiguration() == null)
			return result.append(CUMULUS4J_NOT_ACTIVATED).toString();
		else
			return result.append(PersonDAO.sharedInstance().currentConfiguration()).toString();
	}
}
