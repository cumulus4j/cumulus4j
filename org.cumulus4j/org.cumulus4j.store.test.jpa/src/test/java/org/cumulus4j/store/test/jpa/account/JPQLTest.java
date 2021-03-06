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

import java.io.IOException;
import java.util.Calendar;
import java.util.List;

import javax.persistence.Query;

import org.cumulus4j.store.test.framework.CleanupUtil;
import org.cumulus4j.store.test.jpa.AbstractJPATransactionalTest;
import org.cumulus4j.store.test.jpa.account.id.AnchorID;
import org.cumulus4j.store.test.jpa.account.id.LocalAccountantDelegateID;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class JPQLTest
extends AbstractJPATransactionalTest
{
	private static final Logger logger = LoggerFactory.getLogger(JPQLTest.class);

	private static final String ORGANISATION_ID = "jfire.my.org";
	private static final LocalAccountantDelegateID LOCAL_ACCOUNTANT_DELEGATE_ID_0 = LocalAccountantDelegateID.create(ORGANISATION_ID, "0");
	private static final AnchorID ACCOUNT_ID_0 = AnchorID.create(ORGANISATION_ID, Account.ANCHOR_TYPE_ID_ACCOUNT, "voucher.00");
	private static final AnchorID ACCOUNT_ID_1 = AnchorID.create(ORGANISATION_ID, Account.ANCHOR_TYPE_ID_ACCOUNT, "voucher.01");
	private static final LocalAccountantDelegateID LOCAL_ACCOUNTANT_DELEGATE_ID_1 = LocalAccountantDelegateID.create(ORGANISATION_ID, "1");
	private static final AnchorID ACCOUNT_ID_2 = AnchorID.create(ORGANISATION_ID, Account.ANCHOR_TYPE_ID_ACCOUNT, "voucher.02");

	@BeforeClass
	public static void clearDatabase()
	throws Exception
	{
		logger.info("clearDatabase: Clearing database (dropping all tables).");
		CleanupUtil.dropAllTables();
	}

	@Before
	public void createData()
	{
		Account account = new Account(ACCOUNT_ID_0);
		LocalAccountantDelegate localAccountantDelegate = new LocalAccountantDelegate(LOCAL_ACCOUNTANT_DELEGATE_ID_0);
		localAccountantDelegate.setAccount("EUR", account);
		em.persist(localAccountantDelegate); // this should implicitely persist the account

		localAccountantDelegate.setAccount("CHF", new Account(ACCOUNT_ID_1));
		localAccountantDelegate.setName("New test bla bla bla.");
		localAccountantDelegate.setName2("2nd name");
		localAccountantDelegate.setDescription("description");
		Calendar cal = Calendar.getInstance();
		cal.set(Calendar.YEAR, 2011);
		cal.set(Calendar.DAY_OF_MONTH, 25);
		cal.set(Calendar.MONTH, 5);
		localAccountantDelegate.setCreationDate(cal.getTime());

		Account account2 = new Account(ACCOUNT_ID_2);
		LocalAccountantDelegate localAccountantDelegate1 = new LocalAccountantDelegate(LOCAL_ACCOUNTANT_DELEGATE_ID_1);
		localAccountantDelegate1.setAccount("GBP", account2);
		em.persist(localAccountantDelegate1); // this should implicitely persist the account
		localAccountantDelegate1.setName("Some other test");
		localAccountantDelegate1.setName2("Second name");
		localAccountantDelegate1.setDescription("description2");
		Calendar cal2 = Calendar.getInstance();
		cal2.set(Calendar.YEAR, 2010);
		cal2.set(Calendar.DAY_OF_MONTH, 5);
		cal2.set(Calendar.MONTH, 3);
		localAccountantDelegate1.setCreationDate(cal2.getTime());
	}

	private void assertDelegate0(LocalAccountantDelegate delegate)
	{
		Assert.assertNotNull(delegate);
		Assert.assertEquals("name is wrong", "New test bla bla bla.", delegate.getName());
		Assert.assertEquals("description is wrong", "description", delegate.getDescription());
		Assert.assertEquals("name2 is wrong", "2nd name", delegate.getName2());
		Assert.assertEquals("description is wrong", "description", delegate.getDescription());
	}

	/*private void assertDelegate1(LocalAccountantDelegate delegate)
	{
		Assert.assertNotNull(delegate);
		Assert.assertEquals("name is wrong", "Some other test", delegate.getName());
		Assert.assertEquals("description is wrong", "description2", delegate.getDescription());
		Assert.assertEquals("name2 is wrong", "Second name", delegate.getName2());
		Assert.assertEquals("description is wrong", "description2", delegate.getDescription());
	}*/

	@Test
	public void queryStringEquals() throws IOException
	{
		Query q = em.createQuery("SELECT d FROM " + LocalAccountantDelegate.class.getName() + " d WHERE name = :name");

        // Positive test
		q.setParameter("name", "New test bla bla bla.");
		@SuppressWarnings("unchecked")
		List<LocalAccountantDelegate> result = q.getResultList();
		Assert.assertEquals("Number of results was wrong", 1, result.size());
		LocalAccountantDelegate delegate = result.iterator().next();
		assertDelegate0(delegate);

		// Negative test
		q.setParameter("name", "New test bla bla bla2");
		@SuppressWarnings("unchecked")
		List<LocalAccountantDelegate> l = q.getResultList();
		result = l;
		Assert.assertEquals("Number of results was wrong", 0, result.size());
	}

	@After
	public void deleteAll() throws IOException
	{
		LocalAccountantDelegate localAccountantDelegate =
		    em.find(LocalAccountantDelegate.class, LOCAL_ACCOUNTANT_DELEGATE_ID_0);
		em.remove(localAccountantDelegate);

		Account account = em.find(Account.class, ACCOUNT_ID_0);
		em.remove(account);

		account = em.find(Account.class, ACCOUNT_ID_1);
		em.remove(account);

		localAccountantDelegate = em.find(LocalAccountantDelegate.class, LOCAL_ACCOUNTANT_DELEGATE_ID_1);
		em.remove(localAccountantDelegate);

		account = em.find(Account.class, ACCOUNT_ID_2);
		em.remove(account);
	}
}
