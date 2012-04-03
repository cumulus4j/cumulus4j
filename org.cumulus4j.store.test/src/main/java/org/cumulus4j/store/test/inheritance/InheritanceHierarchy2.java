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

package org.cumulus4j.store.test.inheritance;

import java.util.concurrent.atomic.AtomicInteger;

import javax.jdo.annotations.IdentityType;
import javax.jdo.annotations.PersistenceCapable;

@PersistenceCapable(identityType=IdentityType.APPLICATION, detachable="true")
public class InheritanceHierarchy2 extends InheritanceHierarchy1{

	private static AtomicInteger counter2 = new AtomicInteger();

	private int i2 = counter2.getAndIncrement();

//    public InheritanceHierarchy2(){
//    }
//
//    public InheritanceHierarchy2(int i0, int i1, int i2) {
//        super(i0, i1);
//        this.i2 = i2;
//    }

    public void setI2(int i){
        i2 = i;
    }

    public int getI2(){
        return i2;
    }

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = super.hashCode();
		result = prime * result + i2;
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (!super.equals(obj))
			return false;
		if (getClass() != obj.getClass())
			return false;
		InheritanceHierarchy2 other = (InheritanceHierarchy2) obj;
		if (i2 != other.i2)
			return false;
		return true;
	}
}
