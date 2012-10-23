/**
 * Copyright (C) by AX Business Solutions AG
 * (2010-2011)
 * All rights reserved
 */
package de.alexgauss.test.server;

import java.io.Serializable;

public class BankAccount implements Serializable {
    /**
	 * 
	 */
    private static final long serialVersionUID = 8044729610135039218L;

    private String bankName = "";
    private String bankCode = "";
    private String accountNumber = "";
    private String iban = "";
    private String bic = "";

    public String getBankName() {
        return bankName;
    }

    public String getBankCode() {
        return bankCode;
    }

    public String getAccountNumber() {
        return accountNumber;
    }

    public String getIban() {
        return iban;
    }

    public String getBic() {
        return bic;
    }

    public void setBankName(String bankName) {
        this.bankName = bankName;
    }

    public void setBankCode(String bankCode) {
        this.bankCode = bankCode;
    }

    public void setAccountNumber(String accountNumber) {
        this.accountNumber = accountNumber;
    }

    public void setIban(String iban) {
        this.iban = iban;
    }

    public void setBic(String bic) {
        this.bic = bic;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        BankAccount other = (BankAccount) obj;
        if (accountNumber == null) {
            if (other.accountNumber != null)
                return false;
        } else if (!accountNumber.equals(other.accountNumber))
            return false;
        if (bankCode == null) {
            if (other.bankCode != null)
                return false;
        } else if (!bankCode.equals(other.bankCode))
            return false;
        if (bankName == null) {
            if (other.bankName != null)
                return false;
        } else if (!bankName.equals(other.bankName))
            return false;
        if (bic == null) {
            if (other.bic != null)
                return false;
        } else if (!bic.equals(other.bic))
            return false;
        if (iban == null) {
            if (other.iban != null)
                return false;
        } else if (!iban.equals(other.iban))
            return false;
        return true;
    }
}
