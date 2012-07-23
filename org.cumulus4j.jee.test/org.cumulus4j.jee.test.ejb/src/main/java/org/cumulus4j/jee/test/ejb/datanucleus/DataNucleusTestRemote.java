package org.cumulus4j.jee.test.ejb.datanucleus;

import java.util.UUID;

import javax.ejb.Remote;

@Remote
public interface DataNucleusTestRemote {

	void testRollbackOnException(UUID id, boolean throwException)
			throws Exception;

	void testRollbackOnNestedTransactionException(UUID id1, UUID id2,
			boolean throwExceptionInMainTransaction,
			boolean throwExceptionInNestedTransaction) throws Exception;

	boolean objectExists(UUID id);

	boolean isAvailable();

	void testRollbackWithSharedTransaction(UUID id1, UUID id2,
			boolean throwExceptionInMainTransaction,
			boolean throwExceptionInNestedTransaction) throws Exception;

}
