/**
 * Copyright (C) by AX Business Solutions AG
 * (2010-2011)
 * All rights reserved
 */
package de.alexgauss.test.server;

import java.util.Date;

import javax.jdo.annotations.Embedded;
import javax.jdo.annotations.IdGeneratorStrategy;
import javax.jdo.annotations.PersistenceCapable;
import javax.jdo.annotations.Persistent;
import javax.jdo.annotations.PrimaryKey;

import com.google.appengine.api.datastore.Key;

@PersistenceCapable
public class BusinessPartnerDBO {

    @PrimaryKey
    @Persistent(valueStrategy = IdGeneratorStrategy.SEQUENCE)
    private Long bp_id;

    /**
     * Customer id, e.g. "1".
     * 
     * This field is used in ReceiptDBO as nullIndicatorColumn and is
     * therefore initialized as null
     */
    @Persistent
    private String businessPartner_id = null;

    @Persistent
    @Embedded
    private AddressDBO address = null;

    @Persistent
    private String eMail_address = "";

    @Persistent
    private String phoneNumber = "";

    @Persistent
    private String cellPhoneNumber = "";

    @Persistent
    private String fax_number = "";

    @Persistent
    private Date birthDate;

    @Persistent
    private String website = "";

    @Persistent
    private String logoKeyString;

    @Persistent
    private int customerScore;

    @Persistent
    @Embedded
    private BankAccountDBO bankAccount;

    @Persistent
    @Embedded
    private TaxAccountDBO taxAccount;

    @Persistent
    private Integer version = 0;

    @Persistent
    private String companyId;

    @Persistent
    private Date registrationDate;

    /**
     * This field indicates, which sub the company chose.
     */
    @Persistent
    private String subscription = "0";

    /**
     * This field indicates, which billingMethod the company choosed
     */
    @Persistent
    private String billingMethod = "0";

    // Information about who is the businessPartner
    @Persistent
    private String relationship = ""; // Kunde, Lieferant, oder man selbst

    // Key of the signature image
    @Persistent
    private String signatureKeyString = null;

    // Signature name
    @Persistent
    private String signatureName = "";

    // The following variables are lower-case versions of the upper ones.
    // They are used only inside queries and therefore possess neither getter
    // nor setter
    @SuppressWarnings("unused")
    @Persistent
    private String lc_businessPartner_id ="";

    public Integer getVersion() {
        return version;
    }

    public void setVersion(Integer version) {
        this.version = version;
    }

    public String getCompanyId() {
        return companyId;
    }

    public void setCompanyId(String companyId) {
        this.companyId = companyId;
    }

    public String getBusinessPartner_id() {
        return businessPartner_id;
    }

    public void setBusinessPartner_id(String businessPartner_id) {
        this.businessPartner_id = businessPartner_id;
        this.lc_businessPartner_id = businessPartner_id.toLowerCase().trim();
    }

    public AddressDBO getAddress() {
        return address;
    }

    public void setAddress(AddressDBO address) {
        this.address = address;
    }

    public String geteMail_address() {
        return eMail_address;
    }

    public void seteMail_address(String eMail_address) {
        this.eMail_address = eMail_address;
    }

    public String getPhoneNumber() {
        return phoneNumber;
    }

    public void setPhoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber;
    }

    public String getCellPhoneNumber() {
        return cellPhoneNumber;
    }

    public void setCellPhoneNumber(String cellPhoneNumber) {
        this.cellPhoneNumber = cellPhoneNumber;
    }

    public String getFax_number() {
        return fax_number;
    }

    public void setFax_number(String fax_number) {
        this.fax_number = fax_number;
    }

    public Date getBirthDate() {
        return birthDate;
    }

    public void setBirthDate(Date birthDate) {
        this.birthDate = birthDate;
    }

    public String getWebsite() {
        return website;
    }

    public void setWebsite(String website) {
        this.website = website;
    }

    public String getLogoKeyString() {
        return logoKeyString;
    }

    public void setLogoKeyString(String logoKeyString) {
        this.logoKeyString = logoKeyString;
    }

    public int getCustomerScore() {
        return customerScore;
    }

    public void setCustomerScore(int customerScore) {
        this.customerScore = customerScore;
    }

    public BankAccountDBO getBankAccount() {
        return bankAccount;
    }

    public void setBankAccount(BankAccountDBO bankAccount) {
        this.bankAccount = bankAccount;
    }

    public TaxAccountDBO getTaxAccount() {
        return taxAccount;
    }

    public void setTaxAccount(TaxAccountDBO taxAccount) {
        this.taxAccount = taxAccount;
    }

    public String getRelationship() {
        return relationship;
    }

    public void setRelationship(String relationship) {
        this.relationship = relationship;
    }

    public Long getId() {
        return bp_id;
    }

    public void setSignatureName(String signatureName) {
        this.signatureName = signatureName;
    }

    public String getSignatureName() {
        return signatureName;
    }

    public void setSignatureKeyString(String signatureKeyString) {
        this.signatureKeyString = signatureKeyString;
    }

    public String getSignatureKeyString() {
        return signatureKeyString;
    }

    public Date getRegistrationDate() {
        return registrationDate;
    }

    public void setRegistrationDate(Date registrationDate) {
        this.registrationDate = registrationDate;
    }

    public String getSubscription() {
        return subscription;
    }

    public void setSubscription(String subscription) {
        this.subscription = subscription;
    }

    public String getBillingMethod() {
        return billingMethod;
    }

    public void setBillingMethod(String billingMethod) {
        this.billingMethod = billingMethod;
    }

}
