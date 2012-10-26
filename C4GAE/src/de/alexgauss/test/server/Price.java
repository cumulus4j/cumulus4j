/**
 * Copyright (C) by AX Business Solutions AG
 * (2010-2011)
 * All rights reserved
 */
package de.alexgauss.test.server;

import java.io.Serializable;
import java.math.BigDecimal;

public class Price implements Serializable {

    private static final long serialVersionUID = 4981113697780715794L;

    /**
     * Currency.
     */
    private String currency;

    /**
     * Price.
     */
    private BigDecimal price;

    public Price() {
        this("EUR", "0.00");
    }

    public Price(String currency, long value) {
        this.currency = currency;
        price = BigDecimal.valueOf(value);
        price = price.setScale(2, BigDecimal.ROUND_HALF_UP);
    }

    public Price(String currency, String value) {
        this.currency = currency;
        price = new BigDecimal(value);
        price = price.setScale(2, BigDecimal.ROUND_HALF_UP);
    }

    public Price(String currency, BigDecimal value) {
        this.currency = currency;
        price = value;
        price = price.setScale(2, BigDecimal.ROUND_HALF_UP);
    }

    public Price(String currency, double value) {
        this.currency = currency;
        price = new BigDecimal(value);
        price = price.setScale(2, BigDecimal.ROUND_HALF_UP);
    }

    public Price(Price objectToCopy) {
        currency = new String(objectToCopy.currency);
        price = new BigDecimal(objectToCopy.price.toString());
        price = price.setScale(2, BigDecimal.ROUND_HALF_UP);
    }

    public void setPrice(BigDecimal price) {
        this.price = price;
    }

    public BigDecimal getPrice() {
        return price;
    }

    public void setCurrency(String currency) {
        this.currency = currency;
    }

    public String getCurrency() {
        return currency;
    }

    public Price add(Price value) {
        Price result = new Price(this);
        result.price = result.price.add(value.price);
        return result;
    }

    public Price subtract(Price value) {
        Price result = new Price(this);
        result.price = result.price.subtract(value.price);
        return result;
    }

    public Price multiply(Price value) {
        Price result = new Price(this);
        result.price = result.price.multiply(value.price);
        result.price = result.price.setScale(2, BigDecimal.ROUND_HALF_UP);
        return result;
    }

    public Price divide(Price value) {
        Price result = new Price(this);
        result.price = result.price.divide(value.price);
        result.price = result.price.setScale(2, BigDecimal.ROUND_HALF_UP);
        return result;
    }

    public Price multiply(BigDecimal amount) {
        Price result = new Price(this);
        result.price = result.price.multiply(amount);
        result.price = result.price.setScale(2, BigDecimal.ROUND_HALF_UP);
        return result;
    }
  
    

    @Override
    public String toString() {
        return price.toString();
    }

    public static Price valueOf(String currency, String value) throws NumberFormatException {
        return new Price(currency, value);
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((currency == null) ? 0 : currency.hashCode());
        result = prime * result + ((price == null) ? 0 : price.hashCode());
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
        Price other = (Price) obj;
        if (currency == null) {
            if (other.currency != null)
                return false;
        } else if (!currency.equals(other.currency))
            return false;
        if (price == null) {
            if (other.price != null)
                return false;
        } else if (!price.equals(other.price))
            return false;
        return true;
    }
}
