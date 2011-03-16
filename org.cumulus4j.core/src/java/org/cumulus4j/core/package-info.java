/**
 * <p>
 * Cumulus4j is a plug-in for DataNucleus providing encrypted data-storage.
 * </p>
 * <p>
 * Most classes in this package are extensions using DataNucleus' extension-points in order to implement
 * reading and writing of objects. DataNucleus uses the Eclipse extension mechanism. If you are not
 * familiar with Eclipse extensions, start reading here:
 * <a href="http://wiki.eclipse.org/FAQ_What_are_extensions_and_extension_points%3F">Eclipse FAQ
 * What are extensions and extension points?</a>
 * </p>
 * <p>
 * On one side, Cumulus4j is accessed by the frontend (i.e. the API consumer) via the following APIs:
 * </p>
 * <ul>
 * 	<li>Object-oriented persistence via JDO or JPA.</li>
 * 	<li>Key management API.</li>
 * </ul>
 * <p>
 * On the other side, Cumulus4j accesses the backend (i.e. another data storage) via a 2nd object-oriented
 * persistence layer instance - more precisely an instance of {@link javax.jdo.PersistenceManagerFactory}.
 * </p>
 */
package org.cumulus4j.core;