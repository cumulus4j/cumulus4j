/**
 * Copyright (C) by AX Business Solutions AG
 * (2010-2011)
 * All rights reserved
 */
package de.alexgauss.test.server;

import java.io.Serializable;
import java.math.BigDecimal;

public class ReceiptItem implements Serializable {
    private static final long serialVersionUID = -3660074050300790029L;

    private Article article = null;

    /**
     * Member field to preserve historical data
     * Information is combined with article.taxCodeId.
     */
    private TaxCode taxCode = null;

    private BigDecimal amount = null;
   

    public Article getArticle() {
        return article;
    }

    public void setArticle(Article article) {
        this.article = article;
    }

    public TaxCode getTaxCode() {
        return taxCode;
    }

    public void setTaxCode(TaxCode taxCode) {
        this.taxCode = taxCode;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public void setAmount(BigDecimal amount) {
        this.amount = amount;
    }

    public void setAmount(String amount) {
        this.amount = new BigDecimal(amount);
    }

    public void setAmount(double amount) {
        this.amount = new BigDecimal(amount);
    }

    /**
     * Get the total price after tax.
     * 
     * @return total price after tax.
     */
    public final Price getTotalReceiptItemPricePreTax() {
        return (article.getPricePreTax().multiply(amount));
    }


    public Price getTotalReceiptItemPriceAfterTax() {
    	return this.getTotalReceiptItemPricePreTax().add(this.getTotalReceiptItemTax());
    	
    }

    /**
     * get the total Tax of a Article
     * @return tax
     */
    public Price getTotalReceiptItemTax() {
    	
    	Price helpPrice = new Price("EUR", "0.00");
    	helpPrice = article.getPricePreTax().multiply(amount);
    	
    	BigDecimal factor = taxCode.getTax();
    	return (helpPrice.multiply(factor));
  
    }
    
    /*
    public Price getTotalReceiptItemTax() {

    	return (article.getTax(taxCode).multiply(amount));
	
    }
    */
   
    
 
    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((amount == null) ? 0 : amount.hashCode());
        result = prime * result + ((article == null) ? 0 : article.hashCode());
        result = prime * result + ((taxCode == null) ? 0 : taxCode.hashCode());
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
        ReceiptItem other = (ReceiptItem) obj;
        if (amount == null) {
            if (other.amount != null)
                return false;
        } else if (!amount.equals(other.amount))
            return false;
        if (article == null) {
            if (other.article != null)
                return false;
        } else if (!article.equals(other.article))
            return false;
        if (taxCode == null) {
            if (other.taxCode != null)
                return false;
        } else if (!taxCode.equals(other.taxCode))
            return false;
        return true;
    }
}
