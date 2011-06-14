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
package org.cumulus4j.store.test.framework;

import java.io.File;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.Properties;
import java.util.Set;

import javax.jdo.JDOHelper;

import org.cumulus4j.store.model.ClassMeta;
import org.cumulus4j.store.model.DataEntry;
import org.cumulus4j.store.model.FieldMeta;
import org.cumulus4j.store.model.IndexEntryContainerSize;
import org.cumulus4j.store.model.Sequence;
import org.datanucleus.api.jdo.JDOPersistenceManagerFactory;
import org.datanucleus.plugin.ConfigurationElement;
import org.datanucleus.plugin.PluginManager;
import org.datanucleus.store.StoreManager;
import org.datanucleus.store.schema.SchemaAwareStoreManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Util class to clean up the database. Its (currently) only method is {@link #dropAllTables()} which
 * uses the 'datanucleus.properties' to obtain a JDBC connection and then to drop all tables in it, thus
 * ensuring that every test run finds exactly the same situation - an empty database.
 *
 * @author Marco หงุ่ยตระกูล-Schulze - marco at nightlabs dot de
 */
public class CleanupUtil
{
	private static final Logger logger = LoggerFactory.getLogger(CleanupUtil.class);

	public static void dropAllTables() throws Exception {
		Properties properties = TestUtil.loadProperties("cumulus4j-test-datanucleus.properties");

		logger.debug("Deleting all tables");
		String connectionURL = properties.getProperty("javax.jdo.option.ConnectionURL");
		if (connectionURL == null) {
			logger.warn("dropAllTables: Property 'javax.jdo.option.ConnectionURL' is not set! Skipping!");
			return;
		}
		else if (!connectionURL.startsWith("jdbc:")) {
			properties.remove("datanucleus.storeManagerType");
			JDOPersistenceManagerFactory pmf = (JDOPersistenceManagerFactory)JDOHelper.getPersistenceManagerFactory(properties);
			StoreManager storeMgr = pmf.getNucleusContext().getStoreManager();
			if (storeMgr instanceof SchemaAwareStoreManager) {
				// Use SchemaTool to delete the schema
				SchemaAwareStoreManager schemaMgr = (SchemaAwareStoreManager) storeMgr;
				Set<String> schemaClassNames = new HashSet<String>();
				schemaClassNames.add(ClassMeta.class.getName());
				schemaClassNames.add(FieldMeta.class.getName());
				schemaClassNames.add(DataEntry.class.getName());
				schemaClassNames.add(Sequence.class.getName());
				schemaClassNames.add(IndexEntryContainerSize.class.getName());

				PluginManager pluginMgr = pmf.getNucleusContext().getPluginManager();
				ConfigurationElement[] elems = pluginMgr.getConfigurationElementsForExtension(
						"org.cumulus4j.store.index_mapping", null, null);
				if (elems != null && elems.length > 0) {
					for (int i=0;i<elems.length;i++) {
						String indexTypeName = elems[i].getAttribute("index-entry-type");
						if (!schemaClassNames.contains(indexTypeName)) {
							schemaClassNames.add(indexTypeName);
						}
					}
				}

				logger.info("dropAllTables : running SchemaTool to delete Cumulus4J schema");
				schemaMgr.deleteSchema(schemaClassNames, null);
				logger.info("dropAllTables : SchemaTool deletion of Cumulus4J schema complete");
			}
			else {
				logger.info("dropAllTables: Not RDBMS nor Schema-Aware datastore so skipping");
			}
			return;
		}

		String connectionDriverName = properties.getProperty("javax.jdo.option.ConnectionDriverName");
		if (connectionDriverName == null) {
			logger.warn("dropAllTables: Property 'javax.jdo.option.ConnectionDriverName' is not set! Skipping!");
			return;
		}

		Class.forName(connectionDriverName);

		if ("org.apache.derby.jdbc.EmbeddedDriver".equals(connectionDriverName)) {
			// First shut down derby, in case it is open
			try {
				DriverManager.getConnection("jdbc:derby:;shutdown=true");
			} catch (SQLException x) {
				// ignore, because this is to be expected according to http://db.apache.org/derby/docs/dev/devguide/tdevdvlp40464.html
				doNothing(); // Remove warning from PMD report: http://cumulus4j.org/pmd.html
			}

			// simply delete the directory - the drop table commands failed and I don't have time to find out why
			if (!connectionURL.startsWith("jdbc:derby:"))
				throw new IllegalStateException("URL is not \"jdbc:derby:\"");

			String path = connectionURL.substring(11);
			int semicolonIdx = path.indexOf(';');
			if (semicolonIdx >= 0)
				path = path.substring(0, semicolonIdx);

			if (!deleteDirectoryRecursively(new File(path)))
				throw new IllegalStateException("Deleting Derby database \"" + path + "\" failed!");

			return;
		}

		java.sql.Connection con = DriverManager.getConnection(
				connectionURL,
				properties.getProperty("javax.jdo.option.ConnectionUserName"),
				properties.getProperty("javax.jdo.option.ConnectionPassword")
		);
		try {
			for (int i = 0; i < 10; ++i) {
				for (String tableName : getTables(con)) {
					Statement delStmt = con.createStatement();
					try {
						logger.debug("Deleting table " + tableName);
						delStmt.execute("drop table " + tableName);
					} catch (Throwable t) {
						logger.warn("Could not drop table " + tableName + ": " + t);
					} finally {
						delStmt.close();
					}
				}
			}

			Collection<String> tables = getTables(con);
			if (!tables.isEmpty()) {
				StringBuilder sb = new StringBuilder();
				for (String tableName : tables) {
					if (sb.length() > 0)
						sb.append(',');

					sb.append(tableName);
				}
				throw new IllegalStateException("Not all tables have been dropped! Still there: " + sb);
			}
		} finally {
			con.close();
		}
	}

	private static Collection<String> getTables(Connection con)
	throws SQLException
	{
		ArrayList<String> res = new ArrayList<String>();

		ResultSet rs = con.getMetaData().getTables(null, null, null, null);
		while (rs.next()) {
			String tableName = rs.getString("TABLE_NAME");
			if (!tableName.toLowerCase().startsWith("sys"))
				res.add(tableName);
		}

		return res;
	}

	private static boolean deleteDirectoryRecursively(File dir)
	{
		if (!dir.exists())
			return true;

		// If we're running this on linux (that's what I just tested ;) and dir denotes a symlink,
		// we must not dive into it and delete its contents! We can instead directly delete dir.
		// There is no way in Java (except for calling system tools) to find out whether it is a symlink,
		// but we can simply delete it. If the deletion succeeds, it was a symlink, otherwise it's a real directory.
		// This way, we don't delete the contents in symlinks and thus prevent data loss!
		try {
			if (dir.delete())
				return true;
		} catch(SecurityException e) {
			// ignore according to docs.
			if (logger.isDebugEnabled())
				logger.debug("deleteDirectoryRecursively: Could not delete directory \"" + dir.getAbsolutePath() + "\" (skipping deletion of its contents) due to SecurityException: " + e.getMessage(), e);

			return false; // or should we really ignore this security exception and delete the contents?!?!?! To return false instead is definitely safer.
		}

		if (dir.isDirectory()) {
			File[] content = dir.listFiles();
			for (File f : content) {
				if (f.isDirectory())
					deleteDirectoryRecursively(f);
				else
					try {
						f.delete();
					} catch(SecurityException e) {
						// ignore according to docs.
						if (logger.isDebugEnabled())
							logger.debug("deleteDirectoryRecursively: Could not delete file \"" + f.getAbsolutePath() + "\" due to SecurityException: " + e.getMessage(), e);
					}
			}
		}

		try {
			return dir.delete();
		} catch(SecurityException e) {
			return false;
		}
	}

	private static final void doNothing() { }
}
