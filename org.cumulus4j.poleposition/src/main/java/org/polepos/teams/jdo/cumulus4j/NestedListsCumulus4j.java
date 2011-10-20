package org.polepos.teams.jdo.cumulus4j;

import java.util.Collection;
import java.util.Iterator;

import javax.jdo.Query;

import org.polepos.circuits.nestedlists.NestedLists;
import org.polepos.framework.Procedure;
import org.polepos.framework.Visitor;
import org.polepos.teams.jdo.cumulus4j.data.Cumulus4jListHolder;

public class NestedListsCumulus4j extends Cumulus4jDriver implements NestedLists{

	@Override
	public void create() throws Throwable {
		begin();
		store(Cumulus4jListHolder.generate(depth(), objectCount(), reuse()));
		commit();
	}

	@Override
	public void read() throws Throwable {
		Cumulus4jListHolder root = root();
		root.accept(new Visitor<Cumulus4jListHolder>(){
			public void visit(Cumulus4jListHolder cumulus4jListHolder){
				addToCheckSum(cumulus4jListHolder);
			}
		});
	}

	private Cumulus4jListHolder root() {
		beginRead();
        Query query = db().newQuery(Cumulus4jListHolder.class, "this._name == '" + Cumulus4jListHolder.ROOT_NAME + "'");
        Collection<Cumulus4jListHolder> result = (Collection<Cumulus4jListHolder>)query.execute();
        if(result.size() != 1){
        	throw new IllegalStateException();
        }
        Iterator<Cumulus4jListHolder> it = result.iterator();
        return it.next();
	}

	@Override
	public void update() throws Throwable {
		begin();
		Cumulus4jListHolder root = root();
		addToCheckSum(root.update(depth(), new Procedure<Cumulus4jListHolder>() {
			@Override
			public void apply(Cumulus4jListHolder obj) {
				store(obj);
			}
		}));
		commit();
	}

	@Override
	public void delete() throws Throwable {
		begin();
		Cumulus4jListHolder root = root();
		addToCheckSum(root.delete(depth(), new Procedure<Cumulus4jListHolder>() {
			@Override
			public void apply(Cumulus4jListHolder cumulus4jListHolder) {
				delete(cumulus4jListHolder);
			}
		}));
		commit();
	}

}
