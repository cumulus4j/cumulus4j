package org.polepos.teams.jdo.cumulus4j;

import java.util.Collection;
import java.util.Iterator;

import javax.jdo.Extent;
import javax.jdo.JDOFatalInternalException;
import javax.jdo.PersistenceManager;
import javax.jdo.Query;
import javax.jdo.Transaction;

import org.polepos.framework.Car;
import org.polepos.framework.CarMotorFailureException;
import org.polepos.framework.CheckSummable;
import org.polepos.framework.DriverBase;
import org.polepos.framework.TurnSetup;

public class Cumulus4jDriver extends DriverBase{

	private transient PersistenceManager mPersistenceManager;

	@Override
	public void takeSeatIn( Car car, TurnSetup setup) throws CarMotorFailureException{
        super.takeSeatIn(car, setup);
	}

	@Override
	public void prepare(){
		mPersistenceManager = jdoCar().getPersistenceManager();
	}

	@Override
	public void backToPit(){
        Transaction tx = db().currentTransaction();
        if(tx.isActive()){
            tx.rollback();
        }
		mPersistenceManager.close();
		mPersistenceManager = null;
	}

	protected Cumulus4jCar jdoCar(){
		return (Cumulus4jCar)car();
	}

	protected PersistenceManager db(){
		return mPersistenceManager;
	}

	public void beginRead(){
		Transaction currentTransaction = db().currentTransaction();
		if(! currentTransaction.isActive()){
			currentTransaction.begin();
		}
	}

    public void begin(){
        db().currentTransaction().begin();
    }

    public void commit(){
        db().currentTransaction().commit();
    }

    public void store(Object obj){
        db().makePersistent(obj);
    }

    protected void doQuery( Query q, Object param){
    	beginRead();
        Collection result = (Collection)q.execute(param);
        Iterator it = result.iterator();
        while(it.hasNext()){
            Object o = it.next();
            if(o instanceof CheckSummable){
            	try{
            		addToCheckSum(((CheckSummable)o).checkSum());
            	} catch(JDOFatalInternalException e){
            		Throwable[] nestedExceptions = e.getNestedExceptions();
            		if(nestedExceptions != null){
            			for (int i = 0; i < nestedExceptions.length; i++) {
            				nestedExceptions[i].printStackTrace();
						}
            		}

            	}
            }
        }
    }

    protected void readExtent(Class clazz){
    	beginRead();
        Extent extent = db().getExtent( clazz, false );
        int count = 0;
        Iterator itr = extent.iterator();
        while (itr.hasNext()){
            Object o = itr.next();
            count++;
            if(o instanceof CheckSummable){
                addToCheckSum(((CheckSummable)o).checkSum());
            }
        }
        extent.closeAll();
    }

	protected void delete(Object obj) {
		db().deletePersistent(obj);
	}

}
