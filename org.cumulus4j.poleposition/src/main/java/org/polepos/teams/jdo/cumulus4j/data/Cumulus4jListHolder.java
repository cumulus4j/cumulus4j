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

import java.util.ArrayList;
import java.util.HashSet;
import java.util.IdentityHashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.polepos.framework.CheckSummable;
import org.polepos.framework.IdGenerator;
import org.polepos.framework.Procedure;
import org.polepos.framework.Visitor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Cumulus4jListHolder implements CheckSummable {

	public static final String ROOT_NAME = "root";

	private static IdGenerator _idGenerator = new IdGenerator();

	private long _id;

	private String _name;

	private List<Cumulus4jListHolder> _list;

	public static Cumulus4jListHolder generate(int depth, int leafs, int reuse){
		Cumulus4jListHolder root = generate(new ArrayList<Cumulus4jListHolder>(), depth, leafs, reuse);
		root._name = ROOT_NAME;
		return root;
	}

	private static Cumulus4jListHolder generate(List<Cumulus4jListHolder> flatList, int depth, int leafs, int reuse){
		if(depth == 0){
			return null;
		}
		Cumulus4jListHolder cumulus4jListHolder = new Cumulus4jListHolder();
		cumulus4jListHolder.setId(_idGenerator.nextId());

		flatList.add(cumulus4jListHolder);
		if(depth == 1){
			return cumulus4jListHolder;
		}
		cumulus4jListHolder.setList(new ArrayList<Cumulus4jListHolder>());
		int childDepth = depth -1;
		for (int i = leafs -1; i >= 0; i--) {
			if(i < reuse){
				int indexInList = (flatList.size() - i) / 2;
				if(indexInList < 0){
					indexInList = 0;
				}
				cumulus4jListHolder.getList().add(flatList.get(indexInList) );
			} else {
				Cumulus4jListHolder child = generate(flatList, childDepth, leafs, reuse);
				child._name = "child:" + depth + ":" + i;
				cumulus4jListHolder.getList().add(child);
			}
		}
		return cumulus4jListHolder;
	}

	@Override
	public long checkSum() {
		return _name.hashCode();
	}

	public void accept(Visitor<Cumulus4jListHolder> visitor) {
		Set<Cumulus4jListHolder> visited = new HashSet<Cumulus4jListHolder>();
		acceptInternal(visited, visitor);
	}

	private void acceptInternal(Set<Cumulus4jListHolder> visited, Visitor<Cumulus4jListHolder> visitor){
		if(visited.contains(this)){
			return;
		}
		visitor.visit(this);
		visited.add(this);
		if(getList() == null){
			return;
		}
		Iterator<Cumulus4jListHolder> i = getList().iterator();
		while(i.hasNext()){
			Cumulus4jListHolder child = i.next();
			child.acceptInternal(visited, visitor);
		}
	}

	public int update(int maxDepth, Procedure<Cumulus4jListHolder> storeProcedure) {
		Set<Cumulus4jListHolder> visited = new HashSet<Cumulus4jListHolder>();
		return updateInternal(visited, maxDepth, 0, storeProcedure);
	}


	public int updateInternal(Set<Cumulus4jListHolder> visited, int maxDepth, int depth, Procedure<Cumulus4jListHolder> storeProcedure) {
		if(visited.contains(this)){
			return 0;
		}
		visited.add(this);
		int updatedCount = 1;
		if(depth > 0){
			_name = "updated " + _name;
		}

		if(_list != null){
			for (int i = 0; i < _list.size(); i++) {
				Cumulus4jListHolder child = _list.get(i);
				updatedCount += child.updateInternal(visited, maxDepth, depth +  1, storeProcedure);
			}
		}
		storeProcedure.apply(this);
		return updatedCount;
	}

	public int delete(int maxDepth, Procedure<Cumulus4jListHolder> deleteProcedure) {
		// We use an IdentityHashMap here so hashCode is not called on deleted items.
		Map<Cumulus4jListHolder, Cumulus4jListHolder> visited = new IdentityHashMap<Cumulus4jListHolder, Cumulus4jListHolder>();
		return deleteInternal(visited, maxDepth, 0, deleteProcedure);
	}

	public int deleteInternal(Map<Cumulus4jListHolder, Cumulus4jListHolder> visited, int maxDepth, int depth, Procedure<Cumulus4jListHolder> deleteProcedure) {
		if(visited.containsKey(this)){
			return 0;
		}
		visited.put(this, this);
		int deletedCount = 1;
		if(_list != null){
			for (int i = 0; i < _list.size(); i++) {
				Cumulus4jListHolder child = getList().get(i);

				logger.debug("child: " + child);
				logger.debug("deletedCount: " + deletedCount);
				logger.debug("visited: " + visited);
				logger.debug("maxDepth: " + maxDepth);
				logger.debug("depth: " + depth);
				logger.debug("deletePrecedure: " + deleteProcedure);

				deletedCount += child.deleteInternal(visited, maxDepth, depth +  1, deleteProcedure);
			}
		}
		deleteProcedure.apply(this);
		return deletedCount;
	}

	private static Logger logger = LoggerFactory.getLogger(Cumulus4jListHolder.class);

	private void setId(long id) {
		_id = id;
	}


	public long getId() {
		return _id;
	}


	private void setList(List<Cumulus4jListHolder> list) {
		_list = list;
	}


	private List<Cumulus4jListHolder> getList() {
		return _list;
	}

	@Override
	public boolean equals(Object obj) {
		if(this == obj){
			return true;
		}
		if(obj == null){
			return false;
		}
		if(obj.getClass() != this.getClass()){
			return false;
		}
		Cumulus4jListHolder other = (Cumulus4jListHolder) obj;
		return _id == other._id;
	}

	@Override
	public int hashCode() {
		return (int)_id;
	}

	@Override
	public String toString() {
		return "Cumulus4jListHolder [_id=" + _id + "]";
	}

}
