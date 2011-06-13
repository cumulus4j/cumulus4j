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

package org.cumulus4j.store.test.metadata;

import javax.jdo.annotations.Extension;
import javax.jdo.annotations.PersistenceCapable;

import org.cumulus4j.annotation.NotQueryable;

@PersistenceCapable
public class Entity1
{
	protected Entity1() { }

	public Entity1(String name, String field1, String field2, String field3) {
		setName(name);
		setField1(field1);
		setField2(field2);
		setField3(field3);
	}

	private String name;

	private String field1;

	@NotQueryable
	private String field2;

	@Extension(vendorName="datanucleus", key="cumulus4j-queryable", value="false")
	private String field3;

	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}

	public String getField1() {
		return field1;
	}
	public void setField1(String field1) {
		this.field1 = field1;
	}
	public String getField2() {
		return field2;
	}
	public void setField2(String field2) {
		this.field2 = field2;
	}
	public String getField3() {
		return field3;
	}
	public void setField3(String field3) {
		this.field3 = field3;
	}

	@Override
	public String toString() {
		return getClass().getSimpleName() + "[name=\"" + name + "\"]";
	}
}
