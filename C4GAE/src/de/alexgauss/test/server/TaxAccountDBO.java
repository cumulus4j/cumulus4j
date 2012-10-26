/**
 * Copyright (C) by AX Business Solutions AG
 * (2010-2011)
 * All rights reserved
 */
package de.alexgauss.test.server;

import javax.jdo.annotations.PersistenceCapable;
import javax.jdo.annotations.Persistent;

@PersistenceCapable
public class TaxAccountDBO {

    @Persistent
    private String salesTaxID; // german: USt.-ID

    @Persistent
    private String taxNumber; // german: Steuernummer

    public String getSalesTaxID() {
        return salesTaxID;
    }

    public void setSalesTaxID(String salesTaxID) {
        this.salesTaxID = salesTaxID;
    }

    public String getTaxNumber() {
        return taxNumber;
    }

    public void setTaxNumber(String taxNumber) {
        this.taxNumber = taxNumber;
    }

}
