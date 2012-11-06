package org.cumulus4j.store.test.inheritance.sources;

import java.io.Serializable;

public class Article implements Serializable, Comparable<Article> {

    private static final long serialVersionUID = -7299915499041177889L;

    private String article_id;

    private String name;

    private Price pricePreTax;

    private Integer version;

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

    public Price getPricePreTax() {
        return pricePreTax;
    }

    public void setPricePreTax(Price pricePreTax) {
        this.pricePreTax = pricePreTax;
    }

    public void setPricePreTax(String currency, double pricePreTax) {
        this.pricePreTax = new Price(currency, pricePreTax);
    }

    public Integer getVersion() {
        return version;
    }

    public void setVersion(Integer version) {
        this.version = version;
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
        result = prime * result + ((name == null) ? 0 : name.hashCode());
        result = prime * result + ((pricePreTax == null) ? 0 : pricePreTax.hashCode());
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
        if (version == null) {
            if (other.version != null)
                return false;
        } else if (!version.equals(other.version))
            return false;
        return true;
    }
}
