/**
 * Copyright (C) by AX Business Solutions AG
 * (2010-2011)
 * All rights reserved
 */
package de.alexgauss.test.server;

/**
 * Note: No DBO-Object is necessary for this class
 */
public class TaxCodeItem {
    private TaxCode taxCode = new TaxCode();
    private Price totalTax = new Price("EUR", "0.00");

    public TaxCodeItem(TaxCode taxCode, Price totalTax) {
        this.taxCode = taxCode;
        this.totalTax = totalTax;
    }

    public TaxCode getTaxCode() {
        return taxCode;
    }

    public Price getTotalTax() {
        return totalTax;
    }
}
