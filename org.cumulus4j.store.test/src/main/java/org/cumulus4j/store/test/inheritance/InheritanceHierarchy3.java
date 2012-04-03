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
public class InheritanceHierarchy3 extends InheritanceHierarchy2
{
	private static AtomicInteger counter3 = new AtomicInteger();

	private int i3 = counter3.getAndIncrement();

//    public InheritanceHierarchy3(){
//    }
//
//    public InheritanceHierarchy3(int i0, int i1, int i2, int i3) {
//        super(i0, i1, i2);
//        this.i3 = i3;
//    }

    public void setI3(int i){
        i3 = i;
    }

    public int getI3(){
        return i3;
    }

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = super.hashCode();
		result = prime * result + i3;
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
		InheritanceHierarchy3 other = (InheritanceHierarchy3) obj;
		if (i3 != other.i3)
			return false;
		return true;
	}


}
