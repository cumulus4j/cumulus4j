package org.polepos.teams.jdo.cumulus4j;

import java.util.Collection;
import java.util.Iterator;
import java.util.List;

import javax.jdo.Query;

import org.polepos.circuits.complex.Complex;
import org.polepos.framework.NullVisitor;
import org.polepos.framework.Visitor;
import org.polepos.teams.jdo.cumulus4j.data.Cumulus4jComplexHolder0;
import org.polepos.teams.jdo.cumulus4j.data.Cumulus4jComplexHolder2;
import org.polepos.teams.jdo.cumulus4j.data.Cumulus4jComplexRoot;


public class ComplexJdoCumulus4j extends JdoCumulus4jDriver implements Complex{

	@Override
	public void write() {
		begin();
		Cumulus4jComplexHolder0 holder = Cumulus4jComplexHolder0.generate(depth(), objectCount());
		addToCheckSum(holder);
		store(new Cumulus4jComplexRoot(holder));
		commit();
	}

	@Override
	public void read() {
		beginRead();
		Cumulus4jComplexHolder0 holder = rootHolder();
		addToCheckSum(holder);
	}

	private Cumulus4jComplexHolder0 rootHolder() {
        return root().getHolder();
	}

	private Cumulus4jComplexRoot root() {
		Query query = db().newQuery(Cumulus4jComplexRoot.class);
        Collection result = (Collection) query.execute();
		Iterator it = result.iterator();
		if(! it.hasNext()){
			throw new IllegalStateException("no Cumulus4jComplexRoot found");
		}
		Cumulus4jComplexRoot root = (Cumulus4jComplexRoot) it.next();
		if(it.hasNext()){
			throw new IllegalStateException("More than one Cumulus4jComplexRoot found");
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
	        Query query = db().newQuery(Cumulus4jComplexHolder2.class, filter);
	        query.declareParameters("int param");
	        Collection result = (Collection) query.execute(currentInt);
			Iterator it = result.iterator();
			if(! it.hasNext()){
				throw new IllegalStateException("no Cumulus4jComplexHolder2 found");
			}
			Cumulus4jComplexHolder2 holder = (Cumulus4jComplexHolder2) it.next();
			addToCheckSum(holder.ownCheckSum());
			if(it.hasNext()){
				throw new IllegalStateException("More than one Cumulus4jComplexHolder2 found");
			}
			List<Cumulus4jComplexHolder0> children = holder.getChildren();
			for (Cumulus4jComplexHolder0 child : children) {
				addToCheckSum(child.ownCheckSum());
			}
			Cumulus4jComplexHolder0[] array = holder.getArray();
			for (Cumulus4jComplexHolder0 arrayElement : array) {
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
		Cumulus4jComplexHolder0 holder = rootHolder();
		holder.traverse(new NullVisitor<Cumulus4jComplexHolder0>(),
				new Visitor<Cumulus4jComplexHolder0>() {
			@Override
			public void visit(Cumulus4jComplexHolder0 holder) {
				addToCheckSum(holder.ownCheckSum());
				holder.setName("updated");
				List<Cumulus4jComplexHolder0> children = holder.getChildren();
				Cumulus4jComplexHolder0[] array = new Cumulus4jComplexHolder0[children.size()];
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
		Cumulus4jComplexRoot root = root();
		Cumulus4jComplexHolder0 holder = root.getHolder();
		delete(root);
		holder.traverse(
			new NullVisitor<Cumulus4jComplexHolder0>(),
			new Visitor<Cumulus4jComplexHolder0>() {
			@Override
			public void visit(Cumulus4jComplexHolder0 holder) {
				addToCheckSum(holder.ownCheckSum());
				delete(holder);
			}
		});
		commit();
	}

}
