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


import java.util.ArrayList;
import java.util.List;

import org.polepos.circuits.bahrain.Bahrain;
import org.polepos.circuits.barcelona.Barcelona;
import org.polepos.circuits.imola.Imola;
import org.polepos.circuits.melbourne.Melbourne;
import org.polepos.circuits.monaco.Monaco;
import org.polepos.circuits.montreal.Montreal;
import org.polepos.circuits.nurburgring.Nurburgring;
import org.polepos.circuits.sepang.Sepang;
import org.polepos.framework.CircuitBase;
import org.polepos.framework.DriverBase;
import org.polepos.framework.Team;
import org.polepos.runner.db4o.AbstractDb4oVersionsRaceRunner;
import org.polepos.teams.db4o.BahrainDb4o;
import org.polepos.teams.db4o.BarcelonaDb4o;
import org.polepos.teams.db4o.Db4oOptions;
import org.polepos.teams.db4o.Db4oTeam;
import org.polepos.teams.db4o.ImolaDb4o;
import org.polepos.teams.db4o.MelbourneDb4o;
import org.polepos.teams.db4o.MonacoDb4o;
import org.polepos.teams.db4o.MontrealDb4o;
import org.polepos.teams.db4o.NurburgringDb4o;
import org.polepos.teams.db4o.SepangDb4o;

/**
 * Please read the README file in the home directory first.
 *
 */
public class Db4oVersionRace extends AbstractDb4oVersionsRaceRunner{

    public static void main(String[] arguments) {
        new Db4oVersionRace().run();
    }

    @Override
	public Team[] teams() {
        List<Team> teamList = new ArrayList<Team>();

        int[] clientServerOptions = new int[] { Db4oOptions.CLIENT_SERVER,
                Db4oOptions.CLIENT_SERVER_TCP };

        teamList.add(new Db4oTeam());
        teamList.add(db4oTeam(clientServerOptions));

//        String db4oCurrentVersion = System.getProperty("polepos.db4o.current");
//        if (db4oCurrentVersion != null) {
//            teamList.add(db4oTeam(db4oCurrentVersion));
//            teamList.add(db4oTeam(db4oCurrentVersion, options));
//        }
//
//        teamList.add(db4oTeam(Db4oVersions.JAR78));
//        teamList.add(db4oTeam(Db4oVersions.JAR74));
//        teamList.add(db4oTeam(Db4oVersions.JAR64));
//

        Team[] teams = new Team[teamList.size()];

        teamList.toArray(teams);
        return teams;
    }

	@Override
	public CircuitBase[] circuits() {
		return new CircuitBase[] {
				new Melbourne(),
				new Sepang(),
				new Bahrain(),
				new Imola(),
				new Barcelona(),
				new Monaco(),
				new Montreal(),
				new Nurburgring(),
		};
	}

	@Override
	public DriverBase[] drivers() {
		return new DriverBase [] {
				new MelbourneDb4o(),
		        new SepangDb4o(),
		        new BahrainDb4o(),
		        new ImolaDb4o(),
		        new BarcelonaDb4o(),
		        new MonacoDb4o(),
		        new NurburgringDb4o(),
		        new MontrealDb4o(),
		};
	}

}
