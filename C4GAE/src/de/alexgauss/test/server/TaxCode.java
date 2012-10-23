/**
 * Copyright (C) by AX Business Solutions AG
 * (2010-2011)
 * All rights reserved
 */
package de.alexgauss.test.server;

import java.io.Serializable;
import java.math.BigDecimal;

public class TaxCode implements Serializable {

    private static final long serialVersionUID = 8271882002242863370L;

    private String taxCodeId;

    /** e.g. 0.19 */
    private BigDecimal tax;

    private String taxString;

    private String taxType;

    public String getTaxType() {
        return taxType;
    }

    public void setTaxType(String taxType) {
        this.taxType = taxType;
    }

    public String getTaxString() {
        return taxString;
    }

    public String getTaxCodeId() {
        return taxCodeId;
    }

    public BigDecimal getTax() {
        return tax;
    }

    public void setTaxString(String taxString) {
        this.taxString = taxString;
    }

    public void setTaxCodeId(String taxCodeId) {
        this.taxCodeId = taxCodeId;
    }

    public void setTax(BigDecimal tax) {
        this.tax = tax;
    }

    public void setTax(String tax) {
        this.tax = new BigDecimal(tax);
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((tax == null) ? 0 : tax.hashCode());
        result = prime * result + ((taxCodeId == null) ? 0 : taxCodeId.hashCode());
        result = prime * result + ((taxString == null) ? 0 : taxString.hashCode());
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
        TaxCode other = (TaxCode) obj;
        if (tax == null) {
            if (other.tax != null)
                return false;
        } else if (!tax.equals(other.tax))
            return false;
        if (taxCodeId == null) {
            if (other.taxCodeId != null)
                return false;
        } else if (!taxCodeId.equals(other.taxCodeId))
            return false;
        if (taxString == null) {
            if (other.taxString != null)
                return false;
        } else if (!taxString.equals(other.taxString))
            return false;
        return true;
    }
}
