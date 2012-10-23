/**
 * Copyright (C) by AX Business Solutions AG
 * (2010-2011)
 * All rights reserved
 */
package de.alexgauss.test.server;

import java.io.Serializable;
import java.math.BigDecimal;

import javax.jdo.annotations.PersistenceCapable;
import javax.jdo.annotations.Persistent;

import de.alexgauss.test.server.PaymentTerms.PaymentOptions;

@PersistenceCapable
public class PaymentTermsDBO implements Serializable {

    private static final long serialVersionUID = 1931143776207228660L;

    @Persistent
    private PaymentOptions paymentOption;

    @Persistent
    private int standardPaymentDays;

    @Persistent
    private int reducedPaymentDays;

    @Persistent
    private boolean showReducedAmount;

    @Persistent
    private boolean showDates;

    @Persistent
    private BigDecimal cashDiscount;

    public PaymentOptions getPaymentOption() {
        return paymentOption;
    }

    public void setPaymentOption(PaymentOptions paymentOption) {
        this.paymentOption = paymentOption;
    }

    public int getStandardPaymentDays() {
        return standardPaymentDays;
    }

    public void setStandardPaymentDays(int standardPaymentDays) {
        this.standardPaymentDays = standardPaymentDays;
    }

    public int getReducedPaymentDays() {
        return reducedPaymentDays;
    }

    public void setReducedPaymentDays(int reducedPaymentDays) {
        this.reducedPaymentDays = reducedPaymentDays;
    }

    public BigDecimal getCashDiscount() {
        return cashDiscount;
    }

    public void setCashDiscount(BigDecimal cashDiscount) {
        this.cashDiscount = cashDiscount;
    }

    public void setShowReducedAmount(boolean showReducedAmount) {
        this.showReducedAmount = showReducedAmount;
    }

    public boolean isShowReducedAmount() {
        return showReducedAmount;
    }

    public void setShowDates(boolean showDates) {
        this.showDates = showDates;
    }

    public boolean isShowDates() {
        return showDates;
    }
}
