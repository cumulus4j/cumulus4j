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
public class Element3
{
	protected Element3() { }

	public Element3(String value, String name) {
		this.value = value;
		setName(name);
	}

	private String value;

	private String name;

	private Element3MapOwner owner;

	public String getValue() {
		return value;
	}

	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}

	public Element3MapOwner getOwner() {
		return owner;
	}
	protected void setOwner(Element3MapOwner owner) {
		this.owner = owner;
	}

	@Override
	public String toString() {
		return getClass().getSimpleName() + "[value=\"" + value + "\", name=\"" + name + "\"]";
	}
}
