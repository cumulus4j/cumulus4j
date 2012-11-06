package org.cumulus4j.store.test.inheritance.sources;

import java.io.Serializable;
import java.util.Date;

public class Class_D implements Serializable{

    /**
     * Generated serial version id.
     */
    private static final long serialVersionUID = -1862946184631185581L;

    private String class_d_id;

    private Information information;

    private int score;

    private Integer version;

    private Date date;

    public Class_D() {
    }

    public Date getDate() {
        return date;
    }

    public void setDate(Date date) {
        this.date = date;
    }

    public String getClass_d_id() {
        return class_d_id;
    }

    public void setClass_d_id(String class_d_id) {
        this.class_d_id = class_d_id;
    }

    public Information getInformation() {
        return information;
    }

    public void setInformation(Information information) {
        this.information = information;
    }

    public int getScore() {
        return score;
    }

    public void setScore(int score) {
        this.score = score;
    }

    public Integer getVersion() {
        return version;
    }

    public void setVersion(Integer version) {
        this.version = version;
    }

    public static long getSerialversionuid() {
        return serialVersionUID;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((information == null) ? 0 : information.hashCode());
        result = prime * result + ((class_d_id == null) ? 0 : class_d_id.hashCode());
        result = prime * result + score;
        result = prime * result + ((date == null) ? 0 : date.hashCode());
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
        Class_D other = (Class_D) obj;
        if (information == null) {
            if (other.information != null)
                return false;
        } else if (!information.equals(other.information))
            return false;
        if (class_d_id == null) {
            if (other.class_d_id != null)
                return false;
        } else if (!class_d_id.equals(other.class_d_id))
            return false;
        if (score != other.score)
            return false;
        if (date == null) {
            if (other.date != null)
                return false;
        } else if (!date.equals(other.date))
            return false;
        if (version == null) {
            if (other.version != null)
                return false;
        } else if (!version.equals(other.version))
            return false;
        return true;
    }

}
