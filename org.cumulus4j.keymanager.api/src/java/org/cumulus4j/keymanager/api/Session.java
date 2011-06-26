package org.cumulus4j.keymanager.api;

import java.io.IOException;

public interface Session
{
	String getAppServerID();

	String getAppServerBaseURL();

	String getCryptoSessionID() throws AuthenticationException, IOException;

	void lock() throws AuthenticationException, IOException;

	void unlock() throws AuthenticationException, IOException;

	void close();
}
