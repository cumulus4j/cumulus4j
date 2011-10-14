package org.polepos.teams.jdo.cumulus4j;

import java.util.Collection;
import java.util.Iterator;

import javax.jdo.Query;

import org.polepos.circuits.nestedlists.NestedLists;
import org.polepos.framework.Procedure;
import org.polepos.framework.Visitor;
import org.polepos.teams.jdo.cumulus4j.data.ListHolder;

public class NestedListsCumulus4j extends Cumulus4jDriver implements NestedLists{

	@Override
	public void create() throws Throwable {
		begin();
		store(ListHolder.generate(depth(), objectCount(), reuse()));
		commit();
	}

	@Override
	public void read() throws Throwable {
		ListHolder root = root();
		root.accept(new Visitor<ListHolder>(){
			public void visit(ListHolder listHolder){
				addToCheckSum(listHolder);
			}
		});
	}

	private ListHolder root() {
		beginRead();
        Query query = db().newQuery(ListHolder.class, "this._name == '" + ListHolder.ROOT_NAME + "'");
        Collection<ListHolder> result = (Collection<ListHolder>)query.execute();
        if(result.size() != 1){
        	throw new IllegalStateException();
        }
        Iterator<ListHolder> it = result.iterator();
        return it.next();
	}

	@Override
	public void update() throws Throwable {
		begin();
		ListHolder root = root();
		addToCheckSum(root.update(depth(), new Procedure<ListHolder>() {
			@Override
			public void apply(ListHolder obj) {
				store(obj);
			}
		}));
		commit();
	}

	@Override
	public void delete() throws Throwable {
		begin();
		ListHolder root = root();
		addToCheckSum(root.delete(depth(), new Procedure<ListHolder>() {
			@Override
			public void apply(ListHolder listHolder) {
				delete(listHolder);
			}
		}));
		commit();
	}

}
