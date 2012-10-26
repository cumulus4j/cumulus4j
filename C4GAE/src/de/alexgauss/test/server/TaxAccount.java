/**
 * Copyright (C) by AX Business Solutions AG
 * (2010-2011)
 * All rights reserved
 */
package de.alexgauss.test.server;

import java.io.Serializable;

public class TaxAccount implements Serializable {
    /**
	 * 
	 */
    private static final long serialVersionUID = 123259861831031079L;

    private String salesTaxID; // german: USt.-ID
    private String taxNumber; // german: Steuernummer

    public String getSalesTaxID() {
        return salesTaxID;
    }

    public String getTaxNumber() {
        return taxNumber;
    }

    public void setSalesTaxID(String salesTaxID) {
        this.salesTaxID = salesTaxID;
    }

    public void setTaxNumber(String taxNumber) {
        this.taxNumber = taxNumber;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        TaxAccount other = (TaxAccount) obj;
        if (salesTaxID == null) {
            if (other.salesTaxID != null)
                return false;
        } else if (!salesTaxID.equals(other.salesTaxID))
            return false;
        if (taxNumber == null) {
            if (other.taxNumber != null)
                return false;
        } else if (!taxNumber.equals(other.taxNumber))
            return false;
        return true;
    }
}
