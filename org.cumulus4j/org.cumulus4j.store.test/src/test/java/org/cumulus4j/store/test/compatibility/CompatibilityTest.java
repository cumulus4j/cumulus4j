package org.cumulus4j.store.test.compatibility;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Modifier;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Properties;
import java.util.Set;
import java.util.UUID;

import javax.jdo.JDOHelper;
import javax.jdo.PersistenceManager;
import javax.jdo.PersistenceManagerFactory;

import org.cumulus4j.store.DummyCryptoManager;
import org.cumulus4j.store.crypto.CryptoManager;
import org.cumulus4j.store.crypto.CryptoSession;
import org.cumulus4j.store.datastoreversion.command.MinimumCumulus4jVersion;
import org.cumulus4j.store.test.framework.CleanupUtil;
import org.cumulus4j.store.test.framework.TestUtil;
import org.cumulus4j.testutil.IOUtil;
import org.cumulus4j.testutil.ReflectUtil;
import org.cumulus4j.testutil.Util;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class CompatibilityTest {
	private static final Logger logger = LoggerFactory.getLogger(CompatibilityTest.class);

	private static final String DATA_NUCLEUS_PROPERTIES_RESOURCE_NAME = "cumulus4j-test-datanucleus.properties";

	static {
		TestUtil.configureLoggingOnce();
	}

	private Collection<Class<? extends CompatibilityTestData>> testDataClasses;

	private Collection<Class<? extends CompatibilityTestData>> getTestDataClasses() throws Exception {
		if (testDataClasses == null) {
			Collection<Class<?>> classes = ReflectUtil.listClassesInPackage(CompatibilityTest.class.getPackage().getName(), true);
			List<Class<? extends CompatibilityTestData>> result = new ArrayList<Class<? extends CompatibilityTestData>>(classes.size());
			for (Class<?> c : classes) {
				if (CompatibilityTestData.class.isAssignableFrom(c) &&  0 == (c.getModifiers() & Modifier.ABSTRACT)) {
					@SuppressWarnings("unchecked")
					Class<? extends CompatibilityTestData> testDataClass = (Class<? extends CompatibilityTestData>) c;
					result.add(testDataClass);
				}
			}
			testDataClasses = result;
		}
		return testDataClasses;
	}

	private Properties getDataNucleusProperties() {
		Properties properties = new Properties();
		InputStream in = CompatibilityTest.class.getClassLoader().getResourceAsStream(DATA_NUCLEUS_PROPERTIES_RESOURCE_NAME);
		if (in == null)
			throw new IllegalStateException("Resource not found: " + DATA_NUCLEUS_PROPERTIES_RESOURCE_NAME);

		try {
			properties.load(in);
			in.close();
		} catch (IOException e) { // reading a resource - should never happen
			throw new RuntimeException(e);
		}

		// We expect a Derby DB. This should never be changed in the resources (might be overridden by
		// dev-specific settings, but we don't read the file in the home directory for this test).
		String connectionDriverNameKey = "javax.jdo.option.ConnectionDriverName";
		String connectionDriverNameValue = properties.getProperty(connectionDriverNameKey);
		if (!"org.apache.derby.jdbc.EmbeddedDriver".equals(connectionDriverNameValue))
			throw new IllegalStateException(String.format("Resource %s contains invalid value for property %s: %s",
					DATA_NUCLEUS_PROPERTIES_RESOURCE_NAME, connectionDriverNameKey, connectionDriverNameValue));

		return properties;
	}

	private Properties prepareOldDatastoreAndGetDataNucleusProperties(String version) throws Exception {
		File tmpBaseDir = IOUtil.createUniqueRandomFolder(IOUtil.getTempDir(), "cumulus4j_");
		String resourceName = String.format("derby-cumulus4j-%s.zip", version);
		File tmpZip = new File(tmpBaseDir, resourceName);
		IOUtil.copyResource(CompatibilityTest.class, resourceName, tmpZip);
		IOUtil.unzipArchive(tmpZip, tmpBaseDir);
		tmpZip.delete();

		File tmpDatabaseDir = getDerbyCumulus4jDatabaseDir(tmpBaseDir, version);
		String connectionURL = String.format("jdbc:derby:%s", tmpDatabaseDir.getAbsolutePath());
		Properties properties = getDataNucleusProperties();
		properties.setProperty("javax.jdo.option.ConnectionURL", connectionURL);
		return properties;
	}

	private File getDerbyCumulus4jDatabaseDir(File baseDir, String version) {
		return new File(baseDir, String.format("derby-cumulus4j-%s", version));
	}

	private void populateDatastore(PersistenceManager pm) throws Exception {
		for (Class<?> testDataClass : getTestDataClasses()) {
				CompatibilityTestData compatibilityTestData = (CompatibilityTestData) testDataClass.newInstance();
				compatibilityTestData.setPersistenceManager(pm);
				compatibilityTestData.create();
				pm.flush();
		}
	}

	private void verifyDatastore(PersistenceManager pm, String version) throws Exception {
		for (Class<?> testDataClass : getTestDataClasses()) {
				CompatibilityTestData compatibilityTestData = (CompatibilityTestData) testDataClass.newInstance();
				// Currently, we simply compare the strings. We might later parse the versions to ensure
				// e.g. "1.0.12" is understood correctly as being newer than "1.0.9".
				if (version.compareTo(compatibilityTestData.getSinceVersion()) < 0) {
					// Skip CompatibilityTestData which did not yet exist in the version we're currently verifying.
					continue;
				}
				compatibilityTestData.setPersistenceManager(pm);
				compatibilityTestData.verify();
		}
	}

	private PersistenceManager createPersistenceManager(PersistenceManagerFactory pmf) {
		PersistenceManager pm = pmf.getPersistenceManager();
		pm.setProperty(CryptoManager.PROPERTY_CRYPTO_MANAGER_ID, "dummy");
		String keyStoreID = DummyCryptoManager.KEY_STORE_ID_COMPATIBILITY_TEST;
		pm.setProperty(CryptoSession.PROPERTY_CRYPTO_SESSION_ID, keyStoreID + '_' + UUID.randomUUID() + '*' + UUID.randomUUID());
		return pm;
	}

	private String getCurrentVersion() {
//		return CompatibilityTestData.VERSION_1_0_0;
		MinimumCumulus4jVersion minimumCumulus4jVersion = new MinimumCumulus4jVersion();
//		int commandVersion = minimumCumulus4jVersion.getCommandVersion();
//		int major = commandVersion / 100 /*minor*/ / 100 /*release*/ / 1000 /*serial*/;
//		int minor = commandVersion                 / 100 /*release*/ / 1000 /*serial*/;
//		int release = commandVersion                                 / 1000 /*serial*/;
//
//		release -= minor * 100; // minor still includes major => only subtract minor.
//		minor -= major * 100;
//
//		return String.format("%s.%s.%s", major, minor, release);
		return String.format(
				"%s.%s.%s",
				minimumCumulus4jVersion.getCommandVersionMajor(),
				minimumCumulus4jVersion.getCommandVersionMinor(),
				minimumCumulus4jVersion.getCommandVersionRelease()
		);
	}

	@Test
	public void createDatastoreCurrentVersion() throws Exception {
		// The current version of the datastore (might be lower than the current C4j version, if the datastore
		// format did not change).
		final String version = getCurrentVersion();

		File tmpBaseDir = IOUtil.createUniqueRandomFolder(IOUtil.getTempDir(), "cumulus4j_");
		try {
			File tmpDatabaseDir = getDerbyCumulus4jDatabaseDir(tmpBaseDir, version);
			String connectionURL = String.format("jdbc:derby:%s;create=true", tmpDatabaseDir.getAbsolutePath());
			Properties properties = getDataNucleusProperties();
			properties.setProperty("javax.jdo.option.ConnectionURL", connectionURL);

			PersistenceManagerFactory pmf = JDOHelper.getPersistenceManagerFactory(properties);
			try {
				PersistenceManager pm = createPersistenceManager(pmf);
				try {
					pm.currentTransaction().begin();
					populateDatastore(pm);
					pm.currentTransaction().commit();
				} finally {
					if (pm.currentTransaction().isActive())
						pm.currentTransaction().rollback();

					pm.close();
				}

				// Immediately verify, so that we can be sure, it works *now* and if it breaks later,
				// we at least now, that it definitely did work before.
				pm = createPersistenceManager(pmf);
				try {
					pm.currentTransaction().begin();
					verifyDatastore(pm, version);
					pm.currentTransaction().commit();
				} finally {
					if (pm.currentTransaction().isActive())
						pm.currentTransaction().rollback();

					pm.close();
				}
			} finally {
				pmf.close();
				TestUtil.shutdownDerby();
			}

			File zipOutputFile = new File(
					IOUtil.getTempDir(),
					String.format("derby-cumulus4j-%s-%s.zip", version, Long.toString(System.currentTimeMillis(), 36))
			);
			IOUtil.zipFolder(zipOutputFile, tmpBaseDir);

		} finally {
			IOUtil.deleteDirectoryRecursively(tmpBaseDir);
		}
	}

	@Test
	public void tryToReadDatastore_1_0_0() throws Exception {
		tryToReadDatastore(CompatibilityTestData.VERSION_1_0_0, new PrepareRunnable() {
			@Override
			public void run() throws Exception {
				// This might be required, if switching the identity type of the column is not
				// supported by the underlying database. Derby does not allow it, some might allow
				// and for some others the new strategy and the legacy strategy might have the same
				// result anyway. You should try it out for your database.
				properties.put("cumulus4j.datanucleus.rdbms.useLegacyNativeValueStrategy", "true");

				// Since DN does not change existing [unique] indices, we have to drop all of them.
				// DN will automatically re-create them. Just changing does not work :-(
				dropAllIndices(properties);
			}
		});
	}

	@Test
	public void tryToReadDatastore_1_1_0() throws Exception {
		tryToReadDatastore(CompatibilityTestData.VERSION_1_1_0, null);
	}

	/**
	 * Preparations that a user/admin must do manually when upgrading from a certain version.
	 */
	private static abstract class PrepareRunnable {
		protected Properties properties;
		public abstract void run() throws Exception;
	}

	private void tryToReadDatastore(String version, PrepareRunnable prepareRunnable) throws Exception {
		Properties properties = prepareOldDatastoreAndGetDataNucleusProperties(version);

		if (prepareRunnable != null) {
			prepareRunnable.properties = properties;
			prepareRunnable.run();
			properties = prepareRunnable.properties;
		}

		PersistenceManagerFactory pmf = JDOHelper.getPersistenceManagerFactory(properties);
		try {
			PersistenceManager pm = createPersistenceManager(pmf);
			try {
				pm.currentTransaction().begin();
				verifyDatastore(pm, version);
				pm.currentTransaction().commit();
			} finally {
				if (pm.currentTransaction().isActive())
					pm.currentTransaction().rollback();

				pm.close();
			}
		} finally {
			pmf.close();
		}
		// Drop only in case of success. Otherwise we want to take a look into it.
		CleanupUtil.dropAllTables(properties);
	}

	private void dropAllIndices(Properties properties) throws Exception {
		String connectionDriverName = properties.getProperty("javax.jdo.option.ConnectionDriverName");
		if (connectionDriverName == null) {
			connectionDriverName = properties.getProperty("datanucleus.ConnectionDriverName");
		}
		if (connectionDriverName == null) {
			throw new IllegalStateException("'ConnectionDriverName' property is not set!");
		}
		Class<?> driverClass = Class.forName(connectionDriverName);
		driverClass.newInstance();

		String connectionURL = properties.getProperty("javax.jdo.option.ConnectionURL");
		String userName = properties.getProperty("javax.jdo.option.ConnectionUserName");
		String password = properties.getProperty("javax.jdo.option.ConnectionPassword");

		Connection connection = DriverManager.getConnection(connectionURL, userName, password);
		try {
			Statement statement = connection.createStatement();
			for (Index index : getForeignKeys(connection)) {
				logger.info("dropAllIndices: Dropping foreign key: tableName='{}' indexName='{}'", index.tableName, index.indexName);
				try {
					statement.executeUpdate(String.format("ALTER TABLE \"%s\" DROP FOREIGN KEY \"%s\"", index.tableName, index.indexName));
				} catch (Exception x) {
					logger.warn(x.toString(), x);
				}
			}

			for (Index index : getIndexes(connection)) {
//				logger.info("dropAllIndices: Dropping unique index: tableName='{}' indexName='{}'", index.tableName, index.indexName);
//				try {
//					statement.executeUpdate(String.format("ALTER TABLE \"%s\" DROP UNIQUE \"%s\"", index.tableName, index.indexName));
//				} catch (Exception x) {
//					logger.warn(x.toString(), x);
//				}
				if (index.indexName.startsWith("SQL")) {
					logger.info("dropAllIndices: Skipping internal index: tableName='{}' indexName='{}'", index.tableName, index.indexName);
					continue;
				}

				logger.info("dropAllIndices: Dropping index: tableName='{}' indexName='{}'", index.tableName, index.indexName);
				try {
					statement.executeUpdate(String.format("DROP INDEX \"%s\"", index.indexName));
				} catch (Exception x) {
					logger.warn(x.toString(), x);
				}
			}
		} finally {
			connection.close();
		}
	}

	private Set<Table> getTables(Connection connection) throws Exception {
		Set<Table> result = new HashSet<Table>();
		ResultSet rs = connection.getMetaData().getTables(null, null, null, null);
		while (rs.next()) {
			String schemaName = rs.getString("TABLE_SCHEM");
//			String catalogName = rs.getString("TABLE_CAT");
			String tableName = rs.getString("TABLE_NAME");
			String tableType = rs.getString("TABLE_TYPE");

			if (!"TABLE".equals(tableType == null ? "" : tableType.toUpperCase()))
				continue;

			result.add(new Table(schemaName, tableName));
		}
		return result;
	}

	private Set<Index> getForeignKeys(Connection connection) throws Exception {
		Set<Index> result = new HashSet<Index>();
		for (Table table : getTables(connection)) {
			ResultSet rs = connection.getMetaData().getExportedKeys(null, null, table.tableName); //table.schemaName, table.tableName);
			while (rs.next()) {
				String schemaName = rs.getString("FKTABLE_SCHEM");
				String tableName = rs.getString("FKTABLE_NAME");
				String indexName = rs.getString("FK_NAME");
				result.add(new Index(schemaName, tableName, indexName));
			}
		}
		return result;
	}

	private Set<Index> getIndexes(Connection connection) throws Exception {
		Set<Index> result = new HashSet<Index>();
		for (Table table : getTables(connection)) {
			ResultSet rs = connection.getMetaData().getIndexInfo(null, table.schemaName, table.tableName, false, false);
			while (rs.next()) {
				String schemaName = rs.getString("TABLE_SCHEM");
				String tableName = rs.getString("TABLE_NAME");
				String indexName = rs.getString("INDEX_NAME");
				result.add(new Index(schemaName, tableName, indexName));
			}
		}
		return result;
	}

	private static class Table {
		public final String schemaName;
		public final String tableName;

		public Table(String schemaName, String tableName) {
			this.schemaName = schemaName;
			this.tableName = tableName;
		}

		@Override
		public int hashCode() {
			final int prime = 31;
			int result = 1;
			result = prime * result + ((schemaName == null) ? 0 : schemaName.hashCode());
			result = prime * result + ((tableName == null) ? 0 : tableName.hashCode());
			return result;
		}
		@Override
		public boolean equals(Object obj) {
			if (this == obj)
				return true;
			if (obj == null)
				return false;
			if (getClass() != obj.getClass())
				return false;
			Table other = (Table) obj;
			return Util.equals(this.tableName, other.tableName) && Util.equals(this.schemaName, other.schemaName);
		}
		@Override
		public String toString() {
			return getClass().getSimpleName() + '[' + schemaName + '.' + tableName + ']';
		}
	}

	private static class Index {
		public final String schemaName;
		public final String tableName;
		public final String indexName;

		public Index(String schemaName, String tableName, String indexName) {
			this.schemaName = schemaName;
			this.tableName = tableName;
			this.indexName = indexName;
		}

		@Override
		public int hashCode() {
			final int prime = 31;
			int result = 1;
			result = prime * result + ((schemaName == null) ? 0 : schemaName.hashCode());
			result = prime * result + ((tableName == null) ? 0 : tableName.hashCode());
			result = prime * result + ((indexName == null) ? 0 : indexName.hashCode());
			return result;
		}
		@Override
		public boolean equals(Object obj) {
			if (this == obj)
				return true;
			if (obj == null)
				return false;
			if (getClass() != obj.getClass())
				return false;
			Index other = (Index) obj;
			return Util.equals(this.indexName, other.indexName) && Util.equals(this.tableName, other.tableName) && Util.equals(this.schemaName, other.schemaName);
		}
		@Override
		public String toString() {
			return getClass().getSimpleName() + '[' + schemaName + '.' + tableName + '.' + indexName + ']';
		}
	}
}
