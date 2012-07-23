package org.cumulus4j.jee.test.ejb.plaindatasource;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.UUID;

import javax.resource.spi.IllegalStateException;

public abstract class AbstractPlainDataSourceTestBean {

	protected String tableName = "testtable";

	protected void executeSomeTestSQL(Connection connection, String prefix) throws SQLException, IllegalStateException
	{


		Statement statement = connection.createStatement();
		statement.execute(String.format("INSERT INTO %s VALUES ('%saaa')", tableName, prefix));
		statement.execute(String.format("INSERT INTO %s VALUES ('%sbbb')", tableName, prefix));
		statement.execute(String.format("INSERT INTO %s VALUES ('%sccc')", tableName, prefix));

		PreparedStatement queryStatement = connection.prepareStatement(String.format("SELECT * FROM %s", tableName));
		ResultSet resultSet = queryStatement.executeQuery();
		if (!resultSet.next())
			throw new IllegalStateException("Inserted a few records, but resultSet is empty!");
		else {
			do {
				System.out.println(String.format("DefaultDataSourceTestBean.test: cell[1]='%s'", resultSet.getObject(1)));
			} while (resultSet.next());
		}
	}

	protected void storeId(Connection connection, UUID id1) throws SQLException{

		Statement statement = connection.createStatement();
		statement.execute(String.format("INSERT INTO %s VALUES ('%s')", tableName, id1.toString()));
	}
}
