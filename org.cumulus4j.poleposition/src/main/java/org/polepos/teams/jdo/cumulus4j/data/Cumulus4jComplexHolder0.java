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


package org.polepos.teams.jdo.cumulus4j.data;

import java.util.*;

import org.polepos.framework.*;

import com.db4o.foundation.*;

public class Cumulus4jComplexHolder0 implements CheckSummable {
	
	private String name;
	
	private List<Cumulus4jComplexHolder0> children = new ArrayList<Cumulus4jComplexHolder0>();
	
	private Cumulus4jComplexHolder0[] array;
	
	public Cumulus4jComplexHolder0() {
		
	}
	
	public static Cumulus4jComplexHolder0 generate(int depth, int leafs){
		Cumulus4jComplexHolder0 complexHolder = new Cumulus4jComplexHolder0();
		complexHolder.name = "root";
		createChildren(complexHolder, depth -1, leafs);
		return complexHolder;
	}
	
	private static void createChildren(Cumulus4jComplexHolder0 root, int depth, int numChildren) {
		if(depth < 1){
			return;
		}
		int factoryIdx = 0;
		int holderIdx = 0;
		List<Cumulus4jComplexHolder0> parentLevel = Arrays.asList(root);
		for (int i = 0; i < depth; i++) {
			Closure4<Cumulus4jComplexHolder0> curFactory = FACTORIES[factoryIdx];
			List<Cumulus4jComplexHolder0> childLevel = new ArrayList<Cumulus4jComplexHolder0>();

			for (Cumulus4jComplexHolder0 curParent : parentLevel) {
				for (int childIdx = 0; childIdx < numChildren; childIdx++) {
					Cumulus4jComplexHolder0 curChild = curFactory.run();
					curChild.name = String.valueOf(holderIdx);
					curChild.array = createArray(holderIdx);
					curChild.setSpecial(holderIdx);
					curParent.addChild(curChild);
					childLevel.add(curChild);
					holderIdx++;
				}
			}

			parentLevel = childLevel;
			
			factoryIdx++;
			if(factoryIdx == FACTORIES.length) {
				factoryIdx = 0;
			}
		}
		
	}

	private static Cumulus4jComplexHolder0[] createArray(int holderIdx) {
		Cumulus4jComplexHolder0[] holders = new Cumulus4jComplexHolder0[] {
			new Cumulus4jComplexHolder0(),
			new Cumulus4jComplexHolder1(),
			new Cumulus4jComplexHolder2(),
			new Cumulus4jComplexHolder3(),
			new Cumulus4jComplexHolder4(),
		};
		for (int i = 0; i < holders.length; i++) {
			holders[i].name = "a" + holderIdx + "_" + i;
		}
		return holders;
	}

	public void addChild(Cumulus4jComplexHolder0 child) {
		children.add(child);
	}


	public static final Closure4[] FACTORIES = {
		new Closure4<Cumulus4jComplexHolder0>(){
			@Override
			public Cumulus4jComplexHolder0 run() {
				return new Cumulus4jComplexHolder0();
			}
		},
		new Closure4<Cumulus4jComplexHolder0>(){
			@Override
			public Cumulus4jComplexHolder0 run() {
				return new Cumulus4jComplexHolder1();
			}
		},
		new Closure4<Cumulus4jComplexHolder0>(){
			@Override
			public Cumulus4jComplexHolder0 run() {
				return new Cumulus4jComplexHolder2();
			}
		},
		new Closure4<Cumulus4jComplexHolder0>(){
			@Override
			public Cumulus4jComplexHolder0 run() {
				return new Cumulus4jComplexHolder3();
			}
		},
		new Closure4<Cumulus4jComplexHolder0>(){
			@Override
			public Cumulus4jComplexHolder0 run() {
				return new Cumulus4jComplexHolder4();
			}
		}
	};

	@Override
	public long checkSum() {
		
		class CheckSumVisitor implements Visitor<Cumulus4jComplexHolder0> {
			
			long checkSum;
			
			@Override
			public void visit(Cumulus4jComplexHolder0 holder) {
				checkSum += Math.abs(holder.ownCheckSum());
			}
		}
		CheckSumVisitor visitor = new CheckSumVisitor();
		traverse(visitor, new NullVisitor<Cumulus4jComplexHolder0>());
		return visitor.checkSum;
	}

	public void traverse(Visitor<Cumulus4jComplexHolder0> preVisitor, Visitor<Cumulus4jComplexHolder0> postVisitor) {
		internalTraverse(new IdentityHashMap<Cumulus4jComplexHolder0, Cumulus4jComplexHolder0>(), preVisitor, postVisitor);
	}

	private void internalTraverse(IdentityHashMap<Cumulus4jComplexHolder0, Cumulus4jComplexHolder0> visited, Visitor<Cumulus4jComplexHolder0> preVisitor, Visitor<Cumulus4jComplexHolder0> postVisitor) {
		if(visited.containsKey(this)) {
			return;
		}
		visited.put(this, this);
		preVisitor.visit(this);
		for (Cumulus4jComplexHolder0 child : getChildren()) {
			child.internalTraverse(visited, preVisitor, postVisitor);
		}
		if(getArray() != null) {
			for (Cumulus4jComplexHolder0 child : getArray()) {
				child.internalTraverse(visited, preVisitor, postVisitor);
			}
		}
		postVisitor.visit(this);
	}
	

	public long ownCheckSum() {
		return getName().hashCode();
	}

	protected void setSpecial(int value) {
	}
	
	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public List<Cumulus4jComplexHolder0> getChildren() {
		return children;
	}
	
	public void setChildren(List<Cumulus4jComplexHolder0> children) {
		this.children = children;
	}
	
	public Cumulus4jComplexHolder0[] getArray() {
		return array;
	}

	public void setArray(Cumulus4jComplexHolder0[] array) {
		this.array = array;
	}


}
