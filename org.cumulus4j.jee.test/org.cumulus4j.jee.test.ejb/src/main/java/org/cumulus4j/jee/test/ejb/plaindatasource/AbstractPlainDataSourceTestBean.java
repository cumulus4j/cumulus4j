package org.cumulus4j.jee.test.ejb.plaindatasource;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.UUID;

import javax.annotation.Resource;
import javax.sql.DataSource;

public abstract class AbstractPlainDataSourceTestBean {

	protected String tableName = "testtable";

	@Resource(name = "jdbc/__default")
	protected DataSource defaultDataSource;

//	protected void executeSomeTestSQL(String prefix) throws SQLException, IllegalStateException
//	{
//		Connection connection = defaultDataSource.getConnection();
//
//		Statement statement = connection.createStatement();
//		statement.execute(String.format("INSERT INTO %s VALUES ('%saaa')", tableName, prefix));
//		statement.execute(String.format("INSERT INTO %s VALUES ('%sbbb')", tableName, prefix));
//		statement.execute(String.format("INSERT INTO %s VALUES ('%sccc')", tableName, prefix));
//
//		PreparedStatement queryStatement = connection.prepareStatement(String.format("SELECT * FROM %s", tableName));
//		ResultSet resultSet = queryStatement.executeQuery();
//		if (!resultSet.next())
//			throw new IllegalStateException("Inserted a few records, but resultSet is empty!");
//		else {
//			do {
//				System.out.println(String.format("PlainDataSourceTestBean.test: cell[1]='%s'", resultSet.getObject(1)));
//			} while (resultSet.next());
//		}
//	}

	protected void storeId(UUID id1) throws SQLException{

		Connection connection = defaultDataSource.getConnection();

		Statement statement = connection.createStatement();
		statement.execute(String.format("INSERT INTO %s VALUES ('%s')", tableName, id1.toString()));
	}
}
