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
package org.cumulus4j.store.test.collection.join;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import javax.jdo.annotations.Join;
import javax.jdo.annotations.PersistenceCapable;

@PersistenceCapable
public class ElementASetOwner
{
	private String name;

	@Join
	private Set<ElementA> set = new HashSet<ElementA>();

	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public Set<ElementA> getSet() {
		return set == null ? null : Collections.unmodifiableSet(set);
	}
	public void addElementA(ElementA elementA)
	{
		set.add(elementA);
	}
	@Override
	public String toString() {
		return getClass().getSimpleName() + "[name=\"" + name + "\"]";
	}
}
