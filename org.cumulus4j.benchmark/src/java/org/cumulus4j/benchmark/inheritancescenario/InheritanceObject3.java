package org.cumulus4j.benchmark.inheritancescenario;

import javax.jdo.annotations.IdentityType;
import javax.jdo.annotations.PersistenceCapable;

/**
 * @author Jan Mortensen - jmortensen at nightlabs dot de
 */
@PersistenceCapable(identityType=IdentityType.APPLICATION, detachable="true")
public abstract class InheritanceObject3 extends InheritanceObject2{

	private int i3;

	public InheritanceObject3(int i0, int i1, int i2, int i3) {
		super(i0, i1, i2);
		this.setI3(i3);
	}

	public void setI3(int i3) {
		this.i3 = i3;
	}

	public int getI3() {
		return i3;
	}

}
