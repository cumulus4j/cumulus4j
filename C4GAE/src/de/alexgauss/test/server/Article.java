/**
 * Copyright (C) by AX Business Solutions AG
 * (2010-2011)
 * All rights reserved
 */
package de.alexgauss.test.server;

import java.io.Serializable;
import java.math.BigDecimal;

public class Article implements Serializable, Comparable<Article> {

    /**
     * Generated serial version id.
     */
    private static final long serialVersionUID = -7299915499041177889L;

    private String article_id = "";

    private String name = "";

    private String description = "";

    private Price pricePreTax = new Price();

    private String unitId = "";

    private String unitText = "";

    private String taxCodeId;

    private Integer version = 0;

    // Konstruktor
    public Article() {
    }

    public String getArticle_id() {
        return article_id;
    }

    public void setArticle_id(String article_id) {
        this.article_id = article_id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getTaxCodeId() {
        return taxCodeId;
    }

    public void setTaxCodeId(String taxCodeId) {
        this.taxCodeId = taxCodeId;
    }

    public Price getPricePreTax() {
        return pricePreTax;
    }

    public void setPricePreTax(Price pricePreTax) {
        this.pricePreTax = pricePreTax;
    }

    public void setPricePreTax(String currency, double pricePreTax) {
        this.pricePreTax = new Price(currency, pricePreTax);
    }

    public String getUnitId() {
        return unitId;
    }

    public void setUnitId(String unitId) {
        this.unitId = unitId;
    }

    public void setUnitText(String unitText) {
        this.unitText = unitText;
    }

    public String getUnitText() {
        return unitText;
    }

    public Integer getVersion() {
        return version;
    }

    public void setVersion(Integer version) {
        this.version = version;
    }

    public Price getArticlePriceAfterTax(TaxCode taxCode) {
        assert (taxCode.getTaxCodeId().equals(taxCodeId));

        BigDecimal factor = taxCode.getTax();
        factor = factor.add(BigDecimal.valueOf(1));
        factor.setScale(2, BigDecimal.ROUND_HALF_UP);
        return (pricePreTax.multiply(factor));
    }
  

    public Price getTax(TaxCode taxCode) {
        assert (taxCode.getTaxCodeId().equals(taxCodeId));
        
        return (pricePreTax.multiply(taxCode.getTax()));
    }
    
    

    public String getUnit(Unit unit) {
        assert (unit.getUnitId().equals(taxCodeId));

        return (unit.getUnitText());
    }

    @Override
    public int compareTo(Article o) {

        if (Integer.valueOf(article_id).compareTo(Integer.valueOf(o.getArticle_id())) < 0)
            return -1;
        if (Integer.valueOf(article_id).compareTo(Integer.valueOf(o.getArticle_id())) > 0)
            return 1;

        return 0;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((article_id == null) ? 0 : article_id.hashCode());
        result = prime * result + ((description == null) ? 0 : description.hashCode());
        result = prime * result + ((name == null) ? 0 : name.hashCode());
        result = prime * result + ((pricePreTax == null) ? 0 : pricePreTax.hashCode());
        result = prime * result + ((taxCodeId == null) ? 0 : taxCodeId.hashCode());
        result = prime * result + ((unitId == null) ? 0 : unitId.hashCode());
        result = prime * result + ((version == null) ? 0 : version.hashCode());
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
        Article other = (Article) obj;
        if (article_id == null) {
            if (other.article_id != null)
                return false;
        } else if (!article_id.equals(other.article_id))
            return false;
        if (description == null) {
            if (other.description != null)
                return false;
        } else if (!description.equals(other.description))
            return false;
        if (name == null) {
            if (other.name != null)
                return false;
        } else if (!name.equals(other.name))
            return false;
        if (pricePreTax == null) {
            if (other.pricePreTax != null)
                return false;
        } else if (!pricePreTax.equals(other.pricePreTax))
            return false;
        if (taxCodeId == null) {
            if (other.taxCodeId != null)
                return false;
        } else if (!taxCodeId.equals(other.taxCodeId))
            return false;
        if (unitId == null) {
            if (other.unitId != null)
                return false;
        } else if (!unitId.equals(other.unitId))
            return false;
        if (version == null) {
            if (other.version != null)
                return false;
        } else if (!version.equals(other.version))
            return false;
        return true;
    }
}
