package org.cumulus4j.benchmark.scenario.relation;

import javax.jdo.PersistenceManager;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;

import org.cumulus4j.benchmark.framework.BaseScenario;
import org.cumulus4j.benchmark.framework.PersistenceManagerProvider;
import org.cumulus4j.benchmark.scenario.inheritance.InheritanceScenarioService;
import org.nightlabs.util.Stopwatch;

/**
 * @author Jan Mortensen - jmortensen at nightlabs dot de
 */
@Path(InheritanceScenarioService.PATH)
@Consumes({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
@Produces({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
public class RelationScenarioService extends BaseScenario<SimplePerson> {

	public static final String GET_ACCOUNTS_OF_PERSON = "getAccountOfPerson";

	public static final String CREATE_ACCOUNTS = "createAccounts";

	@Override
	protected SimplePerson createNewObject() {

		return new SimplePerson();
	}

	@Override
	protected Class<SimplePerson> getObjectClass() {

		return SimplePerson.class;
	}

	@GET
	@Path(GET_ACCOUNTS_OF_PERSON)
	@Produces(MediaType.TEXT_PLAIN)
	public String getAccountsOfPerson(
			@QueryParam("cryptoManagerID") String cryptoManagerID,
			@QueryParam("cryptoSessionID") String cryptoSessionID
	)
	{


		return "";
	}

	@GET
	@Path(CREATE_ACCOUNTS)
	@Produces(MediaType.TEXT_PLAIN)
	public String createAccounts(
			@QueryParam("cryptoManagerID") String cryptoManagerID,
			@QueryParam("cryptoSessionID") String cryptoSessionID
	)
	{
		if (cryptoManagerID == null || cryptoManagerID.isEmpty())
			cryptoManagerID = "keyManager";

		PersistenceManager pm = PersistenceManagerProvider.sharedInstance().getPersistenceManager(cryptoManagerID, cryptoSessionID);

		getRandomObjectId(cryptoManagerID, cryptoSessionID);

		BankAccount account = new BankAccount(new SimplePerson());

		try{
			pm.currentTransaction().begin();

			Stopwatch stopwatch = new Stopwatch();
			stopwatch.start(STORE_SINGLE_OBJECT);
			pm.makePersistent(account);
			stopwatch.stop(STORE_SINGLE_OBJECT);
			results.add(stopwatch.createHumanReport(true));

			pm.currentTransaction().commit();
		}
		finally{
			if (pm.currentTransaction().isActive())
				pm.currentTransaction().rollback();

			pm.close();
		}


		return "";
	}
}
