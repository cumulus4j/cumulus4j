package org.cumulus4j.store.test.inheritance.sources;

import javax.jdo.annotations.PersistenceCapable;
import javax.jdo.annotations.Persistent;

@PersistenceCapable
public class Class_B extends Class_A {

    @Persistent(serialized = "true")
    private ItemList items;

    @Persistent
    private PriceDBO pricePreTax;

    @Persistent
    private PriceDBO priceAfterTax;

    @Persistent
    private String text;

    @Persistent
    private TermsDBO terms;

    public ItemList getItems() {
        return items;
    }

    public void setOfferItems(ItemList items) {
        this.items = items;
    }

    public void setText(String text) {
        this.text = text;
    }

    public String getText() {
        return text;
    }

    public void setTerms(TermsDBO terms) {
        this.terms = terms;
    }

    public TermsDBO getTerms() {
        return terms;
    }

    public void setPricePreTax(PriceDBO pricePreTax) {
        this.pricePreTax = pricePreTax;
    }

    public PriceDBO getPricePreTax() {
        return pricePreTax;
    }

    public void setPriceAfterTax(PriceDBO priceAfterTax) {
        this.priceAfterTax = priceAfterTax;
    }

    public PriceDBO getPriceAfterTax() {
        return priceAfterTax;
    }
}
