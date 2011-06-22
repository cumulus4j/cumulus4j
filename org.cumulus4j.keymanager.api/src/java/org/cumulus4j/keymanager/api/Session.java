package org.cumulus4j.keymanager.api;

public interface Session
{
	String getAppServerID();

	String getAppServerBaseURL();

	String getCryptoSessionID();

	void lock();

	void unlock();

	void close();
}
