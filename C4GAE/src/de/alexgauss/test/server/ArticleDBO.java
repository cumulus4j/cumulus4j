/**
 * Copyright (C) by AX Business Solutions AG
 * (2010-2011)
 * All rights reserved
 */
package de.alexgauss.test.server;

import javax.jdo.annotations.Embedded;
import javax.jdo.annotations.IdGeneratorStrategy;
import javax.jdo.annotations.PersistenceCapable;
import javax.jdo.annotations.Persistent;
import javax.jdo.annotations.PrimaryKey;

@PersistenceCapable
public class ArticleDBO {

    @PrimaryKey
    @Persistent(valueStrategy = IdGeneratorStrategy.SEQUENCE)
    private Long id;

    @Persistent
    private String article_id = "";

    @Persistent
    private String name = "";

    @Persistent
    private String description = "";

    @Persistent
    private String taxCodeId;

    @Persistent
    private PriceDBO pricePreTax = new PriceDBO();

    @Persistent
    private String unitId = "";

    @Persistent
    private String unitText = "";

    @Persistent
    private Integer version = 0;

    @Persistent
    private String companyId;

    // The following variables are lower-case versions of the upper ones.
    // They are used only inside queries and therefore possess neither getter
    // nor setter
    @SuppressWarnings("unused")
    @Persistent
    private String lc_article_id = "";

    @SuppressWarnings("unused")
    @Persistent
    private String lc_name = "";

    @SuppressWarnings("unused")
    @Persistent
    private String lc_description = "";

    public String getUnitId() {
        return unitId;
    }

    public void setUnitId(String unitId) {
        this.unitId = unitId;
    }

    public String getUnitText() {
        return unitText;
    }

    public void setUnitText(String unitText) {
        this.unitText = unitText;
    }

    public String getCompanyId() {
        return companyId;
    }

    public void setCompanyId(String companyId) {
        this.companyId = companyId;
    }

    public Integer getVersion() {
        return this.version;
    }

    public void setVersion(Integer ver) {
        this.version = ver;
    }

    public String getArticle_id() {
        return article_id;
    }

    public void setArticle_id(String article_id) {
        this.article_id = article_id;
        this.lc_article_id = article_id.toLowerCase().trim();
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
        this.lc_name = name.toLowerCase().trim();
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
        this.lc_description = description.toLowerCase().trim();
    }

    public String getTaxCodeId() {
        return taxCodeId;
    }

    public void setTaxCodeId(String taxCodeId) {
        this.taxCodeId = taxCodeId;
    }

    public PriceDBO getPricePreTax() {
        return pricePreTax;
    }

    public void setPricePreTax(PriceDBO pricePreTax) {
        this.pricePreTax = pricePreTax;
    }

    public Long getId() {
        return id;
    }
}
