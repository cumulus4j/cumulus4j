package org.cumulus4j.jee.test.ejb.plaindatasource;

import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Collection;
import java.util.HashSet;
import java.util.UUID;

import javax.annotation.Resource;
import javax.ejb.EJB;
import javax.ejb.Stateless;
import javax.sql.DataSource;

import org.apache.commons.lang.exception.ExceptionUtils;
import org.cumulus4j.jee.test.ejb.TestRollbackException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Stateless
public class DefaultDataSourceTestBean extends AbstractPlainDataSourceTestBean
		implements DefaultDataSourceTestRemote {
	private static final Logger logger = LoggerFactory
			.getLogger(DefaultDataSourceTestBean.class);

	@Resource(name = "jdbc/__default")
	protected DataSource defaultDataSource;

	@EJB
	private PlainDataSourceNewTransactionBean plainDataSourceNewTransactionBean;

	@Override
	public boolean testDataStoreConnection() throws SQLException {

		Connection connection = defaultDataSource.getConnection();

		try {
			logger.trace("test: *** TRACE *** TRACE *** TRACE *** TRACE *** TRACE *** TRACE ***");
			logger.debug("test: *** DEBUG *** DEBUG *** DEBUG *** DEBUG *** DEBUG *** DEBUG ***");
			logger.info("test: *** INFO *** INFO *** INFO *** INFO *** INFO *** INFO ***");
			logger.warn("test: *** WARN *** WARN *** WARN *** WARN *** WARN *** WARN ***");
			logger.error("test: *** ERROR *** ERROR *** ERROR *** ERROR *** ERROR *** ERROR ***");

			logger.info("test: connection={}");
			executeSomeTestSQL(connection, "plain-");
		} catch (Exception x) {
			throw new RuntimeException(x);
		}

		return true;
	}

	@Override
	public void init() throws SQLException {

		Connection connection = defaultDataSource.getConnection();

		DatabaseMetaData md = connection.getMetaData();
		ResultSet rs = md.getTables(null, null, "%", null);
		Collection<String> tableNames = new HashSet<String>();
		while (rs.next()) {
			tableNames.add(rs.getString(3).toLowerCase());
		}

		if (!tableNames.contains(tableName)) {
			Statement statement = connection.createStatement();
			statement.execute(String.format(
					"CREATE TABLE %s (name VARCHAR(255))", tableName));
		}
	}

	@Override
	public void testRollbackOnException(UUID id, boolean throwException)
			throws Exception {

		Connection connection = defaultDataSource.getConnection();

		storeId(connection, id);

		if (throwException)
			throw new TestRollbackException(
					"Legal exception for test purposes. Object "
							+ id.toString() + " should now be deleted!");
	}

	@Override
	public void testRollbackOnNestedTransactionException(UUID id1, UUID id2,
			boolean throwExceptionInMainTransaction,
			boolean throwExceptionInNestedTransaction) throws Exception {

		Connection connection = defaultDataSource.getConnection();

		storeId(connection, id1);

		boolean expectedExceptionThrown = false;
		try {
			plainDataSourceNewTransactionBean.testRollback(connection, id2,
					throwExceptionInNestedTransaction);
		} catch (Exception x) {
			int index = ExceptionUtils.indexOfThrowable(x,
					TestRollbackException.class);
			if (index >= 0)
				expectedExceptionThrown = true;
			else
				throw x;
		}

		if (throwExceptionInNestedTransaction && !expectedExceptionThrown)
			logger.error("TestRollbackException was not thrown!",
					expectedExceptionThrown);

		if (throwExceptionInMainTransaction)
			throw new TestRollbackException(
					"Legal exception in main transaction for test purposes. Object "
							+ id1.toString() + " should now be deleted!");
		else
			logger.info("Main transaction ended without throwing an exception!");

	}

	@Override
	public boolean keyExists(UUID id) throws SQLException {

		Connection connection = defaultDataSource.getConnection();

		PreparedStatement queryStatement = connection.prepareStatement(String
				.format("SELECT * FROM %s WHERE name='%s'", tableName,
						id.toString()));
		ResultSet resultSet = queryStatement.executeQuery();
		return resultSet.next();

	}

	@Override
	public boolean isAvailable() {
		return true;
	}
}
