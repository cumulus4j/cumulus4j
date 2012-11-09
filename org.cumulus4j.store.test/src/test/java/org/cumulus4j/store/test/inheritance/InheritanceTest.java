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
package org.cumulus4j.store.test.inheritance;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.jdo.FetchPlan;
import javax.jdo.Query;

import junit.framework.Assert;

import org.cumulus4j.store.test.framework.AbstractJDOTransactionalTestClearingDatabase;
import org.cumulus4j.store.test.inheritance.sources.Article;
import org.cumulus4j.store.test.inheritance.sources.Class_B;
import org.cumulus4j.store.test.inheritance.sources.Class_C;
import org.cumulus4j.store.test.inheritance.sources.Class_D;
import org.cumulus4j.store.test.inheritance.sources.F1;
import org.cumulus4j.store.test.inheritance.sources.F2;
import org.cumulus4j.store.test.inheritance.sources.Information;
import org.cumulus4j.store.test.inheritance.sources.InformationDBO;
import org.cumulus4j.store.test.inheritance.sources.Item;
import org.cumulus4j.store.test.inheritance.sources.PriceDBO;
import org.cumulus4j.store.test.inheritance.sources.ItemList;
import org.cumulus4j.store.test.inheritance.sources.Terms.Options;
import org.cumulus4j.store.test.inheritance.sources.TermsDBO;
import org.junit.Before;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class InheritanceTest
extends AbstractJDOTransactionalTestClearingDatabase
{
	private static final Logger logger = LoggerFactory.getLogger(InheritanceTest.class);

	@Before
	public void deleteAllInstances()
	{
		for (Iterator<?> it = pm.getExtent(InheritanceHierarchy0.class).iterator(); it.hasNext(); ) {
			Object o = it.next();
			pm.deletePersistent(o);
		}

		for (Iterator<?> it = pm.getExtent(Class_B.class).iterator(); it.hasNext(); ) {
			Object o = it.next();
			pm.deletePersistent(o);
		}
	}

	@Test
	public void persistAndQueryInheritanceTest() throws Exception {
		
		logger.info("start: persistAndQueryInheritanceTest");
		Class_B b = new Class_B();
		
		b.setQuery_id("test_id_abc");
		b.setDate(new Date());
		b.setText("test test 123");
		
		Article article = new Article();
		article.setArticle_id("AID-Ox1234567890FF");
		article.setVersion(1);
		article.setName("Plasmastrahlengenerator");
		article.setPricePreTax("Credits", 1000000);
		
		Item item = new Item();
		item.setAmount(1000);
		item.setArticle(article);
		
		List<Item> items = new ArrayList<Item>();
		items.add(item);
		
		ItemList offerItems = new ItemList();
		offerItems.setItems(items);
		
		b.setOfferItems(offerItems);
		
		TermsDBO terms = new TermsDBO();
		terms.setOption(Options.PAY_WITHIN_DAYS_OFFER_DISCOUNT);
		
		b.setTerms(terms);
		
		Information information = new Information();
		information.setAdditionalInformation("Bender is a robot that likes to drink alcoholic beverages");
		
		Class_D	sender = new Class_D();
		sender.setClass_d_id("Bender");
		sender.setScore(9001);
		sender.setDate(new Date());
		sender.setVersion(1);
		sender.setInformation(information);
		
		b.setSender(sender);
		
		PriceDBO pricePreTax = new PriceDBO();
		pricePreTax.setCurrency("Credits");
		pricePreTax.setPrice(new BigDecimal(1000000));
		
		b.setPricePreTax(pricePreTax);
		
		PriceDBO priceAfterTax = new PriceDBO();
		priceAfterTax.setCurrency("Credits");
		priceAfterTax.setPrice(new BigDecimal(1190000));

		b.setPriceAfterTax(priceAfterTax);
		
		InformationDBO infos = new InformationDBO();
		infos.setAdditionalInformation("Fry loves Leela");
		
		Class_C acceptor = new Class_C();
		acceptor.setClass_c_id("Fry");
		acceptor.setScore(0);
		acceptor.setDate(new Date());
		acceptor.setVersion(1);
		acceptor.setInformation(infos);
		
		b.setAcceptor(acceptor);
		
		pm.makePersistent(b);
		
		commitAndBeginNewTransaction();
		
		List<Class_B> result = (List<Class_B>) pm.newQuery("select from " + Class_B.class.getName() + " where query_id == 'test_id_abc'").execute();
		
		logger.info("The query_id of the query result Class_B object: " + result.get(0).getQuery_id());
		logger.info("The sender id: " + result.get(0).getSender().getClass_d_id() + ". - Should be 'Bender'");
		logger.info("The sender's additional info: " + result.get(0).getSender().getInformation().getAdditionalInformation() + ". - Should tell you about his favorite drinks.");
		logger.info("The acceptor's id: " + result.get(0).getAcceptor().getClass_c_id() + ". - Should be 'Fry'");
		logger.info("The acceptor's additional info: " + result.get(0).getAcceptor().getInformation().getAdditionalInformation() + ". - Fry and Leela sitting in a tree. K-I-S-S-I-N-G.");
		logger.info("The article's price as stored in the article: " + result.get(0).getItems().getItems().get(0).getArticle().getPricePreTax().getPrice().toString() + ". - Should be a Million.");
		logger.info("The total price after tax as stored in the price after tax field: " + result.get(0).getPriceAfterTax().getPrice().toString() + ". - Should be a 1190000.");

		logger.info("end: persistenAndQueryInheritanceTest");
		
	}
	
	@Test
	public void persistAnObjectAndQueryWithAnObjectOfAnotherSubclass() throws Exception
	{
		logger.info("start: persistAnObjectAndQueryWithAnObjectOfAnotherSubclass");

		F1 f1 = new F1();
		f1.setF1Text("class F1 object");
		f1.setQuery_id("Test_ID");
		pm.makePersistent(f1);
		
		commitAndBeginNewTransaction();

		List<Object> result = (List<Object>) pm.newQuery("select from " + F2.class.getName() + " where query_id == 'Test_ID'").execute();
		
		logger.info("the following objects were found:");
		for(int i = 0; i < result.size(); i++) {
			logger.info(result.get(i).toString());
		}
		
		Assert.assertEquals(0, result.size());

		//if (result.size() > 0) {
        //    throw new IllegalArgumentException();
        //}
		
		logger.info("end: persistAnObjectAndQueryWithAnObjectOfAnotherSubclass");
	}
	
	@Test
	public void persistOneInheritanceObject()
	throws Exception
	{
		logger.info("createOneInheritanceObject: entered");
		pm.getExtent(InheritanceHierarchy4.class);

		InheritanceHierarchy4 inheritanceObject = new InheritanceHierarchy4();
		inheritanceObject = pm.makePersistent(inheritanceObject);
	}

	private Map<Class<? extends InheritanceHierarchy0>, List<InheritanceHierarchy0>> persistSomeInstances()
	{
		Map<Class<? extends InheritanceHierarchy0>, List<InheritanceHierarchy0>> result = new HashMap<Class<? extends InheritanceHierarchy0>, List<InheritanceHierarchy0>>();
		List<InheritanceHierarchy0> list;

		list = new ArrayList<InheritanceHierarchy0>(); result.put(InheritanceHierarchy0.class, list);
		list.add(pm.makePersistent(new InheritanceHierarchy0()));
		list.add(pm.makePersistent(new InheritanceHierarchy0()));

		list = new ArrayList<InheritanceHierarchy0>(); result.put(InheritanceHierarchy1.class, list);
		list.add(pm.makePersistent(new InheritanceHierarchy1()));
		list.add(pm.makePersistent(new InheritanceHierarchy1()));
		list.add(pm.makePersistent(new InheritanceHierarchy1()));

		list = new ArrayList<InheritanceHierarchy0>(); result.put(InheritanceHierarchy2.class, list);
		list.add(pm.makePersistent(new InheritanceHierarchy2()));
		list.add(pm.makePersistent(new InheritanceHierarchy2()));
		list.add(pm.makePersistent(new InheritanceHierarchy2()));
		list.add(pm.makePersistent(new InheritanceHierarchy2()));

		list = new ArrayList<InheritanceHierarchy0>(); result.put(InheritanceHierarchy3.class, list);
		list.add(pm.makePersistent(new InheritanceHierarchy3()));
		list.add(pm.makePersistent(new InheritanceHierarchy3()));
		list.add(pm.makePersistent(new InheritanceHierarchy3()));
		list.add(pm.makePersistent(new InheritanceHierarchy3()));
		list.add(pm.makePersistent(new InheritanceHierarchy3()));
		list.add(pm.makePersistent(new InheritanceHierarchy3()));
		list.add(pm.makePersistent(new InheritanceHierarchy3()));
		list.add(pm.makePersistent(new InheritanceHierarchy3()));

		list = new ArrayList<InheritanceHierarchy0>(); result.put(InheritanceHierarchy4.class, list);
		list.add(pm.makePersistent(new InheritanceHierarchy4()));
		list.add(pm.makePersistent(new InheritanceHierarchy4()));
		list.add(pm.makePersistent(new InheritanceHierarchy4()));
		list.add(pm.makePersistent(new InheritanceHierarchy4()));
		list.add(pm.makePersistent(new InheritanceHierarchy4()));
		list.add(pm.makePersistent(new InheritanceHierarchy4()));

		return result;
	}

	@Test
	public void persistAndQueryIncludingSubclasses()
	throws Exception
	{
		persistSomeInstances();

		commitAndBeginNewTransaction();

		{
			Query query = pm.newQuery(InheritanceHierarchy0.class);
			Collection<?> result = (Collection<?>) query.execute();
			Assert.assertEquals(23, result.size());
		}

		{
			Query query = pm.newQuery(InheritanceHierarchy1.class);
			Collection<?> result = (Collection<?>) query.execute();
			Assert.assertEquals(21, result.size());
		}

		{
			Query query = pm.newQuery(InheritanceHierarchy2.class);
			Collection<?> result = (Collection<?>) query.execute();
			Assert.assertEquals(18, result.size());
		}

		{
			Query query = pm.newQuery(InheritanceHierarchy3.class);
			Collection<?> result = (Collection<?>) query.execute();
			Assert.assertEquals(14, result.size());
		}

		{
			Query query = pm.newQuery(InheritanceHierarchy4.class);
			Collection<?> result = (Collection<?>) query.execute();
			Assert.assertEquals(6, result.size());
		}
	}

	@Test
	public void persistAndQueryExcludingSubclassesUsingExtent()
	throws Exception
	{
		persistSomeInstances();

		commitAndBeginNewTransaction();

		{
			Query query = pm.newQuery(pm.getExtent(InheritanceHierarchy0.class, false));
			Collection<?> result = (Collection<?>) query.execute();
			Assert.assertEquals(2, result.size());
		}

		{
			Query query = pm.newQuery(pm.getExtent(InheritanceHierarchy1.class, false));
			Collection<?> result = (Collection<?>) query.execute();
			Assert.assertEquals(3, result.size());
		}

		{
			Query query = pm.newQuery(pm.getExtent(InheritanceHierarchy2.class, false));
			Collection<?> result = (Collection<?>) query.execute();
			Assert.assertEquals(4, result.size());
		}

		{
			Query query = pm.newQuery(pm.getExtent(InheritanceHierarchy3.class, false));
			Collection<?> result = (Collection<?>) query.execute();
			Assert.assertEquals(8, result.size());
		}

		{
			Query query = pm.newQuery(pm.getExtent(InheritanceHierarchy4.class, false));
			Collection<?> result = (Collection<?>) query.execute();
			Assert.assertEquals(6, result.size());
		}
	}

	@Test
	public void persistAndQueryExcludingSubclassesUsingJDOQL()
	throws Exception
	{
		persistSomeInstances();

		commitAndBeginNewTransaction();

		{
			Query query = pm.newQuery("SELECT FROM " + InheritanceHierarchy0.class.getName() + " EXCLUDE SUBCLASSES");
			Collection<?> result = (Collection<?>) query.execute();
			Assert.assertEquals(2, result.size());
		}

		{
			Query query = pm.newQuery("SELECT FROM " + InheritanceHierarchy1.class.getName() + " EXCLUDE SUBCLASSES");
			Collection<?> result = (Collection<?>) query.execute();
			Assert.assertEquals(3, result.size());
		}

		{
			Query query = pm.newQuery("SELECT FROM " + InheritanceHierarchy2.class.getName() + " EXCLUDE SUBCLASSES");
			Collection<?> result = (Collection<?>) query.execute();
			Assert.assertEquals(4, result.size());
		}

		{
			Query query = pm.newQuery("SELECT FROM " + InheritanceHierarchy3.class.getName() + " EXCLUDE SUBCLASSES");
			Collection<?> result = (Collection<?>) query.execute();
			Assert.assertEquals(8, result.size());
		}

		{
			Query query = pm.newQuery("SELECT FROM " + InheritanceHierarchy4.class.getName() + " EXCLUDE SUBCLASSES");
			Collection<?> result = (Collection<?>) query.execute();
			Assert.assertEquals(6, result.size());
		}
	}

	@Test
	public void persistAndIterateExtent()
	throws Exception
	{
		persistSomeInstances();

		commitAndBeginNewTransaction();

		Map<Class<?>, Integer[]> class2count = new HashMap<Class<?>, Integer[]>();
		for (Iterator<?> it = pm.getExtent(InheritanceHierarchy0.class).iterator(); it.hasNext(); ) {
			Object o = it.next();
			Assert.assertNotNull(o);
			Integer[] count = class2count.get(o.getClass());
			if (count == null) {
				count = new Integer[] { 0 };
				class2count.put(o.getClass(), count);
			}
			++count[0];
		}

		Assert.assertNotNull(class2count.get(InheritanceHierarchy0.class));
		Assert.assertNotNull(class2count.get(InheritanceHierarchy1.class));
		Assert.assertNotNull(class2count.get(InheritanceHierarchy2.class));
		Assert.assertNotNull(class2count.get(InheritanceHierarchy3.class));
		Assert.assertNotNull(class2count.get(InheritanceHierarchy4.class));

		Assert.assertEquals(Integer.valueOf(2), class2count.get(InheritanceHierarchy0.class)[0]);
		Assert.assertEquals(Integer.valueOf(3), class2count.get(InheritanceHierarchy1.class)[0]);
		Assert.assertEquals(Integer.valueOf(4), class2count.get(InheritanceHierarchy2.class)[0]);
		Assert.assertEquals(Integer.valueOf(8), class2count.get(InheritanceHierarchy3.class)[0]);
		Assert.assertEquals(Integer.valueOf(6), class2count.get(InheritanceHierarchy4.class)[0]);
	}

	@Test
	public void persistAndQuery()
	throws Exception
	{
		Map<Class<? extends InheritanceHierarchy0>, List<InheritanceHierarchy0>> map = persistSomeInstances();

		pm.getFetchPlan().setMaxFetchDepth(-1);
		pm.getFetchPlan().setGroup(FetchPlan.ALL);

		List<InheritanceHierarchy0> list2 = map.get(InheritanceHierarchy2.class);
		Collection<InheritanceHierarchy0> c2 = pm.detachCopyAll(list2);

		List<InheritanceHierarchy0> list3 = map.get(InheritanceHierarchy3.class);
		Collection<InheritanceHierarchy0> c3 = pm.detachCopyAll(list3);

		commitAndBeginNewTransaction();

		InheritanceHierarchy2 obj2 = (InheritanceHierarchy2) c2.iterator().next();
		InheritanceHierarchy3 obj3 = (InheritanceHierarchy3) c3.iterator().next();

		Query query = pm.newQuery(InheritanceHierarchy2.class);
		query.setFilter("this.i2 == :arg");

		Collection<?> queryResult = (Collection<?>) query.execute(obj2.getI2());
		Assert.assertTrue(queryResult.contains(obj2));
		for (Object object : queryResult) {
			InheritanceHierarchy2 o = (InheritanceHierarchy2) object;
			Assert.assertEquals(obj2.getI2(), o.getI2());
		}

		queryResult = (Collection<?>) query.execute(obj3.getI2());
		Assert.assertTrue(queryResult.contains(obj3));
		for (Object object : queryResult) {
			InheritanceHierarchy2 o = (InheritanceHierarchy2) object;
			Assert.assertEquals(obj3.getI2(), o.getI2());
		}
	}
}
