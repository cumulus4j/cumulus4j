package org.cumulus4j.test.framework;

import java.io.File;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Properties;

import org.apache.log4j.Logger;

/**
 * Util class to clean up the database. Its (currently) only method is {@link #dropAllTables()} which
 * uses the 'datanucleus.properties' to obtain a JDBC connection and then to drop all tables in it, thus
 * ensuring that every test run finds exactly the same situation - an empty database.
 *
 * @author Marco หงุ่ยตระกูล-Schulze - marco at nightlabs dot de
 */
public class CleanupUtil
{
	private static final Logger logger = Logger.getLogger(CleanupUtil.class);

	public static void dropAllTables() throws Exception {
		Properties properties = TestUtil.loadProperties("datanucleus.properties");

		logger.debug("Deleting all tables");
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
			}

			// simply delete the directory - the drop table commands failed and I don't have time to find out why
			String url = properties.getProperty("javax.jdo.option.ConnectionURL");
			if (!url.startsWith("jdbc:derby:"))
				throw new IllegalStateException("URL is not \"jdbc:derby:\"");

			String path = url.substring(11);
			int semicolonIdx = path.indexOf(';');
			if (semicolonIdx >= 0)
				path = path.substring(0, semicolonIdx);

			if (!deleteDirectoryRecursively(new File(path)))
				throw new IllegalStateException("Deleting Derby database \"" + path + "\" failed!");

			return;
		}

		String url = properties.getProperty("javax.jdo.option.ConnectionURL");
		java.sql.Connection con = DriverManager.getConnection(
				url,
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
				};
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
					}
			}
		}

		try {
			return dir.delete();
		} catch(SecurityException e) {
			return false;
		}
	}
}
