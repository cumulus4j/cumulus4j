package org.polepos.teams.jdo.cumulus4j;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import javax.jdo.Extent;
import javax.jdo.PersistenceManager;

import org.polepos.framework.Car;
import org.polepos.framework.DriverBase;
import org.polepos.framework.Team;
import org.polepos.teams.jdo.cumulus4j.data.Cumulus4jIndexedObject;
import org.polepos.teams.jdo.cumulus4j.data.Cumulus4jInheritanceHierarchy0;
import org.polepos.teams.jdo.cumulus4j.data.Cumulus4jInheritanceHierarchy1;
import org.polepos.teams.jdo.cumulus4j.data.Cumulus4jInheritanceHierarchy2;
import org.polepos.teams.jdo.cumulus4j.data.Cumulus4jInheritanceHierarchy3;
import org.polepos.teams.jdo.cumulus4j.data.Cumulus4jInheritanceHierarchy4;
import org.polepos.teams.jdo.cumulus4j.data.Cumulus4jListHolder;

public class JdoCumulus4jTeam extends Team{

	private final Car[] mCars;

    public JdoCumulus4jTeam() {

        String[] impls = JdoCumulus4j.settings().getJdoImplementations();

        if(impls == null){
            System.out.println("No JDO (with JdoCumulus4j) engine configured.");
            mCars = new Car[0];
        }else{

            List <Car> cars = new ArrayList<Car>();

            for (String impl : impls) {

                String[] jdosqldbs = JdoCumulus4j.settings().getJdbc(impl);

                if(jdosqldbs != null && jdosqldbs.length > 0){
                    for(String sqldb : jdosqldbs){
                        try {
                            cars.add(new JdoCumulus4jCar(this, impl, sqldb, JdoCumulus4j.settings().color(impl)));
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                }else{
                    try {
                        cars.add(new JdoCumulus4jCar(this, impl, null, JdoCumulus4j.settings().color(impl)));
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }//				logger.debug("child: " + child);
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
        return new DriverBase[]{
        	new FlatObjectJdoCumulus4j(),
        	new NestedListsJdoCumulus4j(),
//        	new ComplexJdoCumulus4j(),
        	new InheritanceHierarchyJdoCumulus4j(),
        };
    }

    @Override
    public String website() {
        return "http://www.cumulus4j.org";
    }

	@Override
    public void setUp() {

//		try {
//			CleanupUtil.dropAllTables();
//		} catch (Exception e) {
//			throw new RuntimeException(e);
//		}

		for(int i = 0; i < mCars.length;i++){

		    JdoCumulus4jCar jdoCumulus4jCar = (JdoCumulus4jCar)mCars[i];
			PersistenceManager pm = jdoCumulus4jCar.getPersistenceManager();

		    deleteAll(pm, Cumulus4jIndexedObject.class);

		    deleteAll(pm, Cumulus4jListHolder.class);

//		    deleteAll(pm, Cumulus4jComplexRoot.class);
//		    deleteAll(pm, Cumulus4jComplexHolder4.class);
//		    deleteAll(pm, Cumulus4jComplexHolder3.class);
//		    deleteAll(pm, Cumulus4jComplexHolder2.class);
//		    deleteAll(pm, Cumulus4jComplexHolder1.class);
//		    deleteAll(pm, Cumulus4jComplexHolder0.class);

		    deleteAll(pm, Cumulus4jInheritanceHierarchy4.class);
		    deleteAll(pm, Cumulus4jInheritanceHierarchy3.class);
		    deleteAll(pm, Cumulus4jInheritanceHierarchy2.class);
		    deleteAll(pm, Cumulus4jInheritanceHierarchy1.class);
		    deleteAll(pm, Cumulus4jInheritanceHierarchy0.class);

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