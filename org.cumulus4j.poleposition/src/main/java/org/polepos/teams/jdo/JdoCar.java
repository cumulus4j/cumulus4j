/*
This file is part of the PolePosition database benchmark
http://www.polepos.org

This program is free software; you can redistribute it and/or
modify it under the terms of the GNU General Public License
as published by the Free Software Foundation; either version 2
of the License, or (at your option) any later version.

This program is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
GNU General Public License for more details.

You should have received a copy of the GNU General Public
License along with this program; if not, write to the Free
Software Foundation, Inc., 59 Temple Place - Suite 330, Boston,
MA  02111-1307, USA. */

package org.polepos.teams.jdo;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.Map;

import javax.jdo.JDOHelper;
import javax.jdo.PersistenceManager;
import javax.jdo.PersistenceManagerFactory;
import javax.jdo.datastore.JDOConnection;

import org.apache.commons.lang.ArrayUtils;
import org.polepos.framework.Car;
import org.polepos.framework.CarMotorFailureException;
import org.polepos.framework.Team;
import org.polepos.teams.jdbc.Jdbc;
import org.polepos.teams.jdbc.JdbcCar;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public class JdoCar extends Car
{
	private static final Logger logger = LoggerFactory.getLogger(JdoCar.class);

	private transient PersistenceManagerFactory _persistenceManagerFactory;

	protected final String              mDbName;
	protected final String              mName;
	protected JdoImplSettings jdoImplSettings;

	protected JdoCar(Team team, String name, String dbName, String color) throws CarMotorFailureException {
		this(team, name, dbName, color, true);
	}

	protected JdoCar(Team team, String name, String dbName, String color, boolean initialize) throws CarMotorFailureException {
		super(team, color);

		mName = name;
		mDbName = dbName;

		_website = Jdo.settings().getWebsite(name);
		_description = Jdo.settings().getDescription(name);

		int idx = ArrayUtils.indexOf(Jdo.settings().getJdoImplementations(), mName);
		jdoImplSettings = Jdo.settings().getJdoImplSettings()[idx];

	}

	private boolean isSQL() {
		return mDbName != null;
	}

	public Map<String, String> getPersistenceEngineProperties()
	{
		return jdoImplSettings.getFilteredProperties(mDbName);
	}

	protected PersistenceManagerFactory createPersistenceManagerFactory() {
		logger.info("createPersistenceManagerFactory: Entered.");
		try {
			return JDOHelper.getPersistenceManagerFactory(getPersistenceEngineProperties(), JDOHelper.class.getClassLoader());
		} finally {
			logger.info("createPersistenceManagerFactory: Leaving.");
		}
	}

	protected PersistenceManagerFactory getPersistenceManagerFactory()
	{
		if (_persistenceManagerFactory == null)
			_persistenceManagerFactory = createPersistenceManagerFactory();

		return _persistenceManagerFactory;
	}

    public PersistenceManager getPersistenceManager() {

        PersistenceManager persistenceManager = getPersistenceManagerFactory().getPersistenceManager();

        if(! "hsqldb".equals(mDbName)){
        	return persistenceManager;
        }

        JDOConnection dataStoreConnection = persistenceManager.getDataStoreConnection();
        Connection connection = (Connection) dataStoreConnection.getNativeConnection();

        JdbcCar.hsqlDbWriteDelayToZero(connection);
        try {

        	// Closing the connection here really feels strange, but otherwise
        	// Datanucleus hangs, probably because it runs out of JDBC connections.

			connection.close();

		} catch (SQLException e) {
			e.printStackTrace();
		}
		return persistenceManager;
    }

    @Override
    public String name() {

        if(isSQL()){
            return Jdo.settings().getName(mName) + "/" +Jdbc.settings().getName(mDbName)+"-"+Jdbc.settings().getVersion(mDbName);
        }
        return Jdo.settings().getVendor(mName) + "/" + Jdo.settings().getName(mName)+"-"+Jdo.settings().getVersion(mName);

    }

	public void closePersistenceManagerFactory() {
		PersistenceManagerFactory pmf = _persistenceManagerFactory;
		_persistenceManagerFactory = null;
		if (pmf != null)
			pmf.close();
	}

}