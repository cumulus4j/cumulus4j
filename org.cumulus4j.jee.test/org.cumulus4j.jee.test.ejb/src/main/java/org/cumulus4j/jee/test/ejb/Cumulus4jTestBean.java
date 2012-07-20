package org.cumulus4j.jee.test.ejb;

import java.sql.Connection;

import javax.annotation.Resource;
import javax.ejb.Stateless;
import javax.sql.DataSource;

@Stateless
public class Cumulus4jTestBean
extends AbstractPlainDataSourceTestBean // FIXME - why does Cumulus4jTestBean subclass this class?! Shouldn't it subclass AbstractDataNucleusTestBean?!!!
implements Cumulus4jTestRemote
{
	@Resource(name="jdbc/__defaultC4j")
	private DataSource defaultDataSourceC4j;

	@Override
	public void test() {
		try {
			System.out.println(String.format("DefaultDataSourceTestBean.test: defaultDataSourceC4j=%s", defaultDataSourceC4j));
			Connection connection = defaultDataSourceC4j.getConnection();
			System.out.println(String.format("DefaultDataSourceTestBean.test: connection=%s", connection));
			executeSomeTestSQL(connection, "c4j-");
		} catch (Exception x) {
			throw new RuntimeException(x);
		}
	}
}
