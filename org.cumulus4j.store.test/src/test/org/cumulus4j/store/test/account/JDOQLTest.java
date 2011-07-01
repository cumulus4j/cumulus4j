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

import java.io.IOException;
import java.util.Calendar;
import java.util.Currency;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.jdo.JDOHelper;
import javax.jdo.Query;

import org.cumulus4j.store.test.account.id.AnchorID;
import org.cumulus4j.store.test.account.id.LocalAccountantDelegateID;
import org.cumulus4j.store.test.framework.AbstractJDOTransactionalTest;
import org.cumulus4j.store.test.framework.CleanupUtil;
import org.datanucleus.NucleusContext;
import org.datanucleus.api.jdo.JDOPersistenceManagerFactory;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class JDOQLTest
extends AbstractJDOTransactionalTest
{
	private static final Logger logger = LoggerFactory.getLogger(JDOQLTest.class);

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
		account.setCurrency(Currency.getInstance("EUR"));
		LocalAccountantDelegate localAccountantDelegate = new LocalAccountantDelegate(LOCAL_ACCOUNTANT_DELEGATE_ID_0);
		localAccountantDelegate.setAccount("EUR", account);
		pm.makePersistent(localAccountantDelegate); // this should implicitely persist the account

		Account account1 = new Account(ACCOUNT_ID_1);
		account1.setCurrency(Currency.getInstance("CHF"));
		localAccountantDelegate.setAccount("CHF", account1);
		localAccountantDelegate.setName("New test bla bla bla.");
		localAccountantDelegate.setName2("2nd name");
		localAccountantDelegate.setDescription("description");
		Calendar cal = Calendar.getInstance();
		cal.set(Calendar.YEAR, 2011);
		cal.set(Calendar.DAY_OF_MONTH, 25);
		cal.set(Calendar.MONTH, 5);
		localAccountantDelegate.setCreationDate(cal.getTime());

		Account account2 = new Account(ACCOUNT_ID_2);
		account2.setCurrency(Currency.getInstance("GBP"));
		LocalAccountantDelegate localAccountantDelegate1 = new LocalAccountantDelegate(LOCAL_ACCOUNTANT_DELEGATE_ID_1);
		localAccountantDelegate1.setAccount("GBP", account2);
		pm.makePersistent(localAccountantDelegate1); // this should implicitely persist the account
		localAccountantDelegate1.setName("Some other test");
		localAccountantDelegate1.setName2("Second name");
		localAccountantDelegate1.setDescription("description2");
		Calendar cal2 = Calendar.getInstance();
		cal2.set(Calendar.YEAR, 2010);
		cal2.set(Calendar.DAY_OF_MONTH, 5);
		cal2.set(Calendar.MONTH, 3);
		localAccountantDelegate1.setCreationDate(cal2.getTime());
	}

	@Test
	public void updateData() throws IOException
	{
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

		LocalAccountantDelegate localAccountantDelegate = (LocalAccountantDelegate) pm.getObjectById(LOCAL_ACCOUNTANT_DELEGATE_ID_0);
		localAccountantDelegate.setName(name);
		localAccountantDelegate.setDescription(description);

		commitAndBeginNewTransaction();

		localAccountantDelegate = (LocalAccountantDelegate) pm.getObjectById(LOCAL_ACCOUNTANT_DELEGATE_ID_0);
		Assert.assertEquals(name, localAccountantDelegate.getName());
		Assert.assertEquals(description, localAccountantDelegate.getDescription());
	}

	@Test
	public void extentIterator() throws IOException
	{
		Iterator<LocalAccountantDelegate> delegateIter = pm.getExtent(LocalAccountantDelegate.class).iterator();
		Assert.assertTrue(delegateIter.hasNext());
		LocalAccountantDelegate localAccountantDelegate = delegateIter.next();
		Assert.assertTrue(delegateIter.hasNext());
		LocalAccountantDelegate localAccountantDelegate1 = delegateIter.next();
		Assert.assertFalse(delegateIter.hasNext());

		boolean delegate_0_found = false;
		boolean delegate_1_found = false;
		if (localAccountantDelegate.getName().equals("New test bla bla bla."))
		{
			delegate_0_found = true;
			assertDelegate0(localAccountantDelegate);
		}
		else if (localAccountantDelegate1.getName().equals("New test bla bla bla."))
		{
			delegate_0_found = true;
			assertDelegate0(localAccountantDelegate1);
		}
		if (localAccountantDelegate.getName().equals("Some other test"))
		{
			delegate_1_found = true;
			assertDelegate1(localAccountantDelegate);
		}
		else if (localAccountantDelegate1.getName().equals("Some other test"))
		{
			delegate_1_found = true;
			assertDelegate1(localAccountantDelegate1);
		}

		Assert.assertTrue("Delegate 0 was not returned", delegate_0_found);
		Assert.assertTrue("Delegate 1 was not returned", delegate_1_found);
	}

	private void assertDelegate0(LocalAccountantDelegate delegate)
	{
		Assert.assertNotNull(delegate);
		Assert.assertEquals("name is wrong", "New test bla bla bla.", delegate.getName());
		Assert.assertEquals("description is wrong", "description", delegate.getDescription());
		Assert.assertEquals("name2 is wrong", "2nd name", delegate.getName2());
		Assert.assertEquals("description is wrong", "description", delegate.getDescription());
	}

	private void assertDelegate1(LocalAccountantDelegate delegate)
	{
		Assert.assertNotNull(delegate);
		Assert.assertEquals("name is wrong", "Some other test", delegate.getName());
		Assert.assertEquals("description is wrong", "description2", delegate.getDescription());
		Assert.assertEquals("name2 is wrong", "Second name", delegate.getName2());
		Assert.assertEquals("description is wrong", "description2", delegate.getDescription());
	}

	@Test
	public void queryStringEquals() throws IOException
	{
		Query q = pm.newQuery(LocalAccountantDelegate.class);
		q.setFilter("this.name == :name");

        // Positive test
		List<LocalAccountantDelegate> result = (List<LocalAccountantDelegate>) q.execute("New test bla bla bla.");
		Assert.assertEquals("Number of results was wrong", 1, result.size());
		LocalAccountantDelegate delegate = result.iterator().next();
		assertDelegate0(delegate);

		// Negative test
		result = (List<LocalAccountantDelegate>) q.execute("New test bla bla bla2");
		Assert.assertEquals("Number of results was wrong", 0, result.size());
	}

	@Test
	public void queryAndWithTwoStringEquals_2shortStr() throws IOException
	{
		Query q = pm.newQuery(LocalAccountantDelegate.class);
		q.setFilter("this.name == :pName && this.name2 == :pName2");

		List<LocalAccountantDelegate> result = (List<LocalAccountantDelegate>) q.execute(
				"New test bla bla bla.", "2nd name");
		Assert.assertEquals("Number of results was wrong", 1, result.size());
		LocalAccountantDelegate delegate = result.iterator().next();
		assertDelegate0(delegate);
	}

	@Test
	public void queryAndWithTwoStringEquals_1clob() throws IOException
	{
		String propClobIndexingEnabled = "cumulus4j.index.clob.enabled";
		NucleusContext nucleusContext = ((JDOPersistenceManagerFactory)pm.getPersistenceManagerFactory()).getNucleusContext();
		Object valClobIndexingEnabled = nucleusContext.getPersistenceConfiguration().getProperty(propClobIndexingEnabled);
		if (Boolean.FALSE.toString().equals(valClobIndexingEnabled)) {
			logger.warn("queryAndWithTwoStringEquals_1clob: CLOB indexing is disabled (due to property '" + propClobIndexingEnabled + "') => Skipping this test!");
			return;
		}

		Query q = pm.newQuery(LocalAccountantDelegate.class);
		q.setFilter("this.name == :pName && this.description == :pDesc");

		List<LocalAccountantDelegate> result = (List<LocalAccountantDelegate>) q.execute(
				"New test bla bla bla.", "description");
		Assert.assertEquals("Number of results was wrong", 1, result.size());
		LocalAccountantDelegate delegate = result.iterator().next();
		assertDelegate0(delegate);
	}

	@Test
	public void queryStringIndexOf() throws IOException
	{
		Query q = pm.newQuery(LocalAccountantDelegate.class);
		q.setFilter("this.name.indexOf(:needle) >= 0");

		List<LocalAccountantDelegate> result = (List<LocalAccountantDelegate>) q.execute("bla");
		Assert.assertEquals("Number of results was wrong", 1, result.size());
		LocalAccountantDelegate delegate = result.iterator().next();
		assertDelegate0(delegate);
	}

	@Test
	public void queryStringStartsWith() throws IOException
	{
		// PrimaryExpression.startsWith(param)
		Query q = pm.newQuery(LocalAccountantDelegate.class);
		q.setFilter("this.name.startsWith(:startStr)");

		List<LocalAccountantDelegate> result = (List<LocalAccountantDelegate>) q.execute("New ");
		Assert.assertEquals("Number of results was wrong", 1, result.size());
		LocalAccountantDelegate delegate = result.iterator().next();
		assertDelegate0(delegate);

		// ParameterExpression.startsWith(primaryExpression)
		q = pm.newQuery(LocalAccountantDelegate.class);
		q.setFilter("param.startsWith(this.name)");
		q.declareParameters("java.lang.String param");
		Map params = new HashMap<String, Object>();
		params.put("param", "Some other test extra characters");
		result = (List<LocalAccountantDelegate>) q.executeWithMap(params);
		Assert.assertEquals("Number of results was wrong", 1, result.size());
		delegate = result.iterator().next();
		assertDelegate1(delegate);
	}

	@Test
	public void queryStringEndsWith() throws IOException
	{
		Query q = pm.newQuery(LocalAccountantDelegate.class);
		q.setFilter("this.name.endsWith(:endStr)");

		List<LocalAccountantDelegate> result = (List<LocalAccountantDelegate>) q.execute("bla.");
		Assert.assertEquals("Number of results was wrong", 1, result.size());
		LocalAccountantDelegate delegate = result.iterator().next();
		assertDelegate0(delegate);
	}

	@Test
	public void queryStringEndsWith2() throws IOException
	{
		// The second part here is not (currently Apr/2011) evaluatable in-datastore so tests partial evaluation
		Query q = pm.newQuery(LocalAccountantDelegate.class);
		q.setFilter("this.name.endsWith(:endStr) && this.name.substring(1).endsWith('bla.')");

		List<LocalAccountantDelegate> result = (List<LocalAccountantDelegate>) q.execute("bla.");
		Assert.assertEquals("Number of results was wrong", 1, result.size());
		LocalAccountantDelegate delegate = result.iterator().next();
		assertDelegate0(delegate);
	}

	@Test
	public void queryStringMatches() throws IOException
	{
		Query q = pm.newQuery(LocalAccountantDelegate.class);
		q.setFilter("this.name.matches(:endStr)");

		List<LocalAccountantDelegate> result = (List<LocalAccountantDelegate>) q.execute(".*bla.*");
		Assert.assertEquals("Number of results was wrong", 1, result.size());
		LocalAccountantDelegate delegate = result.iterator().next();
		assertDelegate0(delegate);

		result = (List<LocalAccountantDelegate>) q.execute("(?i).*BLA.*");
		Assert.assertEquals("Number of results was wrong", 1, result.size());
		delegate = result.iterator().next();
		assertDelegate0(delegate);
	}

	@Test
	public void queryStringToUpperCase() throws IOException
	{
		Query q = pm.newQuery(LocalAccountantDelegate.class);
		q.setFilter("this.name.toUpperCase() == :str");

		List<LocalAccountantDelegate> result = (List<LocalAccountantDelegate>) q.execute("SOME OTHER TEST");
		Assert.assertEquals("Number of results was wrong", 1, result.size());
		LocalAccountantDelegate delegate = result.iterator().next();
		assertDelegate1(delegate);
	}

	@Test
	public void queryStringToLowerCase() throws IOException
	{
		Query q = pm.newQuery(LocalAccountantDelegate.class);
		q.setFilter("this.name.toLowerCase() == :str");

		List<LocalAccountantDelegate> result = (List<LocalAccountantDelegate>) q.execute("some other test");
		Assert.assertEquals("Number of results was wrong", 1, result.size());
		LocalAccountantDelegate delegate = result.iterator().next();
		assertDelegate1(delegate);
	}

	@Test
	public void queryStringSubstring() throws IOException
	{
		Query q = pm.newQuery(LocalAccountantDelegate.class);
		q.setFilter("this.name.substring(5) == :str");

		List<LocalAccountantDelegate> result = (List<LocalAccountantDelegate>) q.execute("other test");
		Assert.assertEquals("Number of results was wrong", 1, result.size());
		LocalAccountantDelegate delegate = result.iterator().next();
		assertDelegate1(delegate);
		q.closeAll();

		q = pm.newQuery(LocalAccountantDelegate.class);
		q.setFilter("this.name.substring(5,10) == :str");

		result = (List<LocalAccountantDelegate>) q.execute("other");
		Assert.assertEquals("Number of results was wrong", 1, result.size());
		delegate = result.iterator().next();
		assertDelegate1(delegate);
	}

	@Test
	public void queryDateYear() throws IOException
	{
		Query q = pm.newQuery(LocalAccountantDelegate.class);
		q.setFilter("this.creationDate.getYear() == :yr");

		List<LocalAccountantDelegate> result = (List<LocalAccountantDelegate>) q.execute(2011);
		Assert.assertEquals("Number of results was wrong", 1, result.size());
		LocalAccountantDelegate delegate = result.iterator().next();
		assertDelegate0(delegate);
		q.closeAll();
	}

	@Test
	public void queryCollectionParameterContains() throws IOException
	{
		Query q = pm.newQuery(LocalAccountantDelegate.class);
		q.setFilter("param.contains(this.name)");
		q.declareParameters("java.util.Collection param");

		Set<String> inputStrings = new HashSet<String>();
		inputStrings.add("Some other test");

		List<LocalAccountantDelegate> result = (List<LocalAccountantDelegate>) q.execute(inputStrings);
		Assert.assertEquals("Number of results was wrong", 1, result.size());
		LocalAccountantDelegate delegate = result.iterator().next();
		assertDelegate1(delegate);
	}

	@Test
	public void queryCurrencyEquals() throws IOException
	{
		Query q = pm.newQuery(Account.class);
		q.setFilter("this.currency == :currency");

		List<Account> result = (List<Account>) q.execute(Currency.getInstance("GBP"));
		Assert.assertEquals("Number of results was wrong", 1, result.size());
		Account acct = result.iterator().next();
		Assert.assertEquals(ACCOUNT_ID_2, JDOHelper.getObjectId(acct));
	}

	@Test(expected=Exception.class) // TODO which exception exactly?
	public void queryWithDynamicQueryWithMissingParameter()
	{
		Query q = pm.newQuery(Account.class);
		q.setFilter("this.currency == :currency");
		q.execute(); // missing parameter should cause an exception
	}

	@Test(expected=Exception.class) // TODO which exception exactly?
	public void queryWithNamedQueryWithMissingParameter()
	{
		Query q = pm.newNamedQuery(Account.class, "getAccountForCurrency");
		q.execute(); // missing parameter should cause an exception
	}

	@After
	public void deleteAll() throws IOException
	{
		LocalAccountantDelegate localAccountantDelegate = (LocalAccountantDelegate) pm.getObjectById(LOCAL_ACCOUNTANT_DELEGATE_ID_0);
		pm.deletePersistent(localAccountantDelegate);

		Account account = (Account) pm.getObjectById(ACCOUNT_ID_0);
		pm.deletePersistent(account);

		account = (Account) pm.getObjectById(ACCOUNT_ID_1);
		pm.deletePersistent(account);

		localAccountantDelegate = (LocalAccountantDelegate) pm.getObjectById(LOCAL_ACCOUNTANT_DELEGATE_ID_1);
		pm.deletePersistent(localAccountantDelegate);

		account = (Account) pm.getObjectById(ACCOUNT_ID_2);
		pm.deletePersistent(account);
	}
}
