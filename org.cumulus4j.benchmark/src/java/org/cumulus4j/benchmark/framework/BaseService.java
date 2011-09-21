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
	
//	@GET
//	@Path("getConfiguration")
//	@Produces(MediaType.TEXT_PLAIN)
//	public String getConfiguration()
//	{
//		
//		StringBuilder result = new StringBuilder("Current properties:\n");
//		
//		if(PersonDAO.sharedInstance().currentConfiguration() == null)
//			return result.append("Cumulus4j disabled").toString();
//		else
//			return result.append(PersonDAO.sharedInstance().currentConfiguration()).toString();
//	}

	@GET
	@Path("nextConfiguration")
	@Produces(MediaType.TEXT_PLAIN)
	public String nextConfiguration(){

		PersonDAO.sharedInstance().nextConfiguration();
		
		if(PersonDAO.sharedInstance().currentConfiguration() == null)
			return "Cumulus4j disabled";
		else
			return PersonDAO.sharedInstance().currentConfiguration().toString();
	}
	
	@GET
	@Path("getResults")
	@Produces(MediaType.TEXT_PLAIN)
	public String getResults(){
		
		return "";
	}
	
//	public abstract String getBenchmarkProperties();

	public abstract String warmup(String cryptoManagerID, String cryptiSessionID);
}
