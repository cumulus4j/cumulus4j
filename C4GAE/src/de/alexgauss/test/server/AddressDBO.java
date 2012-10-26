/**
 * Copyright (C) by AX Business Solutions AG
 * (2010-2011)
 * All rights reserved
 */
package de.alexgauss.test.server;

import javax.jdo.annotations.PersistenceCapable;
import javax.jdo.annotations.Persistent;

@PersistenceCapable
public class AddressDBO {

    @Persistent
    private String company;

    @Persistent
    private String additionalInformation;

    @Persistent
    private String salutationId;

    @Persistent
    private String salutationText;

    @Persistent
    private String firstName;

    @Persistent
    private String lastName;

    @Persistent
    private String street;

    @Persistent
    private String houseNumber; // e.g. 15b

    @Persistent
    private String postalCode;

    @Persistent
    private String city;

    @Persistent
    private String country;

    // The following variables are lower-case versions of the upper ones.
    // They are used only inside queries and therefore possess neither getter
    // nor setter
    @SuppressWarnings("unused")
    @Persistent
    private String lc_company;

    @SuppressWarnings("unused")
    @Persistent
    private String lc_additionalInformation;

    @SuppressWarnings("unused")
    @Persistent
    private String lc_salutation;

    @SuppressWarnings("unused")
    @Persistent
    private String lc_firstName;

    @SuppressWarnings("unused")
    @Persistent
    private String lc_lastName;

    @SuppressWarnings("unused")
    @Persistent
    private String lc_street;

    @SuppressWarnings("unused")
    @Persistent
    private String lc_houseNumber;

    @SuppressWarnings("unused")
    @Persistent
    private String lc_postalCode;

    @SuppressWarnings("unused")
    @Persistent
    private String lc_city;

    @SuppressWarnings("unused")
    @Persistent
    private String lc_country;

    public String getCompany() {
        return company;
    }

    public void setCompany(String company) {
        this.company = company;
        if (company != null) {
            this.lc_company = company.toLowerCase().trim();
        }
    }

    public String getAdditionalInformation() {
        return additionalInformation;
    }

    public void setAdditionalInformation(String additionalInformation) {
        this.additionalInformation = additionalInformation;
        if (additionalInformation != null) {
            this.lc_additionalInformation = additionalInformation.toLowerCase().trim();
        }
    }

    public String getSalutationId() {
        return salutationId;
    }

    public void setSalutationId(String salutationId) {
        this.salutationId = salutationId;
    }

    public String getSalutationText() {
        return salutationText;
    }

    public void setSalutationText(String salutationText) {
        this.salutationText = salutationText;
        if (salutationText != null) {
            this.lc_salutation = salutationText.toLowerCase().trim();
        }
    }

    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
        if (firstName != null) {
            this.lc_firstName = firstName.toLowerCase().trim();
        }
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
        if (lastName != null) {
            this.lc_lastName = lastName.toLowerCase().trim();
        }
    }

    public String getStreet() {
        return street;
    }

    public void setStreet(String street) {
        this.street = street;
        if (street != null) {
            this.lc_street = street.toLowerCase().trim();
        }
    }

    public String getHouseNumber() {
        return houseNumber;
    }

    public void setHouseNumber(String houseNumber) {
        this.houseNumber = houseNumber;
        if (houseNumber != null) {
            this.lc_houseNumber = houseNumber.toLowerCase().trim();
        }
    }

    public String getPostalCode() {
        return postalCode;
    }

    public void setPostalCode(String postalCode) {
        this.postalCode = postalCode;
        if (postalCode != null) {
            this.lc_postalCode = postalCode.toLowerCase().trim();
        }
    }

    public String getCity() {
        return city;
    }

    public void setCity(String city) {
        this.city = city;
        if (city != null) {
            this.lc_city = city.toLowerCase().trim();
        }
    }

    public String getCountry() {
        return country;
    }

    public void setCountry(String country) {
        this.country = country;
        if (country != null) {
            this.lc_country = country.toLowerCase().trim();
        }
    }
}
