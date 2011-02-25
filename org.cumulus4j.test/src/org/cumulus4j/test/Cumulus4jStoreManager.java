package org.cumulus4j.test;

import java.util.Map;

import org.datanucleus.ClassLoaderResolver;
import org.datanucleus.NucleusContext;
import org.datanucleus.store.AbstractStoreManager;

public class Cumulus4jStoreManager
extends AbstractStoreManager
{
	public Cumulus4jStoreManager(ClassLoaderResolver clr, NucleusContext nucleusContext, Map<String, Object> props)
	{
		super("cumulus4j", clr, nucleusContext, props);

		persistenceHandler = new Cumulus4jPersistenceHandler(this);
	}

}
