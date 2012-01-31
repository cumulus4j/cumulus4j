/*
 * Cumulus4j - Securing your data in the cloud - http://cumulus4j.org
 * Copyright (C) 2011 NightLabs Consulting GmbH
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.cumulus4j.store.test.account;

import java.util.Arrays;
import java.util.Map;
import java.util.Map.Entry;

import javax.jdo.JDOObjectNotFoundException;
import javax.jdo.PersistenceManager;
import javax.jdo.PersistenceManagerFactory;

import org.cumulus4j.store.test.account.id.AnchorID;
import org.cumulus4j.store.test.account.id.LocalAccountantDelegateID;
import org.cumulus4j.store.test.framework.AbstractJDOTransactionalTest;
import org.cumulus4j.store.test.framework.CleanupUtil;
import org.cumulus4j.store.test.framework.JDOTransactionalRunner;
import org.junit.After;
import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class JDOPersistenceTest
extends AbstractJDOTransactionalTest
{
	private static final String EUR = "EUR";

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
			localAccountantDelegate.setAccount(EUR, account);
			pm.makePersistent(localAccountantDelegate); // this should implicitly persist the account
		}

		commitAndBeginNewTransaction();

		{
			LocalAccountantDelegate localAccountantDelegate = (LocalAccountantDelegate) pm.getObjectById(LOCAL_ACCOUNTANT_DELEGATE_ID_0);
			localAccountantDelegate.test();

			Account account = (Account) pm.getObjectById(ACCOUNT_ID_0);
			account.getBalance();
		}
	}

	private static <K, V> Map.Entry<K, V> iterateMapAndGetMapEntryForKey(Map<K, V> map, K key)
	{
		for (Map.Entry<K, V> me : map.entrySet()) {
			if (key == null && me.getKey() == null)
				return me;

			if (key != null && key.equals(me.getKey()))
				return me;
		}

		return null;
	}

	@Test
	@Ignore
	public void createData_HeisenbugAnalysis()
	{
		int runCounter = 0;
		int createDataFailedCounter = 0;
		int[] mapGetReturnedNullCounter = new int[] { 0, 0, 0 };
		int[] mapEntryMissingCounter = new int[] { 0, 0, 0 };
		int[] mapEntryValueIsNullCounter = new int[] { 0, 0, 0 };
		int[] accountIsNotFoundCounter = new int[] { 0, 0, 0 };

		for (int i = 0; i < 100; ++i) {
			++runCounter;
			try {
				createData();
			} catch (Exception x) {
				++createDataFailedCounter;

				{
					int counterIndex = 0;
					pm.getExtent(LocalAccountantDelegate.class);
					LocalAccountantDelegate localAccountantDelegate = (LocalAccountantDelegate) pm.getObjectById(LOCAL_ACCOUNTANT_DELEGATE_ID_0);

					if (localAccountantDelegate.getAccounts().get(EUR) == null)
						++mapGetReturnedNullCounter[counterIndex];

					Entry<String, Account> mapEntry = iterateMapAndGetMapEntryForKey(localAccountantDelegate.getAccounts(), EUR);
					if (mapEntry == null)
						++mapEntryMissingCounter[counterIndex];
					else if (mapEntry.getValue() == null)
						++mapEntryValueIsNullCounter[counterIndex];

					try {
						pm.getExtent(Account.class);
						pm.getObjectById(ACCOUNT_ID_0);
					} catch (JDOObjectNotFoundException onfe) {
						++accountIsNotFoundCounter[counterIndex];
					}
				}

				commitAndBeginNewTransaction();

				{
					int counterIndex = 1;
					pm.getExtent(LocalAccountantDelegate.class);
					LocalAccountantDelegate localAccountantDelegate = (LocalAccountantDelegate) pm.getObjectById(LOCAL_ACCOUNTANT_DELEGATE_ID_0);

					if (localAccountantDelegate.getAccounts().get(EUR) == null)
						++mapGetReturnedNullCounter[counterIndex];

					Entry<String, Account> mapEntry = iterateMapAndGetMapEntryForKey(localAccountantDelegate.getAccounts(), EUR);
					if (mapEntry == null)
						++mapEntryMissingCounter[counterIndex];
					else if (mapEntry.getValue() == null)
						++mapEntryValueIsNullCounter[counterIndex];

					try {
						pm.getExtent(Account.class);
						pm.getObjectById(ACCOUNT_ID_0);
					} catch (JDOObjectNotFoundException onfe) {
						++accountIsNotFoundCounter[counterIndex];
					}
				}

				commitAndBeginNewTransaction();


				PersistenceManagerFactory pmf2 = JDOTransactionalRunner.createPersistenceManagerFactory();
				PersistenceManager pm2 = pmf2.getPersistenceManager();
				JDOTransactionalRunner.setEncryptionCoordinates(pm2);
				pm2.currentTransaction().begin();

				{
					int counterIndex = 2;
					pm2.getExtent(LocalAccountantDelegate.class);
					LocalAccountantDelegate localAccountantDelegate = (LocalAccountantDelegate) pm2.getObjectById(LOCAL_ACCOUNTANT_DELEGATE_ID_0);

					if (localAccountantDelegate.getAccounts().get(EUR) == null)
						++mapGetReturnedNullCounter[counterIndex];

					Entry<String, Account> mapEntry = iterateMapAndGetMapEntryForKey(localAccountantDelegate.getAccounts(), EUR);
					if (mapEntry == null)
						++mapEntryMissingCounter[counterIndex];
					else if (mapEntry.getValue() == null)
						++mapEntryValueIsNullCounter[counterIndex];

					try {
						pm2.getExtent(Account.class);
						pm2.getObjectById(ACCOUNT_ID_0);
					} catch (JDOObjectNotFoundException onfe) {
						++accountIsNotFoundCounter[counterIndex];
					}
				}

				pm2.currentTransaction().rollback();
				pm2.close();
				pmf2.close();
			}

			deleteAll();
		}

		if (createDataFailedCounter > 0)
			throw new RuntimeException(
					String.format(
							"runCounter=%s createDataFailedCounter=%s mapGetReturnedNullCounter=%s mapEntryMissingCounter=%s mapEntryValueIsNullCounter=%s accountIsNotFoundCounter=%s",
							runCounter,
							createDataFailedCounter,
							Arrays.toString(mapGetReturnedNullCounter),
							Arrays.toString(mapEntryMissingCounter),
							Arrays.toString(mapEntryValueIsNullCounter),
							Arrays.toString(accountIsNotFoundCounter)
					)
			);
	}

	private static final void doNothing() { }

	@After
    public void deleteAll()
    {
		LocalAccountantDelegate localAccountantDelegate = null;
		try {
			localAccountantDelegate = (LocalAccountantDelegate) pm.getObjectById(LOCAL_ACCOUNTANT_DELEGATE_ID_0);
		} catch (JDOObjectNotFoundException x) { doNothing(); }

		if (localAccountantDelegate != null)
			pm.deletePersistent(localAccountantDelegate);

		Account account = null;
		try {
			account = (Account) pm.getObjectById(ACCOUNT_ID_0);
		} catch (JDOObjectNotFoundException x) { doNothing(); }

		if (account != null)
			pm.deletePersistent(account);
    }
}
