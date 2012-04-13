package org.polepos.teams.jdo.cumulus4j;

import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import org.polepos.framework.Car;
import org.polepos.teams.jdo.Jdo;
import org.polepos.teams.jdo.JdoTeam;

public class JdoCumulus4jTeam extends JdoTeam {

	private Properties runtimeProperties;

    public JdoCumulus4jTeam(Properties runtimeProperties)
    {
    	this.runtimeProperties = runtimeProperties;

        String[] impls = Jdo.settings().getJdoImplementations();

        if(impls == null){
            System.out.println("No JDO (with JdoCumulus4j) engine configured.");
            mCars = new Car[0];
        }else{

            List <Car> cars = new ArrayList<Car>();

            for (String impl : impls) {

                String[] jdosqldbs = Jdo.settings().getJdbc(impl);

                if(jdosqldbs != null && jdosqldbs.length > 0){
                    for(String sqldb : jdosqldbs){
                        try {
                        	//TODO move color to cumulus4j properties file
                            cars.add(new JdoCumulus4jCar(this, impl, sqldb, "0x27518C"));
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                }else{
                    try {
                        cars.add(new JdoCumulus4jCar(this, impl, null, Jdo.settings().color(impl)));
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }

            mCars = new Car[ cars.size() ];
            cars.toArray(mCars);
        }

    }

    public Properties getRuntimeProperties() {
		return runtimeProperties;
	}

    @Override
	public String name(){
		return "JDO/Cumulus4j";
	}

    @Override
    public String description() {
        return "the JDO/Cumulus4j team";
    }


    @Override
    public String website() {
        return "http://www.cumulus4j.org";
    }
}