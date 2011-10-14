package org.polepos.teams.jdo.cumulus4j;

import java.util.Collection;

import javax.jdo.PersistenceManager;
import javax.jdo.Query;

import org.polepos.circuits.flatobject.FlatObject;
import org.polepos.data.IndexedObject;
import org.polepos.teams.jdo.cumulus4j.data.Cumulus4jIndexedObject;

public class FlatObjectCumulus4j extends Cumulus4jDriver implements FlatObject{

	public void write(){
		begin();
        initializeTestId(objectCount());
		while ( hasMoreTestIds()){
			Cumulus4jIndexedObject indexedObject = new Cumulus4jIndexedObject(nextTestId());
			store(indexedObject);
			if(doCommit()){
				commit();
				begin();
			}
            addToCheckSum(indexedObject);
		}
		commit();
	}

    public void queryIndexedString() {
        initializeTestId(setup().getSelectCount());
        String filter = "this._string == param";
        while(hasMoreTestIds()) {
            Query query = db().newQuery(Cumulus4jIndexedObject.class, filter);
            query.declareParameters("String param");
            doQuery(query, IndexedObject.queryString(nextTestId()));
        }
    }

    public void queryIndexedInt() {
        initializeTestId(setup().getSelectCount());
        String filter = "this._int == param";
        while(hasMoreTestIds()) {
            Query query = db().newQuery(Cumulus4jIndexedObject.class, filter);
            query.declareParameters("Integer param");
            doQuery(query, nextTestId());
        }
    }

    public void update() {
    	PersistenceManager pm = db();
    	begin();
    	String filter = "this._int == param";
        initializeTestId(setup().getUpdateCount());
        while(hasMoreTestIds()) {
            Query query = db().newQuery(Cumulus4jIndexedObject.class, filter);
            query.declareParameters("Integer param");
            Collection result = (Collection)query.execute(nextTestId());
            Cumulus4jIndexedObject indexedObject = (Cumulus4jIndexedObject) result.iterator().next();
        	indexedObject.updateString();
            addToCheckSum(indexedObject);
        }
        commit();
	}

    public void delete() {
    	PersistenceManager pm = db();
    	begin();
    	String filter = "this._int == param";
        initializeTestId(setup().getUpdateCount());
        while(hasMoreTestIds()) {
            Query query = db().newQuery(Cumulus4jIndexedObject.class, filter);
            query.declareParameters("Integer param");
            Collection result = (Collection)query.execute(nextTestId());
            Cumulus4jIndexedObject indexedObject = (Cumulus4jIndexedObject) result.iterator().next();
            addToCheckSum(indexedObject);
        	indexedObject.updateString();
        	delete(indexedObject);
        }
        commit();
    }

}
