package org.cumulus4j.jee.test.ejb.plaindatasource;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.UUID;

import javax.annotation.Resource;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.sql.DataSource;

public abstract class AbstractPlainDataSourceTestBean {

	protected String tableName = "testtable";

	/**
	 * Which way of obtaining (and maybe keeping) the data source should be used? The data source can be kept statically in
	 * a global variable (i.e. only one single JNDI lookup), or it can be looked up from JNDI every time it is needed,
	 * or it can be injected.
	 */
	private static final DataSourceManagementMode DATA_SOURCE_MANAGEMENT_MODE = DataSourceManagementMode.injection;

	private static final String JNDI_NAME_XA = "jdbc/__default";
	private static final String JNDI_NAME_NO_XA = "jdbc/__defaultNoXa";

	@Resource(name = JNDI_NAME_XA, mappedName="jdbc/__default")
	private DataSource _ds_xa;

	@Resource(name = JNDI_NAME_NO_XA, mappedName="jdbc/__defaultNoXa")
	private DataSource _ds_no_xa;

	private static DataSource defaultDataSourceXa;
	private static DataSource defaultDataSourceNoXa;

	private Object lookup(String name) {
		try {
			return new InitialContext().lookup(name);
		} catch (NamingException e) {
			throw new RuntimeException(e);
		}
	}

	protected DataSource getDefaultDataSourceXa() {
		switch (DATA_SOURCE_MANAGEMENT_MODE) {
			case injection:
				return _ds_xa;
			case lookupEveryTime:
				return (DataSource) lookup(JNDI_NAME_XA);
			case lookupOnceKeepGlobally:
				synchronized (AbstractPlainDataSourceTestBean.class) {
					if (defaultDataSourceXa == null) {
						defaultDataSourceXa = (DataSource) lookup(JNDI_NAME_XA);
					}
					return defaultDataSourceXa;
				}
			default:
				throw new IllegalStateException("Unknown mode: " + DATA_SOURCE_MANAGEMENT_MODE);
		}
	}

	protected DataSource getDefaultDataSourceNoXa() {
		switch (DATA_SOURCE_MANAGEMENT_MODE) {
			case injection:
				return _ds_no_xa;
			case lookupEveryTime:
				return (DataSource) lookup(JNDI_NAME_NO_XA);
			case lookupOnceKeepGlobally:
				synchronized (AbstractPlainDataSourceTestBean.class) {
					if (defaultDataSourceNoXa == null) {
						defaultDataSourceNoXa = (DataSource) lookup(JNDI_NAME_NO_XA);
					}
					return defaultDataSourceNoXa;
				}
			default:
				throw new IllegalStateException("Unknown mode: " + DATA_SOURCE_MANAGEMENT_MODE);
		}
	}

	protected void storeId(UUID id1) throws SQLException{

//		Connection connection = defaultDataSourceXa.getConnection();
	    Connection connection = getDefaultDataSourceXa().getConnection();
		Statement statement = connection.createStatement();
		statement.execute(String.format("INSERT INTO %s VALUES ('%s')", tableName, id1.toString()));
	}
}
