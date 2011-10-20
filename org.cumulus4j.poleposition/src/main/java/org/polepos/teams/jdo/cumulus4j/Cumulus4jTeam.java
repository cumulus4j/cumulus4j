package org.polepos.teams.jdo.cumulus4j;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import javax.jdo.Extent;
import javax.jdo.PersistenceManager;

import org.polepos.framework.Car;
import org.polepos.framework.DriverBase;
import org.polepos.framework.Team;
import org.polepos.teams.jdo.cumulus4j.data.InheritanceHierarchy0;
import org.polepos.teams.jdo.cumulus4j.data.InheritanceHierarchy1;
import org.polepos.teams.jdo.cumulus4j.data.InheritanceHierarchy2;
import org.polepos.teams.jdo.cumulus4j.data.InheritanceHierarchy3;
import org.polepos.teams.jdo.cumulus4j.data.InheritanceHierarchy4;

public class Cumulus4jTeam extends Team{

	private final Car[] mCars;

    public Cumulus4jTeam() {

        String[] impls = Cumulus4j.settings().getJdoImplementations();

        if(impls == null){
            System.out.println("No JDO (with Cumulus4j) engine configured.");
            mCars = new Car[0];
        }else{

            List <Car> cars = new ArrayList<Car>();

            for (String impl : impls) {

                String[] jdosqldbs = Cumulus4j.settings().getJdbc(impl);

                if(jdosqldbs != null && jdosqldbs.length > 0){
                    for(String sqldb : jdosqldbs){
                        try {
                            cars.add(new Cumulus4jCar(this, impl, sqldb, Cumulus4j.settings().color(impl)));
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                }else{
                    try {
                        cars.add(new Cumulus4jCar(this, impl, null, Cumulus4j.settings().color(impl)));
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
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
//        	new FlatObjectCumulus4j(),
//        	new NestedListsCumulus4j(),
//        	new ComplexCumulus4j(),
        	new InheritanceHierarchyCumulus4j(),
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

		    Cumulus4jCar cumulus4jCar = (Cumulus4jCar)mCars[i];
			PersistenceManager pm = cumulus4jCar.getPersistenceManager();

//		    deleteAll(pm, Cumulus4jIndexedObject.class);

//		    deleteAll(pm, ListHolder.class);
//
//		    deleteAll(pm, ComplexRoot.class);
//		    deleteAll(pm, ComplexHolder0.class);
//		    deleteAll(pm, ComplexHolder1.class);
//		    deleteAll(pm, ComplexHolder2.class);
//		    deleteAll(pm, ComplexHolder3.class);
//		    deleteAll(pm, ComplexHolder4.class);
//
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