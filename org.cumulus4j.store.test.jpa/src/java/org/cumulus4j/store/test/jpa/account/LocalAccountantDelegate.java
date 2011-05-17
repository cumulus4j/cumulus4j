package org.cumulus4j.store.test.jpa.account;

import java.io.Serializable;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import javax.jdo.JDOHelper;
import javax.persistence.Column;
import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.IdClass;
import javax.persistence.JoinTable;
import javax.persistence.Lob;
import javax.persistence.OneToMany;
import javax.persistence.OneToOne;
import javax.persistence.Table;

import org.cumulus4j.store.test.jpa.account.id.LocalAccountantDelegateID;

@Entity
@IdClass(LocalAccountantDelegateID.class)
@Table(name="JFireTrade_LocalAccountantDelegate")
@DiscriminatorValue("LocalAccountantDelegate")
public class LocalAccountantDelegate implements Serializable {
	private static final long serialVersionUID = 1L;

	@Id
	@Column(length=100)
	private String organisationID;

	@Id
	@Column(length=100)
	private String localAccountantDelegateID;

	@OneToOne
	private LocalAccountantDelegate extendedAccountantDelegate;

	private String name;

	private String name2;

	private Date creationDate;

	@Lob
	private String description;

	public LocalAccountantDelegate(LocalAccountantDelegateID localAccountantDelegateID) {
		this(localAccountantDelegateID.organisationID, localAccountantDelegateID.localAccountantDelegateID);
	}

	public LocalAccountantDelegate(String organisationID, String localAccountantDelegateID) {
		this.organisationID = organisationID;
		this.localAccountantDelegateID = localAccountantDelegateID;
		accounts = new HashMap<String, Account>();
	}

	public LocalAccountantDelegate(LocalAccountantDelegate parent, LocalAccountantDelegateID localAccountantDelegateID) {
		this(parent, localAccountantDelegateID.organisationID, localAccountantDelegateID.localAccountantDelegateID);
	}

	public LocalAccountantDelegate(LocalAccountantDelegate parent, String organisationID, String localAccountantDelegateID) {
		this(organisationID, localAccountantDelegateID);
		this.extendedAccountantDelegate = parent;
		accounts = new HashMap<String, Account>();
	}

	public String getOrganisationID() {
		return organisationID;
	}

	public String getLocalAccountantDelegateID() {
		return localAccountantDelegateID;
	}

	public LocalAccountantDelegate getExtendedAccountantDelegate() {
		return extendedAccountantDelegate;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getName2() {
		return name2;
	}

	public void setName2(String name2) {
		this.name2 = name2;
	}

	public Date getCreationDate() {
		return creationDate;
	}

	public void setCreationDate(Date date) {
		this.creationDate = date;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	@OneToMany
	@JoinTable(name="JFireVoucher_VoucherLocalAccountantDelegate_accounts")
	private Map<String, Account> accounts;

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((organisationID == null) ? 0 : organisationID.hashCode());
		result = prime * result + ((localAccountantDelegateID == null) ? 0 : localAccountantDelegateID.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) return true;
		if (obj == null) return false;
		if (getClass() != obj.getClass()) return false;

		LocalAccountantDelegate other = (LocalAccountantDelegate) obj;
		return (
				equals(this.localAccountantDelegateID, other.localAccountantDelegateID) &&
				equals(this.organisationID, other.organisationID)
		);
	}

	private static final boolean equals(String s1, String s2)
	{
		if (s1 == null)
			return s2 == null;
		else
			return s1.equals(s2);
	}

	@Override
	public String toString() {
		return this.getClass().getName() + '@' + Integer.toHexString(System.identityHashCode(this)) + '[' + organisationID + ',' + localAccountantDelegateID + ']';
	}

	public void setAccount(String currencyID, Account account)
	{
		if (account == null)
			accounts.remove(currencyID);
		else
			accounts.put(currencyID, account);
	}

	public void test()
	{
		String currencyID = "EUR";
		Account account = accounts.get(currencyID);
		if (account == null)
			throw new IllegalStateException("The VoucherLocalAccountantDelegate does not contain an account for currencyID '"+currencyID+"'!!! id='"+JDOHelper.getObjectId(this)+"'");
	}
}
