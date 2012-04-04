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

package org.polepos.teams.jdo;

import java.io.File;

import org.polepos.Settings;
import org.polepos.framework.RdbmsSettings;

/**
 * @author Herkules
 */
public class JdoSettings extends RdbmsSettings{

	private final static String KEY_JDO = "jdo";
    private final static String KEY_ENHANCE = "enhance";
    private final static String KEY_ENHANCER = "enhancer";
//    private final static String KEY_CONNECTURL = "javax.jdo.option.ConnectionURL";

	public JdoSettings(){
        super(Settings.JDO);
	}

    public String[] getJdoImplementations(){
        return getArray( KEY_JDO );
    }

    private JdoImplSettings[] jdoImplSettings;

    public JdoImplSettings[] getJdoImplSettings() {
    	if (jdoImplSettings == null) {
    		String[] jdoImplementations = getJdoImplementations();
    		JdoImplSettings[] settings = new JdoImplSettings[jdoImplementations.length];
    		for (int idx = 0; idx < jdoImplementations.length; idx++) {
				String jdoImpl = jdoImplementations[idx];
				settings[idx] = new JdoImplSettings(Settings.SETTINGS_FOLDER + File.separator + jdoImpl + ".properties");
			}
    		jdoImplSettings = settings;
    	}
    	return jdoImplSettings;
    }

//	public String getConnectUrl(){
//		return get( KEY_CONNECTURL );
//	}

    public boolean enhance(){
        return getBoolean(KEY_ENHANCE);
    }

    public String enhancer(){
        return get(KEY_ENHANCER);
    }


}
