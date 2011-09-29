package org.cumulus4j.benchmark.scenario.inheritance;

import javax.jdo.annotations.IdentityType;
import javax.jdo.annotations.PersistenceCapable;

/**
 * @author Jan Mortensen - jmortensen at nightlabs dot de
 */
@PersistenceCapable(identityType=IdentityType.APPLICATION, detachable="true")
public abstract class InheritanceObject1 extends InheritanceObject0{

	private int i1;

	public InheritanceObject1(int i0, int i1) {
		super(i0);
		this.setI1(i1);
	}

	public void setI1(int i1) {
		this.i1 = i1;
	}

	public int getI1() {
		return i1;
	}

}
