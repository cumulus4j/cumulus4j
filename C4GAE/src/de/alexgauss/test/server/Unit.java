/**
 * Copyright (C) by AX Business Solutions AG
 * (2010-2011)
 * All rights reserved
 */
package de.alexgauss.test.server;

import java.io.Serializable;

public class Unit implements Serializable {

    private static final long serialVersionUID = -8215729792950976378L;

    private String unitId;

    private String unitText;

    public Unit() {
    }

    public String getUnitId() {
        return unitId;
    }

    public void setUnitId(String unitId) {
        this.unitId = unitId;
    }

    public String getUnitText() {
        return unitText;
    }

    public void setUnitText(String unitText) {
        this.unitText = unitText;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((unitId == null) ? 0 : unitId.hashCode());
        result = prime * result + ((unitText == null) ? 0 : unitText.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        Unit other = (Unit) obj;
        if (unitId == null) {
            if (other.unitId != null)
                return false;
        } else if (!unitId.equals(other.unitId))
            return false;
        if (unitText == null) {
            if (other.unitText != null)
                return false;
        } else if (!unitText.equals(other.unitText))
            return false;
        return true;
    }
}
