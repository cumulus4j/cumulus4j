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
public class SlimArticleDBO {

    @PrimaryKey
    @Persistent(valueStrategy = IdGeneratorStrategy.SEQUENCE)
    private Long id;

    @Persistent
    private String article_id;

    
    @Persistent
    @Embedded
    private SlimPriceDBO pricePreTax;

    @SuppressWarnings("unused")
    @Persistent
    private String lc_article_id;

    public String getArticle_id() {
        return article_id;
    }

    public void setArticle_id(String article_id) {
        this.article_id = article_id;
        this.lc_article_id = article_id.toLowerCase().trim();
    }

    public SlimPriceDBO getPricePreTax() {
        return pricePreTax;
    }

    public void setPricePreTax(SlimPriceDBO pricePreTax) {
        this.pricePreTax = pricePreTax;
    }

    public Long getId() {
        return id;
    }
}
