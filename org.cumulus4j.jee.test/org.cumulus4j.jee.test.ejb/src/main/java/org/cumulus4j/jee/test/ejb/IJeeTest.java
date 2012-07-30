package org.cumulus4j.jee.test.ejb;

import java.sql.SQLException;
import java.util.UUID;

public interface IJeeTest {

//	public boolean testDataStoreConnection() throws SQLException;

	public void init() throws SQLException;

	public void testRollbackOnException(UUID id, boolean throwException)
			throws Exception;

	public void testRollbackOnNestedTransactionException(UUID id1, UUID id2,
			boolean throwExceptionInMainBean,
			boolean throwExceptionInNestedBeanCall) throws Exception;

	public void testRollbackWithSharedTransaction(UUID id1, UUID id2,
			boolean throwExceptionInMainBean,
			boolean throwExceptionInNestedBeanCall) throws Exception;

	public boolean objectExists(UUID id) throws SQLException;

	public boolean isAvailable();
}
