package org.cumulus4j.jee.test.ejb;

import java.sql.Connection;

import javax.annotation.Resource;
import javax.ejb.Stateless;
import javax.sql.DataSource;

@Stateless
public class DefaultDataSourceTestBean
extends AbstractPlainDataSourceTestBean
implements DefaultDataSourceTestRemote
{
	@Resource(name="jdbc/__default")
	private DataSource defaultDataSource;

	@Override
	public void test() {
		try {
			System.out.println(String.format("DefaultDataSourceTestBean.test: defaultDataSource=%s", defaultDataSource));
			Connection connection = defaultDataSource.getConnection();
			System.out.println(String.format("DefaultDataSourceTestBean.test: connection=%s", connection));
			executeSomeTestSQL(connection, "plain-");
		} catch (Exception x) {
			throw new RuntimeException(x);
		}
	}
}
