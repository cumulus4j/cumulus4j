package org.polepos.teams.jdo.cumulus4j;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import javax.jdo.Extent;
import javax.jdo.PersistenceManager;

import org.polepos.framework.Car;
import org.polepos.framework.DriverBase;
import org.polepos.framework.Team;
import org.polepos.teams.jdo.ComplexJdo;
import org.polepos.teams.jdo.FlatObjectJdo;
import org.polepos.teams.jdo.InheritanceHierarchyJdo;
import org.polepos.teams.jdo.Jdo;
import org.polepos.teams.jdo.NestedListsJdo;
import org.polepos.teams.jdo.data.ComplexHolder0;
import org.polepos.teams.jdo.data.ComplexHolder1;
import org.polepos.teams.jdo.data.ComplexHolder2;
import org.polepos.teams.jdo.data.ComplexHolder3;
import org.polepos.teams.jdo.data.ComplexHolder4;
import org.polepos.teams.jdo.data.ComplexRoot;
import org.polepos.teams.jdo.data.InheritanceHierarchy0;
import org.polepos.teams.jdo.data.InheritanceHierarchy1;
import org.polepos.teams.jdo.data.InheritanceHierarchy2;
import org.polepos.teams.jdo.data.InheritanceHierarchy3;
import org.polepos.teams.jdo.data.InheritanceHierarchy4;
import org.polepos.teams.jdo.data.JdoIndexedObject;
import org.polepos.teams.jdo.data.ListHolder;

public class JdoCumulus4jTeam extends Team {

	private final Car[] mCars;

    public JdoCumulus4jTeam() {

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
                            cars.add(new JdoCumulus4jCar(this, impl, sqldb, Jdo.settings().color(impl)));
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
//				logger.debug("child: " + child);
//				logger.debug("deletedCount: " + deletedCount);
//				logger.debug("visited: " + visited);
//				logger.debug("maxDepth: " + maxDepth);
//				logger.debug("depth: " + depth);
//				logger.debug("deletePrecedure: " + deleteProcedure);
            }

            mCars = new Car[ cars.size() ];
            cars.toArray(mCars);
        }

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
	public Car[] cars(){
		return mCars;
	}

    @Override
	public String databaseFile() {
    	// not supported yet
    	return null;
    }

    @Override
    public DriverBase[] drivers() {
        return new DriverBase[] {
        	new FlatObjectJdo(),
        	new NestedListsJdo(),
        	new InheritanceHierarchyJdo(),
        	new ComplexJdo()
        };
    }

    @Override
    public String website() {
        return "http://www.cumulus4j.org";
    }

	@Override
    public void setUp() {

		for(int i = 0; i < mCars.length;i++){

		    JdoCumulus4jCar jdoCumulus4jCar = (JdoCumulus4jCar)mCars[i];
			PersistenceManager pm = jdoCumulus4jCar.getPersistenceManager();

		    deleteAll(pm, JdoIndexedObject.class);

		    deleteAll(pm, ListHolder.class);

		    deleteAll(pm, ComplexRoot.class);
		    deleteAll(pm, ComplexHolder4.class);
		    deleteAll(pm, ComplexHolder3.class);
		    deleteAll(pm, ComplexHolder2.class);
		    deleteAll(pm, ComplexHolder1.class);
		    deleteAll(pm, ComplexHolder0.class);

		    deleteAll(pm, InheritanceHierarchy4.class);
		    deleteAll(pm, InheritanceHierarchy3.class);
		    deleteAll(pm, InheritanceHierarchy2.class);
		    deleteAll(pm, InheritanceHierarchy1.class);
		    deleteAll(pm, InheritanceHierarchy0.class);

		    pm.close();
		}
	}

	private void deleteAll(PersistenceManager pm, Class clazz) {

		deleteAllBatched(pm, clazz);
	}

	private void deleteAllBatched(PersistenceManager pm, Class clazz) {

		pm.currentTransaction().begin();
		int batchSize = 10000;
        int commitctr = 0;
        Extent extent = pm.getExtent(clazz, false);
        Iterator it = extent.iterator();
        while(it.hasNext()){
            pm.deletePersistent(it.next());
            if ( batchSize > 0  &&  ++commitctr >= batchSize){
                commitctr = 0;
                pm.currentTransaction().commit();
                pm.currentTransaction().begin();
            }
        }
        extent.closeAll();
        pm.currentTransaction().commit();
	}
}