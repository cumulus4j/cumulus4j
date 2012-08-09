package org.cumulus4j.jee.test.ejb;

import java.sql.SQLException;
import java.util.UUID;

public interface RollbackTestRemote {

//	public void init(String cryptoSeccionID, String cryptoManagerID) throws SQLException;
	public void init(String... args) throws Exception;

	public void testRollbackOnException(UUID id, boolean throwException)
			throws Exception;

	public void testRollbackOnExceptionWithNestedTransaction(UUID id1, UUID id2,
			boolean throwExceptionInMainBean,
			boolean throwExceptionInNestedBeanCall) throws Exception;

	public void testRollbackOnExceptionWithSharedTransaction(UUID id1, UUID id2,
			boolean throwExceptionInMainBean,
			boolean throwExceptionInNestedBeanCall) throws Exception;

	public boolean objectExists(UUID id) throws SQLException;

	public boolean isAvailable();
}