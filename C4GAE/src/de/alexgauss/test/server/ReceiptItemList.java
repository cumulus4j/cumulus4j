/**
 * Copyright (C) by AX Business Solutions AG
 * (2010-2011)
 * All rights reserved
 */
package de.alexgauss.test.server;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

public class ReceiptItemList implements Serializable {

    private static final long serialVersionUID = 3429460057260319234L;

    private List<ReceiptItem> receiptItems = new ArrayList<ReceiptItem>();

    /**
     * Returns a list of tax codes which are used inside this bill. Each tax
     * code is listed only once
     */
    public Set<TaxCode> getTaxCodeSet() {
        Set<TaxCode> set = new HashSet<TaxCode>();
        for (ReceiptItem currentItem : getReceiptItems()) {
            set.add(currentItem.getTaxCode());
        }
        return set;
    }

    public Price getTotalPricePreTax() {
        Price taxSum = new Price("EUR", "0.00");

        for (ReceiptItem currentItem : getReceiptItems()) {
            taxSum = taxSum.add(currentItem.getTotalReceiptItemPricePreTax());
        }

        return taxSum;
    }

    public List<TaxCodeItem> getTaxCodeItems() {
        Set<TaxCode> taxCodes = getTaxCodeSet();

        List<TaxCodeItem> resultList = new ArrayList<TaxCodeItem>();

        for (Iterator<TaxCode> i = taxCodes.iterator(); i.hasNext();) {
            TaxCode currentTaxCode = i.next();
            Price taxSum = new Price("EUR", "0.00");

            for (ReceiptItem currentItem : getReceiptItems()) {
                if (currentItem.getTaxCode().equals(currentTaxCode)) {
                    taxSum = taxSum.add(currentItem.getTotalReceiptItemTax());
                }
            }

            resultList.add(new TaxCodeItem(currentTaxCode, taxSum));
        }

        return resultList;
    }


    /*
    public Price getTotalPriceAfterTax() {
        Price taxSum = new Price("EUR", "0.00");
        for (ReceiptItem currentItem : getReceiptItems()) {
            taxSum = taxSum.add(currentItem.getTotalReceiptItemPriceAfterTax());
        }
        
        return taxSum;
    }
    */
    /**
     * Calculate the total price after tax.
     * 
     * @return Total price
     */    
    public Price getTotalPriceAfterTax() {
        Price preTax = new Price("EUR", "0.00");
        preTax = getTotalPricePreTax();
        
        Price taxSum = new Price("EUR", "0.00");
        
        List<TaxCodeItem> taxes = getTaxCodeItems();  
        for (TaxCodeItem item : taxes) {
        	taxSum = taxSum.add(item.getTotalTax());
        }
        
        
        return (preTax.add(taxSum));
    }

    public void setReceiptItems(List<ReceiptItem> receiptItems) {
        this.receiptItems = receiptItems;
    }

    public List<ReceiptItem> getReceiptItems() {
        return receiptItems;
    }

    public void moveItemAfter(int receiptItemIndex, int receiptItemIndexBefore) {
        ReceiptItem receiptItem = receiptItems.get(receiptItemIndex);
        receiptItems.remove(receiptItemIndex);

        int insertIndex = receiptItemIndexBefore;
        if (receiptItemIndex > receiptItemIndexBefore) {
            insertIndex++;
        }

        receiptItems.add(insertIndex, receiptItem);
    }

    public void moveItemBefore(int receiptItemIndex, int receiptItemIndexAfter) {
        ReceiptItem receiptItem = receiptItems.get(receiptItemIndex);
        receiptItems.remove(receiptItemIndex);

        int insertIndex = receiptItemIndexAfter;
        if (receiptItemIndex < receiptItemIndexAfter) {
            insertIndex--;
        }

        receiptItems.add(insertIndex, receiptItem);
    }
}
