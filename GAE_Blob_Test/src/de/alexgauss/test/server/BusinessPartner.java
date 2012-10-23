/**
 * Copyright (C) by AX Business Solutions AG
 * (2010-2011)
 * All rights reserved
 */
package de.alexgauss.test.server;

import java.io.Serializable;
import java.util.Date;

public class BusinessPartner implements Serializable {

    /**
     * Generated serial version id.
     */
    private static final long serialVersionUID = -1862946184631185581L;

    private String businessPartner_id = "";

    private Address address = null;

    private String eMail_address = "";

    private String phoneNumber = "";

    private String cellPhoneNumber = "";

    private String fax_number = "";

    private Date birthDate;

    private String website = "";

    private String logoKeyString = "";

    private int customerScore;

    private BankAccount bankAccount;

    private TaxAccount taxAccount;

    private Integer version = 0;

    private Date registrationDate;

    // Information about who is the businessPartner
    private String relationship = ""; // Kunde, Lieferant, oder man selbst

    // Key of the signature image
    private String signatureKeyString = null;

    // Signature name
    private String signatureName = "";

    /**
     * This field indicates, which sub the company chose.
     */
    private String subscription = "0";

    /**
     * This field indicates, which billingMethod the company chose.
     */
    private String billingMethod = "0";

    public BusinessPartner() {
    }

    public Date getRegistrationDate() {
        return registrationDate;
    }

    public void setRegistrationDate(Date registrationDate) {
        this.registrationDate = registrationDate;
    }

    public String getBusinessPartner_id() {
        return businessPartner_id;
    }

    public void setBusinessPartner_id(String businessPartner_id) {
        this.businessPartner_id = businessPartner_id;
    }

    public Address getAddress() {
        return address;
    }

    public void setAddress(Address address) {
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

    public BankAccount getBankAccount() {
        return bankAccount;
    }

    public void setBankAccount(BankAccount bankAccount) {
        this.bankAccount = bankAccount;
    }

    public TaxAccount getTaxAccount() {
        return taxAccount;
    }

    public void setTaxAccount(TaxAccount taxAccount) {
        this.taxAccount = taxAccount;
    }

    public Integer getVersion() {
        return version;
    }

    public void setVersion(Integer version) {
        this.version = version;
    }

    public String getRelationship() {
        return relationship;
    }

    public void setRelationship(String relationship) {
        this.relationship = relationship;
    }

    public static long getSerialversionuid() {
        return serialVersionUID;
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

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((address == null) ? 0 : address.hashCode());
        result = prime * result + ((bankAccount == null) ? 0 : bankAccount.hashCode());
        result = prime * result + ((birthDate == null) ? 0 : birthDate.hashCode());
        result = prime * result + ((businessPartner_id == null) ? 0 : businessPartner_id.hashCode());
        result = prime * result + ((cellPhoneNumber == null) ? 0 : cellPhoneNumber.hashCode());
        result = prime * result + customerScore;
        result = prime * result + ((eMail_address == null) ? 0 : eMail_address.hashCode());
        result = prime * result + ((fax_number == null) ? 0 : fax_number.hashCode());
        result = prime * result + ((logoKeyString == null) ? 0 : logoKeyString.hashCode());
        result = prime * result + ((phoneNumber == null) ? 0 : phoneNumber.hashCode());
        result = prime * result + ((registrationDate == null) ? 0 : registrationDate.hashCode());
        result = prime * result + ((relationship == null) ? 0 : relationship.hashCode());
        result = prime * result + ((signatureKeyString == null) ? 0 : signatureKeyString.hashCode());
        result = prime * result + ((signatureName == null) ? 0 : signatureName.hashCode());
        result = prime * result + ((taxAccount == null) ? 0 : taxAccount.hashCode());
        result = prime * result + ((version == null) ? 0 : version.hashCode());
        result = prime * result + ((website == null) ? 0 : website.hashCode());
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
        BusinessPartner other = (BusinessPartner) obj;
        if (address == null) {
            if (other.address != null)
                return false;
        } else if (!address.equals(other.address))
            return false;
        if (bankAccount == null) {
            if (other.bankAccount != null)
                return false;
        } else if (!bankAccount.equals(other.bankAccount))
            return false;
        if (birthDate == null) {
            if (other.birthDate != null)
                return false;
        } else if (!birthDate.equals(other.birthDate))
            return false;
        if (businessPartner_id == null) {
            if (other.businessPartner_id != null)
                return false;
        } else if (!businessPartner_id.equals(other.businessPartner_id))
            return false;
        if (cellPhoneNumber == null) {
            if (other.cellPhoneNumber != null)
                return false;
        } else if (!cellPhoneNumber.equals(other.cellPhoneNumber))
            return false;
        if (customerScore != other.customerScore)
            return false;
        if (eMail_address == null) {
            if (other.eMail_address != null)
                return false;
        } else if (!eMail_address.equals(other.eMail_address))
            return false;
        if (fax_number == null) {
            if (other.fax_number != null)
                return false;
        } else if (!fax_number.equals(other.fax_number))
            return false;
        if (logoKeyString == null) {
            if (other.logoKeyString != null)
                return false;
        } else if (!logoKeyString.equals(other.logoKeyString))
            return false;
        if (phoneNumber == null) {
            if (other.phoneNumber != null)
                return false;
        } else if (!phoneNumber.equals(other.phoneNumber))
            return false;
        if (registrationDate == null) {
            if (other.registrationDate != null)
                return false;
        } else if (!registrationDate.equals(other.registrationDate))
            return false;
        if (relationship == null) {
            if (other.relationship != null)
                return false;
        } else if (!relationship.equals(other.relationship))
            return false;
        if (signatureKeyString == null) {
            if (other.signatureKeyString != null)
                return false;
        } else if (!signatureKeyString.equals(other.signatureKeyString))
            return false;
        if (signatureName == null) {
            if (other.signatureName != null)
                return false;
        } else if (!signatureName.equals(other.signatureName))
            return false;
        if (taxAccount == null) {
            if (other.taxAccount != null)
                return false;
        } else if (!taxAccount.equals(other.taxAccount))
            return false;
        if (version == null) {
            if (other.version != null)
                return false;
        } else if (!version.equals(other.version))
            return false;
        if (website == null) {
            if (other.website != null)
                return false;
        } else if (!website.equals(other.website))
            return false;
        return true;
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
