package org.polepos.teams.jdo.cumulus4j;

import org.polepos.Settings;
import org.polepos.framework.RdbmsSettings;

public class JdoCumulus4jSettings extends RdbmsSettings{
	private final static String KEY_JDO = "jdo";
    private final static String KEY_ENHANCE = "enhance";
    private final static String KEY_ENHANCER = "enhancer";
    private final static String KEY_CONNECTURL = "javax.jdo.option.ConnectionURL";

	public JdoCumulus4jSettings(){
        super(Settings.JDO);
	}

    public String[] getJdoImplementations(){
        return getArray( KEY_JDO );
    }

	public String getConnectUrl(){
		return get( KEY_CONNECTURL );
	}

    public boolean enhance(){
        return getBoolean(KEY_ENHANCE);
    }

    public String enhancer(){
        return get(KEY_ENHANCER);
    }


}
