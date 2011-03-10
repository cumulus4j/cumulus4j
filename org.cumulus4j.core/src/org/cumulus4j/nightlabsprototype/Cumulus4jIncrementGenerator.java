package org.cumulus4j.nightlabsprototype;

import java.util.Properties;

import javax.jdo.PersistenceManager;

import org.cumulus4j.nightlabsprototype.model.Sequence;
import org.datanucleus.store.connection.ManagedConnection;
import org.datanucleus.store.valuegenerator.AbstractDatastoreGenerator;
import org.datanucleus.store.valuegenerator.ValueGenerationBlock;

public class Cumulus4jIncrementGenerator extends AbstractDatastoreGenerator
{
	private String sequenceName;

	public Cumulus4jIncrementGenerator(String name, Properties props) {
		super(name, props);
		allocationSize = 5;

        // TODO Check these names and what we want to use for Cumulus4j (classname or fieldname)
        if (properties.getProperty("sequence-name") != null)
        {
            // Specified sequence-name so use that
            sequenceName = properties.getProperty("sequence-name");
        }
        else if (properties.getProperty("field-name") != null)
        {
            // Use field name as the sequence name so we have one sequence per field on the class
            sequenceName = properties.getProperty("field-name");
        }
        else
        {
            // Use actual class name as the sequence name so we have one sequence per class
            sequenceName = properties.getProperty("class-name");
        }
	}

	@Override
	protected ValueGenerationBlock reserveBlock(long size) {
		if (size > Integer.MAX_VALUE)
			throw new IllegalStateException("Cannot reserve a block of more than " + Integer.MAX_VALUE + " values!");

		Long[] values = new Long[(int)size];
		ManagedConnection mconn = connectionProvider.retrieveConnection();
		try {
			PersistenceManager pm = (PersistenceManager) mconn.getConnection();
			pm.currentTransaction().setSerializeRead(true);
			try {
				Sequence sequence = Sequence.createSequence(pm, sequenceName);
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
