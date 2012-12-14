package org.cumulus4j.store.test.inheritance.sources;

import java.util.Date;

import javax.jdo.annotations.Embedded;
import javax.jdo.annotations.IdGeneratorStrategy;
import javax.jdo.annotations.Inheritance;
import javax.jdo.annotations.InheritanceStrategy;
import javax.jdo.annotations.PersistenceCapable;
import javax.jdo.annotations.Persistent;
import javax.jdo.annotations.PrimaryKey;

@PersistenceCapable
@Inheritance(strategy = InheritanceStrategy.SUBCLASS_TABLE)
public abstract class Class_A {

    @PrimaryKey
    @Persistent(valueStrategy = IdGeneratorStrategy.SEQUENCE)
    private Long id;

    @Persistent
    private String query_id;

    @Persistent(serialized = "true")
    private Class_D sender;

    @Persistent
    private Class_C acceptor;

    @Persistent
    private Date date;

    public String getQuery_id() {
        return query_id;
    }

    public void setQuery_id(String query_id) {
        this.query_id = query_id;
    }

    public Class_D getSender() {
        return sender;
    }

    public void setSender(Class_D sender) {
        this.sender = sender;
    }

    public Class_C getAcceptor() {
        return acceptor;
    }

    public void setAcceptor(Class_C acceptor) {
        this.acceptor = acceptor;
    }

    public Date getDate() {
        return date;
    }

    public void setDate(Date date) {
        this.date = date;
    }

    public Long getId() {
        return id;
    }

}
