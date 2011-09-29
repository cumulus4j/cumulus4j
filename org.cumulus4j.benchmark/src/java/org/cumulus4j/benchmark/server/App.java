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
package org.cumulus4j.benchmark.server;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import javax.ws.rs.ApplicationPath;
import javax.ws.rs.core.Application;

import org.cumulus4j.benchmark.scenario.inheritance.InheritanceScenarioService;
import org.cumulus4j.benchmark.scenario.relation.RelationScenarioService;
import org.cumulus4j.benchmark.scenario.simpledatatype.PersonAllQueryableScenarioService;
import org.cumulus4j.benchmark.scenario.simpledatatype.PersonHalfQueryableScenarioService;

/**
 * @author Jan Mortensen - jmortensen at nightlabs dot de
 */
@ApplicationPath("/")
public class App
extends Application
{
	private static final Class<?>[] serviceClassesArray = {
		PersonAllQueryableScenarioService.class,
		PersonHalfQueryableScenarioService.class,
		InheritanceScenarioService.class,
		RelationScenarioService.class,
	};

	private static final Set<Class<?>> serviceClassesSet;
	static {
		Set<Class<?>> s = new HashSet<Class<?>>(serviceClassesArray.length);
		for (Class<?> c : serviceClassesArray)
			s.add(c);

		serviceClassesSet = Collections.unmodifiableSet(s);
	}

	@Override
	public Set<Class<?>> getClasses() {
		return serviceClassesSet;
	}

	private Set<Object> singletons;

	@Override
	public Set<Object> getSingletons()
	{
		if (singletons == null) {
			Set<Object> s = new HashSet<Object>();
			singletons = Collections.unmodifiableSet(s);
		}

		return singletons;
	}
}
