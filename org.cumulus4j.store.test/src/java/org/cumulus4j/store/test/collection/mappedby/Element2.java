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

package org.cumulus4j.store.test.collection.mappedby;

import javax.jdo.annotations.PersistenceCapable;

@PersistenceCapable
public class Element2
{
	protected Element2() { }

	public Element2(String key, String name) {
		this.key = key;
		setName(name);
	}

	private String key;

	private String name;

	private Element2MapOwner owner;

	public String getKey() {
		return key;
	}

	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}

	public Element2MapOwner getOwner() {
		return owner;
	}
	protected void setOwner(Element2MapOwner owner) {
		this.owner = owner;
	}

	@Override
	public String toString() {
		return getClass().getSimpleName() + "[key=\"" + key + "\", name=\"" + name + "\"]";
	}
}
