package org.cumulus4j.jee.test.ejb.plaindatasource;

import java.sql.SQLException;
import java.util.UUID;

import javax.ejb.Remote;

@Remote
public interface DefaultDataSourceTestRemote {

	boolean testDataStoreConnection() throws SQLException;

	void testRollbackOnNestedTransactionException(UUID id1, UUID id2,
			boolean throwExceptionInMainTransaction,
			boolean throwExceptionInNestedTransaction) throws Exception;

	boolean keyExists(UUID id) throws SQLException;

	boolean isAvailable();

	void testRollbackOnException(UUID id, boolean throwException)
			throws Exception;

	void init() throws SQLException;
}
