package org.cumulus4j.store.test.inheritance.sources;

import java.util.Date;

import javax.jdo.annotations.Embedded;
import javax.jdo.annotations.IdGeneratorStrategy;
import javax.jdo.annotations.PersistenceCapable;
import javax.jdo.annotations.Persistent;
import javax.jdo.annotations.PrimaryKey;

@PersistenceCapable
public class Class_C {

    @PrimaryKey
    @Persistent(valueStrategy = IdGeneratorStrategy.SEQUENCE)
    private Long c_id;

    @Persistent
    private String class_c_id;

    @Persistent
    private InformationDBO information;

    @Persistent
    private int score;

    @Persistent
    private Integer version;

    @Persistent
    private Date date;

    public Integer getVersion() {
        return version;
    }

    public void setVersion(Integer version) {
        this.version = version;
    }

    public String getClass_c_id() {
        return class_c_id;
    }

    public void setClass_c_id(String class_c_id) {
        this.class_c_id = class_c_id;
    }

    public InformationDBO getInformation() {
        return information;
    }

    public void setInformation(InformationDBO information) {
        this.information = information;
    }

    public int getScore() {
        return score;
    }

    public void setScore(int score) {
        this.score = score;
    }

    public Long getId() {
        return c_id;
    }

    public Date getDate() {
        return date;
    }

    public void setDate(Date date) {
        this.date = date;
    }

}
