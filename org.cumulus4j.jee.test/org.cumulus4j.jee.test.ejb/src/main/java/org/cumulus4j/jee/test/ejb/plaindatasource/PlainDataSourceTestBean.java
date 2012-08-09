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

import javax.ejb.EJB;
import javax.ejb.Stateless;
import javax.ejb.TransactionAttribute;
import javax.ejb.TransactionAttributeType;

import org.apache.commons.lang.exception.ExceptionUtils;
import org.cumulus4j.jee.test.ejb.TestRollbackException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Stateless
@TransactionAttribute(TransactionAttributeType.REQUIRED)
public class PlainDataSourceTestBean extends AbstractPlainDataSourceTestBean
		implements PlainDataSourceTestRemote {

	private static final Logger logger = LoggerFactory
			.getLogger(PlainDataSourceTestBean.class);

	@EJB
	private PlainDataSourceNewTransactionBean plainDataSourceNewTransactionBean;

	@EJB
	private PlainDataSourceSharedTransactionBean plainDataSourceSharedTransactionBean;

	@Override
	public void init(String... args) throws SQLException {

		logger.info("Creating test tables");

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
	public boolean isAvailable() {
		return true;
	}

	@Override
	public void testRollbackOnException(UUID id, boolean throwException)
			throws Exception {

		storeId(id);

		if (throwException)
			throw new TestRollbackException(
					"Legal exception for test purposes. Object "
							+ id.toString() + " should now be deleted!");
	}

	@Override
	public void testRollbackOnExceptionWithNestedTransaction(UUID id1, UUID id2,
			boolean throwExceptionInMainBean,
			boolean throwExceptionInNestedBeanCall) throws Exception {

		testRollback(id1, id2, throwExceptionInMainBean,
				throwExceptionInNestedBeanCall, true);
	}

	@Override
	public void testRollbackOnExceptionWithSharedTransaction(UUID id1, UUID id2,
			boolean throwExceptionInMainBean,
			boolean throwExceptionInNestedBeanCall) throws Exception {

		testRollback(id1, id2, throwExceptionInMainBean,
				throwExceptionInNestedBeanCall, false);
	}

	private void testRollback(UUID id1, UUID id2,
			boolean throwExceptionInMainBean,
			boolean throwExceptionInNestedBeanCall, boolean nestedTransaction)
			throws Exception {

		storeId(id1);

		boolean expectedExceptionThrown = false;
		try {
			if (nestedTransaction)
				plainDataSourceNewTransactionBean.testRollback(id2,
						throwExceptionInNestedBeanCall);
			else
				plainDataSourceSharedTransactionBean.testRollback(id2,
						throwExceptionInNestedBeanCall);
		} catch (Exception x) {
			int index = ExceptionUtils.indexOfThrowable(x,
					TestRollbackException.class);
			if (index >= 0)
				expectedExceptionThrown = true;
			else
				throw x;
		}

		if (throwExceptionInNestedBeanCall && !expectedExceptionThrown)
			logger.error("TestRollbackException was not thrown!",
					expectedExceptionThrown);

		if (throwExceptionInMainBean)
			throw new TestRollbackException(
					"Legal exception in main transaction for test purposes. Object "
							+ id1.toString() + " should now be deleted!");
		else
			logger.info("Main transaction ended without throwing an exception!");
	}

	@Override
	public boolean objectExists(UUID id) throws SQLException {

		Connection connection = defaultDataSource.getConnection();

		PreparedStatement queryStatement = connection.prepareStatement(String
				.format("SELECT * FROM %s WHERE name='%s'", tableName,
						id.toString()));
		ResultSet resultSet = queryStatement.executeQuery();

		return resultSet.next();
	}

}
