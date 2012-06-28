package org.cumulus4j.jee.test.ejb;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

import javax.jdo.JDOHelper;
import javax.jdo.PersistenceManager;
import javax.jdo.PersistenceManagerFactory;

public abstract class AbstractDataNucleusTestBean {
	
	private static PersistenceManagerFactory pmf = null;

	protected PersistenceManager getPersistenceManager() {
		
		return getPersistenceManagerFactory().getPersistenceManager();
	}
	
	private PersistenceManagerFactory getPersistenceManagerFactory(){
		
		if(pmf == null){
			
			InputStream propertiesStream = getClass().getResourceAsStream("datanucleus.properties");
			Properties propsFile = new Properties();
			try {
				propsFile.load(propertiesStream);
				propertiesStream.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
			pmf = JDOHelper.getPersistenceManagerFactory(propsFile);
//			pmf = JDOHelper.getPersistenceManagerFactory();
		}
		
		return pmf;
	}	
}
