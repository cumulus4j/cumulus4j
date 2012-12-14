package org.cumulus4j.store.test.inheritance.sources;

import java.io.Serializable;
import java.math.BigDecimal;

public class Item implements Serializable {
    private static final long serialVersionUID = -3660074050300790029L;

    private Article article;

    private BigDecimal amount;
   
    public Article getArticle() {
        return article;
    }

    public void setArticle(Article article) {
        this.article = article;
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

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((amount == null) ? 0 : amount.hashCode());
        result = prime * result + ((article == null) ? 0 : article.hashCode());
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
        Item other = (Item) obj;
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
        return true;
    }
}
