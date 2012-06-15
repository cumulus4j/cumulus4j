package org.cumulus4j.jee.test.ejb;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;

import javax.annotation.Resource;
import javax.ejb.Stateless;
import javax.resource.spi.IllegalStateException;
import javax.sql.DataSource;

@Stateless
public class DefaultDataSourceTestBean implements DefaultDataSourceTestRemote {

	@Resource(name="jdbc/__default")
	private DataSource defaultDataSource;

	@Override
	public void test() {
		try {
			System.out.println(String.format("DefaultDataSourceTestBean.test: defaultDataSource=%s", defaultDataSource));
			Connection connection = defaultDataSource.getConnection();
			System.out.println(String.format("DefaultDataSourceTestBean.test: connection=%s", connection));
			String tableName = "testtable";
			Statement statement = connection.createStatement();
			statement.execute(String.format("CREATE TABLE %s (id INT NOT NULL, name VARCHAR(255))", tableName));
			statement.execute(String.format("INSERT INTO %s VALUES (1, 'aaa')", tableName));
			statement.execute(String.format("INSERT INTO %s VALUES (2, 'bbb')", tableName));
			statement.execute(String.format("INSERT INTO %s VALUES (3, 'ccc')", tableName));

			PreparedStatement queryStatement = connection.prepareStatement(String.format("SELECT * FROM %s", tableName));
			ResultSet resultSet = queryStatement.executeQuery();
			if (!resultSet.next())
				throw new IllegalStateException("Inserted a few records, but resultSet is empty!");
			else {
				do {
					System.out.println(String.format("DefaultDataSourceTestBean.test: cell[1]='%s' cell[2]='%s'", resultSet.getObject(1), resultSet.getObject(2)));
				} while (resultSet.next());
			}
		} catch (Exception x) {
			throw new RuntimeException(x);
		}
	}

}
