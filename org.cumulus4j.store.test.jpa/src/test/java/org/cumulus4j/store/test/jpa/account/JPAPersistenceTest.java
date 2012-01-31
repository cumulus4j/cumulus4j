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
package org.cumulus4j.store.test.jpa.account;

import java.util.Arrays;
import java.util.Map;
import java.util.Map.Entry;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;

import org.cumulus4j.store.test.framework.CleanupUtil;
import org.cumulus4j.store.test.jpa.AbstractJPATransactionalTest;
import org.cumulus4j.store.test.jpa.JPATransactionalRunner;
import org.cumulus4j.store.test.jpa.account.id.AnchorID;
import org.cumulus4j.store.test.jpa.account.id.LocalAccountantDelegateID;
import org.junit.After;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class JPAPersistenceTest
extends AbstractJPATransactionalTest
{
	private static final String EUR = "EUR";

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
			localAccountantDelegate.setAccount(EUR, account);
			em.persist(localAccountantDelegate); // this should implicitely persist the account
		}

		commitAndBeginNewTransaction();

		{
			LocalAccountantDelegate localAccountantDelegate =
			    em.find(LocalAccountantDelegate.class, LOCAL_ACCOUNTANT_DELEGATE_ID_0);
			localAccountantDelegate.test();

			Account account = em.find(Account.class, ACCOUNT_ID_0);
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
					LocalAccountantDelegate localAccountantDelegate = em.find(LocalAccountantDelegate.class, LOCAL_ACCOUNTANT_DELEGATE_ID_0);

					if (localAccountantDelegate.getAccounts().get(EUR) == null)
						++mapGetReturnedNullCounter[counterIndex];

					Entry<String, Account> mapEntry = iterateMapAndGetMapEntryForKey(localAccountantDelegate.getAccounts(), EUR);
					if (mapEntry == null)
						++mapEntryMissingCounter[counterIndex];
					else if (mapEntry.getValue() == null)
						++mapEntryValueIsNullCounter[counterIndex];

					if (em.find(Account.class, ACCOUNT_ID_0) == null)
						++accountIsNotFoundCounter[counterIndex];
				}

				commitAndBeginNewTransaction();

				{
					int counterIndex = 1;
					LocalAccountantDelegate localAccountantDelegate = em.find(LocalAccountantDelegate.class, LOCAL_ACCOUNTANT_DELEGATE_ID_0);

					if (localAccountantDelegate.getAccounts().get(EUR) == null)
						++mapGetReturnedNullCounter[counterIndex];

					Entry<String, Account> mapEntry = iterateMapAndGetMapEntryForKey(localAccountantDelegate.getAccounts(), EUR);
					if (mapEntry == null)
						++mapEntryMissingCounter[counterIndex];
					else if (mapEntry.getValue() == null)
						++mapEntryValueIsNullCounter[counterIndex];

					if (em.find(Account.class, ACCOUNT_ID_0) == null)
						++accountIsNotFoundCounter[counterIndex];
				}

				commitAndBeginNewTransaction();


				EntityManagerFactory emf2 = JPATransactionalRunner.createEntityManagerFactory();
				EntityManager em2 = emf2.createEntityManager();
				JPATransactionalRunner.setEncryptionCoordinates(em2);
				em2.getTransaction().begin();

				{
					int counterIndex = 2;
					LocalAccountantDelegate localAccountantDelegate = em2.find(LocalAccountantDelegate.class, LOCAL_ACCOUNTANT_DELEGATE_ID_0);

					if (localAccountantDelegate.getAccounts().get(EUR) == null)
						++mapGetReturnedNullCounter[counterIndex];

					Entry<String, Account> mapEntry = iterateMapAndGetMapEntryForKey(localAccountantDelegate.getAccounts(), EUR);
					if (mapEntry == null)
						++mapEntryMissingCounter[counterIndex];
					else if (mapEntry.getValue() == null)
						++mapEntryValueIsNullCounter[counterIndex];

					if (em2.find(Account.class, ACCOUNT_ID_0) == null)
						++accountIsNotFoundCounter[counterIndex];
				}

				em2.getTransaction().rollback();
				em2.close();
				emf2.close();
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

    @Test
    public void updateData()
    {
        {
            Account account = new Account(ACCOUNT_ID_0);
            LocalAccountantDelegate localAccountantDelegate = new LocalAccountantDelegate(LOCAL_ACCOUNTANT_DELEGATE_ID_0);
            localAccountantDelegate.setAccount(EUR, account);
            em.persist(localAccountantDelegate); // this should implicitely persist the account
        }

        commitAndBeginNewTransaction();

        String name = "Test 0000";
        String description = "This is a very long description bla bla bla trallalala tröt tröt. And " +
        "I don't know exactly what I should write here, bla bla bla, but it should " +
        "be really really long! Very likely this is sufficient now, but I'd better " +
        "add some more words.\n\n" +
        "\n" +
        "Freude, schöner Götterfunken,\n" +
        "Tochter aus Elisium,\n" +
        "Wir betreten feuertrunken\n" +
        "Himmlische, dein Heiligthum.\n" +
        "Deine Zauber binden wieder,\n" +
        "was der Mode Schwerd getheilt;\n" +
        "Bettler werden Fürstenbrüder,\n" +
        "wo dein sanfter Flügel weilt.\n" +
        "\n" +
        "Seid umschlungen, Millionen!\n" +
        "Diesen Kuß der ganzen Welt!\n" +
        "Brüder – überm Sternenzelt\n" +
        "muß ein lieber Vater wohnen.\n" +
        "\n" +
        "Wem der große Wurf gelungen,\n" +
        "eines Freundes Freund zu seyn;\n" +
        "wer ein holdes Weib errungen,\n" +
        "mische seinen Jubel ein!\n" +
        "Ja – wer auch nur eine Seele\n" +
        "sein nennt auf dem Erdenrund!\n" +
        "Und wer’s nie gekonnt, der stehle\n" +
        "weinend sich aus diesem Bund!\n";

        LocalAccountantDelegate localAccountantDelegate =
            em.find(LocalAccountantDelegate.class, LOCAL_ACCOUNTANT_DELEGATE_ID_0);
        localAccountantDelegate.setName(name);
        localAccountantDelegate.setDescription(description);

        commitAndBeginNewTransaction();

        localAccountantDelegate =
            em.find(LocalAccountantDelegate.class, LOCAL_ACCOUNTANT_DELEGATE_ID_0);
        Assert.assertEquals(name, localAccountantDelegate.getName());
        Assert.assertEquals(description, localAccountantDelegate.getDescription());
    }

    @After
    public void deleteAll()
    {
        LocalAccountantDelegate localAccountantDelegate = em.find(LocalAccountantDelegate.class, LOCAL_ACCOUNTANT_DELEGATE_ID_0);
        if (localAccountantDelegate != null)
        	em.remove(localAccountantDelegate);

        Account account = em.find(Account.class, ACCOUNT_ID_0);
        if (account != null)
        	em.remove(account);
    }
}