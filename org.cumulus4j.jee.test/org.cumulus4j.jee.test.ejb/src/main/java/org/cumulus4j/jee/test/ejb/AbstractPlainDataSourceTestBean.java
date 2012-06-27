package org.cumulus4j.jee.test.ejb;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import javax.resource.spi.IllegalStateException;

public abstract class AbstractPlainDataSourceTestBean {

	protected void executeSomeTestSQL(Connection connection, String prefix) throws SQLException, IllegalStateException
	{
		String tableName = "testtable";
		Statement statement = connection.createStatement();
		statement.execute(String.format("CREATE TABLE %s (id INT NOT NULL, name VARCHAR(255))", tableName));
		statement.execute(String.format("INSERT INTO %s VALUES (1, '%saaa')", tableName, prefix));
		statement.execute(String.format("INSERT INTO %s VALUES (2, '%sbbb')", tableName, prefix));
		statement.execute(String.format("INSERT INTO %s VALUES (3, '%sccc')", tableName, prefix));

		PreparedStatement queryStatement = connection.prepareStatement(String.format("SELECT * FROM %s", tableName));
		ResultSet resultSet = queryStatement.executeQuery();
		if (!resultSet.next())
			throw new IllegalStateException("Inserted a few records, but resultSet is empty!");
		else {
			do {
				System.out.println(String.format("DefaultDataSourceTestBean.test: cell[1]='%s' cell[2]='%s'", resultSet.getObject(1), resultSet.getObject(2)));
			} while (resultSet.next());
		}
	}

}
