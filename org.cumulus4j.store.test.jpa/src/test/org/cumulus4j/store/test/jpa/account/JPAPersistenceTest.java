package org.cumulus4j.store.test.jpa.account;

import org.cumulus4j.store.test.jpa.account.Account;
import org.cumulus4j.store.test.jpa.account.LocalAccountantDelegate;
import org.cumulus4j.store.test.jpa.account.id.AnchorID;
import org.cumulus4j.store.test.jpa.account.id.LocalAccountantDelegateID;
import org.cumulus4j.store.test.jpa.AbstractJPATransactionalTest;
import org.cumulus4j.store.test.jpa.CleanupUtil;
import org.junit.BeforeClass;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class JPAPersistenceTest
extends AbstractJPATransactionalTest
{
	private static final Logger logger = LoggerFactory.getLogger(JPAPersistenceTest.class);

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
			em.persist(localAccountantDelegate); // this should implicitely persist the account
		}

		commitAndBeginNewTransaction();

		{
			LocalAccountantDelegate localAccountantDelegate = 
			    (LocalAccountantDelegate) em.find(LocalAccountantDelegate.class, LOCAL_ACCOUNTANT_DELEGATE_ID_0);
			localAccountantDelegate.test();

			Account account = (Account) em.find(Account.class, ACCOUNT_ID_0);
			account.getBalance();
		}
	}
}