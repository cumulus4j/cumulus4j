/*
 * Cumulus4j - Securing your data in the cloud - http://cumulus4j.org
 * Copyright (C) 2011 NightLabs Consulting GmbH
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.cumulus4j.store;

import java.util.Properties;

import javax.jdo.PersistenceManager;

import org.cumulus4j.store.Cumulus4jConnectionFactory.Cumulus4jManagedConnection;
import org.cumulus4j.store.crypto.CryptoContext;
import org.cumulus4j.store.model.Sequence2;
import org.cumulus4j.store.model.Sequence2DAO;
import org.datanucleus.store.valuegenerator.AbstractDatastoreGenerator;
import org.datanucleus.store.valuegenerator.ValueGenerationBlock;
import org.datanucleus.store.valuegenerator.ValueGenerator;

/**
 * {@link ValueGenerator} implementation generating values by incrementing a counter.
 * @author Marco หงุ่ยตระกูล-Schulze - marco at nightlabs dot de
 */
public class Cumulus4jIncrementGenerator extends AbstractDatastoreGenerator
{
	private String sequenceName;

	/**
	 * Create an instance. This is called by DataNucleus.
	 * @param name symbolic name for the generator.
	 * @param props Properties controlling the behaviour of the generator.
	 */
	public Cumulus4jIncrementGenerator(String name, Properties props) {
		super(name, props);
		allocationSize = 5;

		// TODO Check these names and what we want to use for Cumulus4j (classname or fieldname)
		if (properties.getProperty("sequence-name") != null) {
			// Specified sequence-name so use that
			sequenceName = properties.getProperty("sequence-name");
		}
		else if (properties.getProperty("field-name") != null) {
			// Use field name as the sequence name so we have one sequence per field on the class
			sequenceName = properties.getProperty("field-name");
		}
		else {
			// Use actual class name as the sequence name so we have one sequence per class
			sequenceName = properties.getProperty("class-name");
		}
	}

	@Override
	protected ValueGenerationBlock reserveBlock(long size) {
		if (size > Integer.MAX_VALUE)
			throw new IllegalStateException("Cannot reserve a block of more than " + Integer.MAX_VALUE + " values!");

		Long[] values = new Long[(int)size];
		Cumulus4jManagedConnection mconn = (Cumulus4jManagedConnection) connectionProvider.retrieveConnection();
		try {
			PersistenceManagerConnection pmConn = (PersistenceManagerConnection)mconn.getConnection();
			PersistenceManager pm = pmConn.getDataPM();
			Cumulus4jStoreManager storeManager = (Cumulus4jStoreManager) storeMgr;

			CryptoContext cryptoContext = new CryptoContext(
					storeManager.getEncryptionCoordinateSetManager(),
					storeManager.getKeyStoreRefManager(),
					mconn.getExecutionContext(),
					pmConn
			);
			storeManager.getDatastoreVersionManager().applyOnce(cryptoContext);

			pm.currentTransaction().setSerializeRead(true);
			try {
				Sequence2 sequence = new Sequence2DAO(pm, cryptoContext.getKeyStoreRefID()).createSequence2(sequenceName);
				long nextValue = sequence.getNextValue();
				for (int idx = 0; idx < values.length; ++idx) {
					values[idx] = nextValue++;
				}
				sequence.setNextValue(nextValue);
			} finally {
				pm.currentTransaction().setSerializeRead(false);
			}
		} finally {
			connectionProvider.releaseConnection();
		}
		return new ValueGenerationBlock(values);
	}
}
