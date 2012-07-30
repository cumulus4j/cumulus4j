package org.cumulus4j.jee.test.ejb.plaindatasource;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.UUID;

import javax.annotation.Resource;
import javax.ejb.Stateless;
import javax.sql.DataSource;

import org.cumulus4j.jee.test.ejb.TestRollbackException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Stateless
public class PlainDataSourceSharedTransactionBean extends
		AbstractPlainDataSourceTestBean {

	private static final Logger logger = LoggerFactory
			.getLogger(PlainDataSourceSharedTransactionBean.class);

	@Resource(name = "jdbc/__default")
	protected DataSource defaultDataSource;

	public void testRollback(UUID id, boolean throwException)
			throws SQLException {

		Connection connection = defaultDataSource.getConnection();

		storeId(connection, id);

		if (throwException)
			throw new TestRollbackException(
					"Legal exception for test purposes. Object "
							+ id.toString()
							+ " should now be deleted! Also the object from the enclosing transaction should be deleted!");
		else
			logger.info("Nested transaction ended without throwing an exception!");

	}

}
