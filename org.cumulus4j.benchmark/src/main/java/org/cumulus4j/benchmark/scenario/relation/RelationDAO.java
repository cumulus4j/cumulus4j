package org.cumulus4j.benchmark.scenario.relation;

import org.cumulus4j.benchmark.framework.BaseDAO;

public class RelationDAO extends BaseDAO<SimplePerson>{

	private RelationDAO(){}

	public static RelationDAO sharedInstance(){

		RelationDAO instance = (RelationDAO) BaseDAO.getInstance(SimplePerson.class);

		if(instance != null)
			return instance;

		instance = new RelationDAO();
		registerInstance(instance, SimplePerson.class);

		return instance;
	}
}