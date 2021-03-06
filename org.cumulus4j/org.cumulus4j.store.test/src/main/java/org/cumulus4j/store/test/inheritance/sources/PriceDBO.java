package org.cumulus4j.store.test.inheritance.sources;

import java.math.BigDecimal;

import javax.jdo.annotations.PersistenceCapable;
import javax.jdo.annotations.Persistent;

@PersistenceCapable
public class PriceDBO {

    @Persistent
    private String currency;

    @Persistent
    private BigDecimal price;

    public String getCurrency() {
        return currency;
    }

    public void setCurrency(String currency) {
        this.currency = currency;
    }

    public BigDecimal getPrice() {
        return price;
    }

    public void setPrice(BigDecimal price) {
        this.price = price;
    }
}
