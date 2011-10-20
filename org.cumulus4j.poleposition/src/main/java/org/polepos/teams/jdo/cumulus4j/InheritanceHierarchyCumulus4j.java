package org.polepos.teams.jdo.cumulus4j;

import java.util.Iterator;

import javax.jdo.Extent;
import javax.jdo.Query;

import org.polepos.circuits.inheritancehierarchy.InheritanceHierarchy;
import org.polepos.teams.jdo.cumulus4j.data.Cumulus4jInheritanceHierarchy4;

public class InheritanceHierarchyCumulus4j extends Cumulus4jDriver implements InheritanceHierarchy{

	@Override
    public void write(){
        int count = setup().getObjectCount();
        begin();
        for (int i = 1; i<= count; i++) {
            Cumulus4jInheritanceHierarchy4 cumulus4jInheritanceHierarchy4 = new Cumulus4jInheritanceHierarchy4();
            cumulus4jInheritanceHierarchy4.setAll(i);
            store(cumulus4jInheritanceHierarchy4);
        }
        commit();
    }

	@Override
    public void read(){
        readExtent(Cumulus4jInheritanceHierarchy4.class);
    }

	@Override
	public void query(){
        int count = setup().getSelectCount();
        String filter = "this.i2 == param";
        for (int i = 1; i <= count; i++) {
            Query query = db().newQuery(Cumulus4jInheritanceHierarchy4.class, filter);
            query.declareParameters("int param");
            doQuery(query, i);
        }
    }

	@Override
	public void delete(){
        begin();
        Extent extent = db().getExtent(Cumulus4jInheritanceHierarchy4.class, false);
        Iterator it = extent.iterator();
        while(it.hasNext()){
            db().deletePersistent(it.next());
            addToCheckSum(5);
        }
        extent.closeAll();
        commit();
    }
}
