package org.cumulus4j.jee.test.ejb;

import java.sql.Connection;

import javax.annotation.Resource;
import javax.ejb.Stateless;
import javax.sql.DataSource;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Stateless
public class DefaultDataSourceTestBean
extends AbstractPlainDataSourceTestBean
implements DefaultDataSourceTestRemote
{
	private static final Logger logger = LoggerFactory.getLogger(DefaultDataSourceTestBean.class);
	
	@Resource(name="jdbc/__default")
	private DataSource defaultDataSource;

	@Override
	public void test() {
		try {
			logger.trace("test: *** TRACE *** TRACE *** TRACE *** TRACE *** TRACE *** TRACE ***");
			logger.debug("test: *** DEBUG *** DEBUG *** DEBUG *** DEBUG *** DEBUG *** DEBUG ***");
			logger.info("test: *** INFO *** INFO *** INFO *** INFO *** INFO *** INFO ***");
			logger.warn("test: *** WARN *** WARN *** WARN *** WARN *** WARN *** WARN ***");
			logger.error("test: *** ERROR *** ERROR *** ERROR *** ERROR *** ERROR *** ERROR ***");

			logger.info("test: defaultDataSource={}", defaultDataSource);
			Connection connection = defaultDataSource.getConnection();
			logger.info("test: connection={}", connection);
			executeSomeTestSQL(connection, "plain-");
		} catch (Exception x) {
			throw new RuntimeException(x);
		}
	}
}
