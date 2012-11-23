/**
 * Copyright (C) by AX Business Solutions AG
 * (2010-2011)
 * All rights reserved
 */
package de.alexgauss.test.server;

import javax.jdo.annotations.Column;
import javax.jdo.annotations.Embedded;
import javax.jdo.annotations.IdGeneratorStrategy;
import javax.jdo.annotations.PersistenceCapable;
import javax.jdo.annotations.Persistent;
import javax.jdo.annotations.PrimaryKey;

@PersistenceCapable
public class OfferDBO extends ReceiptDBO {

    @Persistent(serialized = "true")
    private ReceiptItemList offerItems = null;

    @Persistent
    @Embedded(members = {@Persistent(name = "price", columns = @Column(name = "pricePreTax")),
            @Persistent(name = "currency", columns = @Column(name = "pricePreTaxCurrency")) })
    private PriceDBO pricePreTax = null;


    @Persistent
	@Embedded(members = {@Persistent(name = "price", columns = @Column(name = "priceAfterTax")),
            @Persistent(name = "currency", columns = @Column(name = "priceAfterTaxCurrency")) })
    private PriceDBO priceAfterTax = null;

    @Persistent
    private String signatureGreeting;

    @Persistent
    private String introduction;

    @Persistent
    private String furtherDetails;

    @Persistent
    @Embedded
    private PaymentTermsDBO paymentTerms;

    public ReceiptItemList getOfferItems() {
        return offerItems;
    }

    public void setOfferItems(ReceiptItemList offerItems) {
        this.offerItems = offerItems;
    }

    public void setSignatureGreeting(String signatureGreeting) {
        this.signatureGreeting = signatureGreeting;
    }

    public String getSignatureGreeting() {
        return signatureGreeting;
    }

    public void setIntroduction(String introduction) {
        this.introduction = introduction;
    }

    public String getIntroduction() {
        return introduction;
    }

    public void setFurtherDetails(String furtherDetails) {
        this.furtherDetails = furtherDetails;
    }

    public String getFurtherDetails() {
        return furtherDetails;
    }

    public void setPaymentTerms(PaymentTermsDBO paymentTerms) {
        this.paymentTerms = paymentTerms;
    }

    public PaymentTermsDBO getPaymentTerms() {
        return paymentTerms;
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
