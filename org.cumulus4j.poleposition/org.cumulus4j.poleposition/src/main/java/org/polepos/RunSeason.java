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

package org.polepos;

import java.util.Properties;

import org.polepos.circuits.complex.Complex;
import org.polepos.circuits.flatobject.FlatObject;
import org.polepos.circuits.inheritancehierarchy.InheritanceHierarchy;
import org.polepos.circuits.nestedlists.NestedLists;
import org.polepos.framework.CircuitBase;
import org.polepos.framework.ReflectiveCircuitBase;
import org.polepos.framework.Team;
import org.polepos.reporters.Cumulus4jReporterFactory;
import org.polepos.reporters.Reporter;
import org.polepos.runner.AbstractRunner;
import org.polepos.teams.jdo.JdoTeam;
import org.polepos.teams.jdo.cumulus4j.JdoCumulus4jTeam;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This is the Main class to run PolePosition. If JDO, JPA and JVI are
 * to be tested also, persistent classes have to be enhanced first.
 *
 * For your convenience you can try {@link RunSeasonAfterEnhancing#main(String[])}
 * or you can use the Ant script to do all in one go.
 *
 *
 */
public class RunSeason extends AbstractRunner {

	private static Logger logger = LoggerFactory.getLogger(RunSeason.class);

	public static void main(String[] args) {

		new RunSeason().run();
	}

	private Properties runtimeProperties = new Properties();

	public Properties getRuntimeProperties() {
		return runtimeProperties;
	}

	@Override
	public CircuitBase[] circuits() {
		return new CircuitBase[] {

				new ReflectiveCircuitBase(Complex.class),
				new ReflectiveCircuitBase(NestedLists.class),
				new ReflectiveCircuitBase(InheritanceHierarchy.class),
				new ReflectiveCircuitBase(FlatObject.class),

// Old Circuits
// Most usecases are covered by the 4 new circuits

//				new Melbourne(),
//				new Sepang(),
//				new Bahrain(),
//				new Imola(),
//				new Barcelona(),
//				new Monaco(),
//				new Montreal(),
		};
	}



	@Override
	public Team[] teams() {
		return new Team[] {

//				new Db4oTeam(),

//				new Db4oClientServerTeam(),

// 				new JdbcTeam(),
//				new HibernateTeam(),
//				new JpaTeam(),

//				 new JviTeam(),
//				 new CobraTeam(),

				new JdoCumulus4jTeam(runtimeProperties),
				new JdoTeam()
		};
	}

	@Override
	protected Reporter[] reporters() {

		return Cumulus4jReporterFactory.getCumulus4jReporters();
//		return DefaultReporterFactory.defaultReporters();
	}

}
