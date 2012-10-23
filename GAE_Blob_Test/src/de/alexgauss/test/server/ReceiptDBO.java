/**
 * Copyright (C) by AX Business Solutions AG
 * (2010-2011)
 * All rights reserved
 */
package de.alexgauss.test.server;

import java.util.Date;

import javax.jdo.annotations.Embedded;
import javax.jdo.annotations.IdGeneratorStrategy;
import javax.jdo.annotations.IdentityType;
import javax.jdo.annotations.Inheritance;
import javax.jdo.annotations.InheritanceStrategy;
import javax.jdo.annotations.PersistenceCapable;
import javax.jdo.annotations.Persistent;
import javax.jdo.annotations.PrimaryKey;

@Inheritance(strategy = InheritanceStrategy.SUBCLASS_TABLE)
public abstract class ReceiptDBO {

    @PrimaryKey
    @Persistent(valueStrategy = IdGeneratorStrategy.SEQUENCE)
    private Long id;

    @Persistent
    private String company_id;

    @Persistent
    private Integer version;

    @Persistent
    private String receipt_id;

    @Persistent(serialized = "true")
    private BusinessPartner receipt_sender = null;

    @Persistent
    @Embedded(nullIndicatorColumn = "businessPartner_id")
    private BusinessPartnerDBO receipt_acceptor = null;

    @Persistent
    private String pdfFileID = null;

    @Persistent
    private Date issueDate = null;

    public String getCompany_id() {
        return company_id;
    }

    public void setCompany_id(String company_id) {
        this.company_id = company_id;
    }

    public Integer getVersion() {
        return version;
    }

    public void setVersion(Integer version) {
        this.version = version;
    }

    public String getReceipt_id() {
        return receipt_id;
    }

    public void setReceipt_id(String receipt_id) {
        this.receipt_id = receipt_id;
    }

    public BusinessPartner getReceipt_sender() {
        return receipt_sender;
    }

    public void setReceipt_sender(BusinessPartner receipt_sender) {
        this.receipt_sender = receipt_sender;
    }

    public BusinessPartnerDBO getReceipt_acceptor() {
        return receipt_acceptor;
    }

    public void setReceipt_acceptor(BusinessPartnerDBO receipt_acceptor) {
        this.receipt_acceptor = receipt_acceptor;
    }

    public String getPdfFileID() {
        return pdfFileID;
    }

    public void setPdfFileID(String pdfFileID) {
        this.pdfFileID = pdfFileID;
    }

    public Date getIssueDate() {
        return issueDate;
    }

    public void setIssueDate(Date issueDate) {
        this.issueDate = issueDate;
    }

    public Long getId() {
        return id;
    }

}
