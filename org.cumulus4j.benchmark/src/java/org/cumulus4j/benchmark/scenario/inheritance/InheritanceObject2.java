package org.cumulus4j.benchmark.scenario.inheritance;

import javax.jdo.annotations.IdentityType;
import javax.jdo.annotations.PersistenceCapable;

/**
 * @author Jan Mortensen - jmortensen at nightlabs dot de
 */
@PersistenceCapable(identityType=IdentityType.APPLICATION, detachable="true")
public abstract class InheritanceObject2 extends InheritanceObject1{

	private int i2;

	public InheritanceObject2(int i0, int i1, int i2) {
		super(i0, i1);
		this.setI2(i2);
	}

	public void setI2(int i2) {
		this.i2 = i2;
	}

	public int getI2() {
		return i2;
	}

}
