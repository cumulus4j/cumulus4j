package org.cumulus4j.benchmark.test;

import org.cumulus4j.benchmark.framework.BaseDAO;
import org.cumulus4j.benchmark.scenario.relation.RelationDAO;
import org.cumulus4j.benchmark.scenario.relation.SimplePerson;
import org.junit.Test;

public class TestDAO {

	@Test
	public void testDAO() throws Exception{

		RelationDAO relationDao = RelationDAO.sharedInstance();

		BaseDAO<SimplePerson> baseDao = (BaseDAO<SimplePerson>) BaseDAO.sharedInstance(SimplePerson.class);

		assert(relationDao.equals(baseDao));
	}
}
