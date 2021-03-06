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

import java.io.File;


public class Settings {

    public static boolean DEBUG = false;

    public static final String SETTINGS_FOLDER = "settings";

    public static final String CIRCUIT = DEBUG ? SETTINGS_FOLDER + "/DebugCircuits.properties" : SETTINGS_FOLDER + File.separator + "Circuits.properties" ;

    public static final String JDBC = SETTINGS_FOLDER + File.separator + "Jdbc.properties";

    public static final String JDO = SETTINGS_FOLDER + File.separator + "Jdo.properties";

    public static final String CUMULUS4J = SETTINGS_FOLDER + File.separator + "cumulus4j.properties";

    static{

        String className = Settings.class.getName() ;

        if(DEBUG){
            System.out.println(className + ".DEBUG is set to true.\n");
        }

    }

}
