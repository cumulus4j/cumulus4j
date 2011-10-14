package org.polepos.teams.jdo.cumulus4j;

import java.util.Collection;
import java.util.Iterator;
import java.util.List;

import javax.jdo.Query;

import org.polepos.circuits.complex.Complex;
import org.polepos.framework.NullVisitor;
import org.polepos.framework.Visitor;
import org.polepos.teams.jdo.cumulus4j.data.ComplexHolder0;
import org.polepos.teams.jdo.cumulus4j.data.ComplexHolder2;
import org.polepos.teams.jdo.cumulus4j.data.ComplexRoot;


public class ComplexCumulus4j extends Cumulus4jDriver implements Complex{

	@Override
	public void write() {
		begin();
		ComplexHolder0 holder = ComplexHolder0.generate(depth(), objectCount());
		addToCheckSum(holder);
		store(new ComplexRoot(holder));
		commit();
	}

	@Override
	public void read() {
		beginRead();
		ComplexHolder0 holder = rootHolder();
		addToCheckSum(holder);
	}

	private ComplexHolder0 rootHolder() {
        return root().getHolder();
	}

	private ComplexRoot root() {
		Query query = db().newQuery(ComplexRoot.class);
        Collection result = (Collection) query.execute();
		Iterator it = result.iterator();
		if(! it.hasNext()){
			throw new IllegalStateException("no ComplexRoot found");
		}
		ComplexRoot root = (ComplexRoot) it.next();
		if(it.hasNext()){
			throw new IllegalStateException("More than one ComplexRoot found");
		}
		return root;
	}

	@Override
	public void query() {
		beginRead();
		int selectCount = selectCount();
		int firstInt = objectCount() * objectCount() + objectCount();
		int lastInt = firstInt + (objectCount() * objectCount() * objectCount()) - 1;
		int currentInt = firstInt;
		for (int run = 0; run < selectCount; run++) {
	        String filter = "this.i2 == param";
	        Query query = db().newQuery(ComplexHolder2.class, filter);
	        query.declareParameters("int param");
	        Collection result = (Collection) query.execute(currentInt);
			Iterator it = result.iterator();
			if(! it.hasNext()){
				throw new IllegalStateException("no ComplexHolder2 found");
			}
			ComplexHolder2 holder = (ComplexHolder2) it.next();
			addToCheckSum(holder.ownCheckSum());
			if(it.hasNext()){
				throw new IllegalStateException("More than one ComplexHolder2 found");
			}
			List<ComplexHolder0> children = holder.getChildren();
			for (ComplexHolder0 child : children) {
				addToCheckSum(child.ownCheckSum());
			}
			ComplexHolder0[] array = holder.getArray();
			for (ComplexHolder0 arrayElement : array) {
				addToCheckSum(arrayElement.ownCheckSum());
			}
			currentInt++;
			if(currentInt > lastInt){
				currentInt = firstInt;
			}
		}

	}

	@Override
	public void update() {
		begin();
		ComplexHolder0 holder = rootHolder();
		holder.traverse(new NullVisitor<ComplexHolder0>(),
				new Visitor<ComplexHolder0>() {
			@Override
			public void visit(ComplexHolder0 holder) {
				addToCheckSum(holder.ownCheckSum());
				holder.setName("updated");
				List<ComplexHolder0> children = holder.getChildren();
				ComplexHolder0[] array = new ComplexHolder0[children.size()];
				for (int i = 0; i < array.length; i++) {
					array[i] = children.get(i);
				}
				holder.setArray(array);
			}
		});
		commit();
	}

	@Override
	public void delete() {
		begin();
		ComplexRoot root = root();
		ComplexHolder0 holder = root.getHolder();
		delete(root);
		holder.traverse(
			new NullVisitor<ComplexHolder0>(),
			new Visitor<ComplexHolder0>() {
			@Override
			public void visit(ComplexHolder0 holder) {
				addToCheckSum(holder.ownCheckSum());
				delete(holder);
			}
		});
		commit();
	}

}
