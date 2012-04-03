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

import javax.jdo.annotations.IdGeneratorStrategy;
import javax.jdo.annotations.IdentityType;
import javax.jdo.annotations.PersistenceCapable;
import javax.jdo.annotations.Persistent;
import javax.jdo.annotations.PrimaryKey;

@PersistenceCapable(identityType=IdentityType.APPLICATION, detachable="true")
public class InheritanceHierarchy1 extends InheritanceHierarchy0{

	@PrimaryKey
	@Persistent(valueStrategy=IdGeneratorStrategy.NATIVE)
    private int i1;

    public InheritanceHierarchy1(){
    }

    public InheritanceHierarchy1(int i0, int i1) {
        super(i0);
        this.i1 = i1;
    }

    public void setI1(int i){
        i1 = i;
    }

    public int getI1(){
        return i1;
    }

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = super.hashCode();
		result = prime * result + i1;
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
		InheritanceHierarchy1 other = (InheritanceHierarchy1) obj;
		if (i1 != other.i1)
			return false;
		return true;
	}

}
