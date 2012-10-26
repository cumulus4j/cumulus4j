/**
 * Copyright (C) by AX Business Solutions AG
 * (2010-2011)
 * All rights reserved
 */
package de.alexgauss.test.server;

import java.io.Serializable;
import java.math.BigDecimal;

/**
 * The following class mixes display information with business logic.
 * Perhaps think about a better solution.
 * 
 * @author Jan
 */
public class PaymentTerms implements Serializable {

    private static final long serialVersionUID = 4289868035065684626L;

    public enum PaymentOptions {
        IMMEDIATE, PAY_WITHIN_DAYS, PAY_WITHIN_DAYS_OFFER_DISCOUNT, RECEIVED_WITH_THANKS
    }

    private PaymentOptions paymentOption;

    private int standardPaymentDays = 30;

    /**
     * Only used with PAY_WITHIN_DAYS_OFFER_DISCOUNT.
     */
    private int reducedPaymentDays;

    /**
     * Only used with PAY_WITHIN_DAYS_OFFER_DISCOUNT.
     * Only used for displaying the receipt.
     */
    private boolean showReducedAmount;

    /**
     * Only used for displaying the receipt.
     */
    private boolean showDates;

    /** Skonto */
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
