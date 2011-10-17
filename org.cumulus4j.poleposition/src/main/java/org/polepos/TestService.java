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
package org.polepos;

import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Path("Test")
@Consumes({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
@Produces({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
public class TestService
{

	private static Logger logger = LoggerFactory.getLogger(TestService.class);

//	private static PersistenceManagerFactory pmf;

//	protected static synchronized PersistenceManagerFactory getPersistenceManagerFactory()
//	{
//		if (pmf == null) {
//			try {
//				CleanupUtil.dropAllTables();
//			} catch (Exception e) {
//				throw new RuntimeException(e);
//			}
//			pmf = JDOHelper.getPersistenceManagerFactory(TestUtil.loadProperties("cumulus4j-test-datanucleus.properties"));
//		}
//
//		return pmf;
//	}
//
//	protected PersistenceManager getPersistenceManager(String cryptoManagerID, String cryptoSessionID)
//	{
//		if (cryptoManagerID == null)
//			throw new IllegalArgumentException("cryptoManagerID == null");
//
//		if (cryptoSessionID == null)
//			throw new IllegalArgumentException("cryptoSessionID == null");
//
//		PersistenceManager pm = getPersistenceManagerFactory().getPersistenceManager();
//		pm.setProperty(CryptoManager.PROPERTY_CRYPTO_MANAGER_ID, cryptoManagerID);
//		pm.setProperty(CryptoSession.PROPERTY_CRYPTO_SESSION_ID, cryptoSessionID);
//		return pm;
//	}

	@POST
	@Produces(MediaType.TEXT_PLAIN)
	public String testPost(
			@QueryParam("cryptoManagerID") String cryptoManagerID,
			@QueryParam("cryptoSessionID") String cryptoSessionID
	)
	{

		for(int i = 0; i < 20; i++)
			logger.debug("#########################################");
		logger.debug("invoked testPost");
		for(int i = 0; i < 20; i++)
			logger.debug("#########################################");

		RunSeason.main(null);

		return "";
//		// We enforce a fresh start every time, because we execute this now with different key-servers / embedded key-stores:
//		if (pmf != null) {
//			pmf.close();
//			pmf = null;
//		}
//
//		if (cryptoManagerID == null || cryptoManagerID.isEmpty())
//			cryptoManagerID = "keyManager";
//
//		StringBuilder resultSB = new StringBuilder();
//		PersistenceManager pm = getPersistenceManager(cryptoManagerID, cryptoSessionID);
//		try {
//			// tx1: persist some data
//			pm.currentTransaction().begin();
//
//			pm.getExtent(Movie.class);
//			{
//				Movie movie = new Movie();
//				movie.setName("MMM " + System.currentTimeMillis());
//				movie = pm.makePersistent(movie);
//
//				Rating rating = new Rating();
//				rating.setName("RRR " + System.currentTimeMillis());
//				rating = pm.makePersistent(rating);
//
//				movie.setRating(rating);
//			}
//
//			{
//				Movie movie = new Movie();
//				movie.setName("MMM " + System.currentTimeMillis());
//				movie = pm.makePersistent(movie);
//
//				Person person = new Person();
//				person.setName("PPP " + System.currentTimeMillis());
//				person = pm.makePersistent(person);
//
//				movie.getStarring().add(person);
//				pm.currentTransaction().commit();
//			}
//
//			pm = getPersistenceManager(cryptoManagerID, cryptoSessionID);
//			// TODO I just had this exception. Obviously the PM is closed when its tx is committed - this is IMHO wrong and a DN bug.
//			// I have to tell Andy.
//			// Marco :-)
////				javax.jdo.JDOFatalUserException: Persistence Manager has been closed
////					at org.datanucleus.api.jdo.JDOPersistenceManager.assertIsOpen(JDOPersistenceManager.java:2189)
////					at org.datanucleus.api.jdo.JDOPersistenceManager.newQuery(JDOPersistenceManager.java:1286)
////					at org.datanucleus.api.jdo.JDOPersistenceManager.newQuery(JDOPersistenceManager.java:1237)
////					at org.datanucleus.api.jdo.JDOPersistenceManager.newQuery(JDOPersistenceManager.java:1349)
////					at org.cumulus4j.store.query.QueryHelper.getAllPersistentObjectsForCandidateClasses(QueryHelper.java:60)
////					at org.cumulus4j.store.query.QueryEvaluator.execute(QueryEvaluator.java:272)
////					at org.cumulus4j.store.query.JDOQLQuery.performExecute(JDOQLQuery.java:83)
////					at org.datanucleus.store.query.Query.executeQuery(Query.java:1744)
////					at org.datanucleus.store.query.Query.executeWithArray(Query.java:1634)
////					at org.datanucleus.store.query.Query.execute(Query.java:1607)
////					at org.datanucleus.store.DefaultCandidateExtent.iterator(DefaultCandidateExtent.java:62)
////					at org.datanucleus.api.jdo.JDOExtent.iterator(JDOExtent.java:120)
////					at org.cumulus4j.integrationtest.webapp.TestService.testPost(TestService.java:86)
////					at org.cumulus4j.integrationtest.webapp.TestService.testGet(TestService.java:106)
////					at sun.reflect.NativeMethodAccessorImpl.invoke0(Native Method)
////					at sun.reflect.NativeMethodAccessorImpl.invoke(NativeMethodAccessorImpl.java:39)
////					at sun.reflect.DelegatingMethodAccessorImpl.invoke(DelegatingMethodAccessorImpl.java:25)
////					at java.lang.reflect.Method.invoke(Method.java:597)
////					at com.sun.jersey.server.impl.model.method.dispatch.AbstractResourceMethodDispatchProvider$TypeOutInvoker._dispatch(AbstractResourceMethodDispatchProvider.java:168)
////					at com.sun.jersey.server.impl.model.method.dispatch.ResourceJavaMethodDispatcher.dispatch(ResourceJavaMethodDispatcher.java:71)
////					at com.sun.jersey.server.impl.uri.rules.HttpMethodRule.accept(HttpMethodRule.java:280)
////					at com.sun.jersey.server.impl.uri.rules.RightHandPathRule.accept(RightHandPathRule.java:147)
////					at com.sun.jersey.server.impl.uri.rules.ResourceClassRule.accept(ResourceClassRule.java:108)
////					at com.sun.jersey.server.impl.uri.rules.RightHandPathRule.accept(RightHandPathRule.java:147)
////					at com.sun.jersey.server.impl.uri.rules.RootResourceClassesRule.accept(RootResourceClassesRule.java:84)
////					at com.sun.jersey.server.impl.application.WebApplicationImpl._handleRequest(WebApplicationImpl.java:1341)
////					at com.sun.jersey.server.impl.application.WebApplicationImpl._handleRequest(WebApplicationImpl.java:1273)
////					at com.sun.jersey.server.impl.application.WebApplicationImpl.handleRequest(WebApplicationImpl.java:1223)
////					at com.sun.jersey.server.impl.application.WebApplicationImpl.handleRequest(WebApplicationImpl.java:1213)
////					at com.sun.jersey.spi.container.servlet.WebComponent.service(WebComponent.java:414)
////					at com.sun.jersey.spi.container.servlet.ServletContainer.service(ServletContainer.java:537)
////					at com.sun.jersey.spi.container.servlet.ServletContainer.service(ServletContainer.java:699)
////					at javax.servlet.http.HttpServlet.service(HttpServlet.java:847)
////					at org.eclipse.jetty.servlet.ServletHolder.handle(ServletHolder.java:546)
////					at org.eclipse.jetty.servlet.ServletHandler.doHandle(ServletHandler.java:483)
////					at org.eclipse.jetty.server.handler.ScopedHandler.handle(ScopedHandler.java:119)
////					at org.eclipse.jetty.security.SecurityHandler.handle(SecurityHandler.java:516)
////					at org.eclipse.jetty.server.session.SessionHandler.doHandle(SessionHandler.java:230)
////					at org.eclipse.jetty.server.handler.ContextHandler.doHandle(ContextHandler.java:956)
////					at org.eclipse.jetty.servlet.ServletHandler.doScope(ServletHandler.java:411)
////					at org.eclipse.jetty.server.session.SessionHandler.doScope(SessionHandler.java:188)
////					at org.eclipse.jetty.server.handler.ContextHandler.doScope(ContextHandler.java:891)
////					at org.eclipse.jetty.server.handler.ScopedHandler.handle(ScopedHandler.java:117)
////					at org.eclipse.jetty.server.handler.ContextHandlerCollection.handle(ContextHandlerCollection.java:247)
////					at org.eclipse.jetty.server.handler.HandlerCollection.handle(HandlerCollection.java:151)
////					at org.eclipse.jetty.server.handler.HandlerWrapper.handle(HandlerWrapper.java:114)
////					at org.eclipse.jetty.server.Server.handle(Server.java:353)
////					at org.eclipse.jetty.server.HttpConnection.handleRequest(HttpConnection.java:598)
////					at org.eclipse.jetty.server.HttpConnection$RequestHandler.headerComplete(HttpConnection.java:1059)
////					at org.eclipse.jetty.http.HttpParser.parseNext(HttpParser.java:590)
////					at org.eclipse.jetty.http.HttpParser.parseAvailable(HttpParser.java:212)
////					at org.eclipse.jetty.server.HttpConnection.handle(HttpConnection.java:427)
////					at org.eclipse.jetty.io.nio.SelectChannelEndPoint.handle(SelectChannelEndPoint.java:510)
////					at org.eclipse.jetty.io.nio.SelectChannelEndPoint.access$000(SelectChannelEndPoint.java:34)
////					at org.eclipse.jetty.io.nio.SelectChannelEndPoint$1.run(SelectChannelEndPoint.java:40)
////					at org.eclipse.jetty.util.thread.QueuedThreadPool$2.run(QueuedThreadPool.java:450)
////					at java.lang.Thread.run(Thread.java:662)
//
//
//			// tx2: read some data
//			pm.currentTransaction().begin();
//
//			for (Iterator<Movie> it = pm.getExtent(Movie.class).iterator(); it.hasNext(); ) {
//				Movie movie = it.next();
//				resultSB.append(" * ").append(movie.getName()).append('\n');
//			}
//
//			pm.currentTransaction().commit();
//			return "OK: " + this.getClass().getName() + "\n\nSome movies:\n" + resultSB;
//		} finally {
//			if (pm.currentTransaction().isActive())
//				pm.currentTransaction().rollback();
//
//			pm.close();
//		}
	}

	@GET
	@Produces(MediaType.TEXT_PLAIN)
	public String testGet()
	{
		return "OK: " + this.getClass().getName() + ": Use POST on the same URL for a real test.";
	}

}
