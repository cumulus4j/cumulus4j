package org.cumulus4j.store.test.account;

import org.cumulus4j.store.test.account.Account;
import org.cumulus4j.store.test.account.LocalAccountantDelegate;
import org.cumulus4j.store.test.account.id.AnchorID;
import org.cumulus4j.store.test.account.id.LocalAccountantDelegateID;
import org.cumulus4j.store.test.framework.AbstractTransactionalTest;
import org.cumulus4j.store.test.framework.CleanupUtil;
import org.junit.BeforeClass;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class JDOPersistenceTest
extends AbstractTransactionalTest
{
	private static final Logger logger = LoggerFactory.getLogger(JDOPersistenceTest.class);

	private static final String ORGANISATION_ID = "jfire.my.org";
	private static final LocalAccountantDelegateID LOCAL_ACCOUNTANT_DELEGATE_ID_0 = LocalAccountantDelegateID.create(ORGANISATION_ID, "0");
	private static final AnchorID ACCOUNT_ID_0 = AnchorID.create(ORGANISATION_ID, Account.ANCHOR_TYPE_ID_ACCOUNT, "voucher.00");

	@BeforeClass
	public static void clearDatabase()
	throws Exception
	{
		logger.info("clearDatabase: Clearing database (dropping all tables).");
		CleanupUtil.dropAllTables();
	}

	@Test
	public void createData()
	{
		{
			Account account = new Account(ACCOUNT_ID_0);
			LocalAccountantDelegate localAccountantDelegate = new LocalAccountantDelegate(LOCAL_ACCOUNTANT_DELEGATE_ID_0);
			localAccountantDelegate.setAccount("EUR", account);
			pm.makePersistent(localAccountantDelegate); // this should implicitely persist the account
		}

		commitAndBeginNewTransaction();

		{
			LocalAccountantDelegate localAccountantDelegate = (LocalAccountantDelegate) pm.getObjectById(LOCAL_ACCOUNTANT_DELEGATE_ID_0);
			localAccountantDelegate.test();

			Account account = (Account) pm.getObjectById(ACCOUNT_ID_0);
			account.getBalance();
		}
	}
}
