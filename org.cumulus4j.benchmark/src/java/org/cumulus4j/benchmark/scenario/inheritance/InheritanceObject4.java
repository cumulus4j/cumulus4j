package org.cumulus4j.benchmark.scenario.inheritance;

import javax.jdo.annotations.IdentityType;
import javax.jdo.annotations.PersistenceCapable;

/**
 * @author Jan Mortensen - jmortensen at nightlabs dot de
 */
@PersistenceCapable(identityType=IdentityType.APPLICATION, detachable="true")
public class InheritanceObject4 extends InheritanceObject3{

	private int i4;

	public InheritanceObject4(int i0, int i1, int i2, int i3, int i4) {
		super(i0, i1, i2, i3);
		this.setI4(i4);
	}

	public void setI4(int i4) {
		this.i4 = i4;
	}

	public int getI4() {
		return i4;
	}
}
