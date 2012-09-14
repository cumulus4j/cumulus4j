package de.alexgauss.test.server;

import java.util.Collection;
import java.util.List;
import java.util.UUID;

import javax.jdo.PersistenceManager;

import org.cumulus4j.store.crypto.CryptoManager;
import org.cumulus4j.store.crypto.CryptoSession;

import com.google.gwt.user.server.rpc.RemoteServiceServlet;

import de.alexgauss.test.client.GreetingService;
import de.alexgauss.test.shared.FieldVerifier;

/**
 * The server side implementation of the RPC service.
 */
@SuppressWarnings("serial")
public class GreetingServiceImpl extends RemoteServiceServlet implements
		GreetingService {

	UUID random1 = UUID.randomUUID();
	UUID random2 = UUID.randomUUID();
	
	public String greetServer(String input) throws IllegalArgumentException {
		// Verify that the input is valid. 
		if (!FieldVerifier.isValidName(input)) {
			// If the input is not valid, throw an IllegalArgumentException back to
			// the client.
			throw new IllegalArgumentException(
					"Name must be at least 4 characters long");
		}

		String serverInfo = getServletContext().getServerInfo();
		String userAgent = getThreadLocalRequest().getHeader("User-Agent");

		// Escape data from the client to avoid cross-site script vulnerabilities.
		input = escapeHtml(input);
		userAgent = escapeHtml(userAgent);

		return "Hello, " + input + "!<br><br>I am running " + serverInfo
				+ ".<br><br>It looks like you are using:<br>" + userAgent;
	}

	/**
	 * Escape an html string. Escaping data received from the client helps to
	 * prevent cross-site script vulnerabilities.
	 * 
	 * @param html the html string to escape
	 * @return the escaped string
	 */
	private String escapeHtml(String html) {
		if (html == null) {
			return null;
		}
		return html.replaceAll("&", "&amp;").replaceAll("<", "&lt;")
				.replaceAll(">", "&gt;");
	}
	
	@Override
	public void saveTestData(String fName, String lName)
			throws IllegalArgumentException {
		
		PersistenceManager pm = null;
		pm = PMF.get().getPersistenceManager();
		pm.setProperty(CryptoManager.PROPERTY_CRYPTO_MANAGER_ID, "dummy");
		pm.setProperty(CryptoSession.PROPERTY_CRYPTO_SESSION_ID, "dummy" + 1 + '_' + random1 + '*' + random2);

//		MovieDBO movie = new MovieDBO("Avatar", "Jake Sully");
			
		TestDBO test = new TestDBO(fName, lName);

		pm.makePersistent(test);
		pm.close();
	}

	@SuppressWarnings("unchecked")
	@Override
	public String getTestData() throws IllegalArgumentException {
		PersistenceManager pm = null;
		pm = PMF.get().getPersistenceManager();
		pm.setProperty(CryptoManager.PROPERTY_CRYPTO_MANAGER_ID, "dummy");
		pm.setProperty(CryptoSession.PROPERTY_CRYPTO_SESSION_ID, "dummy" + 1 + '_' + random1 + '*' + random2);

		List<TestDBO> result = (List<TestDBO>) pm.newQuery("select from " + TestDBO.class.getName() + " WHERE firstName == 'Martin'").execute();

		pm.close();
		return String.valueOf(result.get(0).getFirstName() + " " + result.get(0).getLastName());
	}
	
}
