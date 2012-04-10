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

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import javax.jdo.Extent;
import javax.jdo.PersistenceManager;

import org.cumulus4j.store.test.framework.CleanupUtil;
import org.polepos.framework.Car;
import org.polepos.framework.DriverBase;
import org.polepos.framework.Team;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public class JdoTeam extends Team
{
	private static final Logger logger = LoggerFactory.getLogger(JdoTeam.class);

	protected Car[] mCars;

    public JdoTeam() {

        String[] impls = Jdo.settings().getJdoImplementations();

        if(impls == null){
            System.out.println("No JDO engine configured.");
            mCars = new Car[0];
        }else{

            List <Car> cars = new ArrayList<Car>();

            for (String impl : impls) {

                String[] jdosqldbs = Jdo.settings().getJdbc(impl);

                if(jdosqldbs != null && jdosqldbs.length > 0){
                    for(String sqldb : jdosqldbs){
                        try {
                            cars.add(new JdoCar(this, impl, sqldb, Jdo.settings().color(impl)));
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                }else{
                    try {
                        cars.add(new JdoCar(this, impl, null, Jdo.settings().color(impl)));
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
		return "JDO";
	}

    @Override
    public String description() {
        return "the JDO team";
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
//        	new InheritanceHierarchyJdo(),
//        	new ComplexJdo()
//            new MelbourneJdo(),
//            new SepangJdo(),
//            new BahrainJdo(),
//            new ImolaJdo(),
//            new BarcelonaJdo(),
//            new MonacoJdo(),
//            new MontrealJdo(),
//            new NurburgringJdo()
        };
    }

    @Override
    public String website() {
        return null;
    }

//    private boolean firstSetUp = true;

	@Override
    public void setUp() {
		logger.info("setUp: Entered.");

		for(int i = 0; i < mCars.length;i++) {
			JdoCar jdoCar = (JdoCar)mCars[i];
			jdoCar.closePersistenceManagerFactory(); // it might still be open from a previous turn.
			try {
				CleanupUtil.dropAllTables(jdoCar.getPersistenceEngineProperties());
			} catch (Exception e) {
				throw new RuntimeException(e);
			}
		}

//		if (firstSetUp) {
//			firstSetUp = false;
//
//			for(int i = 0; i < mCars.length;i++) {
//				JdoCar jdoCar = (JdoCar)mCars[i];
//				try {
//					CleanupUtil.dropAllTables(jdoCar.getPersistenceEngineProperties());
//				} catch (Exception e) {
//					throw new RuntimeException(e);
//				}
//			}
//		}
//		else {
//			for(int i = 0; i < mCars.length;i++) {
//				JdoCar jdoCar = (JdoCar)mCars[i];
//				PersistenceManager pm = jdoCar.getPersistenceManager();
//
//				deleteAll(pm, JdoIndexedObject.class);
//				deleteAll(pm, ListHolder.class);
//				deleteAll(pm, ComplexRoot.class);
//				deleteAll(pm, ComplexHolder4.class);
//				deleteAll(pm, ComplexHolder3.class);
//				deleteAll(pm, ComplexHolder2.class);
//				deleteAll(pm, ComplexHolder1.class);
//				deleteAll(pm, ComplexHolder0.class);
//
//				deleteAll(pm, InheritanceHierarchy4.class);
//				deleteAll(pm, InheritanceHierarchy3.class);
//				deleteAll(pm, InheritanceHierarchy2.class);
//				deleteAll(pm, InheritanceHierarchy1.class);
//				deleteAll(pm, InheritanceHierarchy0.class);
//
////				deleteAll(pm, JB0.class);
////				deleteAll(pm, JB1.class);
////				deleteAll(pm, JB2.class);
////				deleteAll(pm, JB3.class);
////				deleteAll(pm, JB4.class);
////
////				deleteAll(pm, JdoIndexedPilot.class);
////				deleteAll(pm, JdoPilot.class);
////				deleteAll(pm, JdoTree.class);
////				deleteAll(pm, JdoLightObject.class);
////				deleteAll(pm, JdoListHolder.class);
////				deleteAll(pm, JN1.class);
//
//				pm.close();
//			}
//		}
	}


	private void deleteAll(PersistenceManager pm, Class<?> clazz) {

		// Added after getting OutOfMemory issues with
		// 3 million objects per extent.
		if(true){
			deleteAllBatched(pm, clazz);
			return;
		}


//		// This didn't work in Datanucleus ....
//
//		pm.currentTransaction().begin();
//		pm.newQuery(clazz).deletePersistentAll();
//		pm.currentTransaction().commit();
//
//		// ...so delete all again like this...
//
//		pm.currentTransaction().begin();
//		pm.deletePersistentAll((Collection<?>) pm.newQuery(clazz).execute());
//		pm.currentTransaction().commit();
	}


	private void deleteAllBatched(PersistenceManager pm, Class<?> clazz)
	{
		pm.currentTransaction().begin();
		int batchSize = 10000;
        int deletedNonCommittedCounter = 0;
        Extent<?> extent = pm.getExtent(clazz, false);
        Iterator<?> it = extent.iterator();
        while(it.hasNext()){
            pm.deletePersistent(it.next());
            if ( batchSize > 0  &&  ++deletedNonCommittedCounter >= batchSize){
                deletedNonCommittedCounter = 0;
                pm.currentTransaction().commit();
                pm.currentTransaction().begin();
            }
        }
        extent.closeAll();
        pm.currentTransaction().commit();
	}
}
