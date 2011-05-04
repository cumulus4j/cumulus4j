package org.cumulus4j.test.account;

import java.io.Serializable;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import javax.jdo.JDOHelper;
import javax.jdo.annotations.Column;
import javax.jdo.annotations.Discriminator;
import javax.jdo.annotations.DiscriminatorStrategy;
import javax.jdo.annotations.Extension;
import javax.jdo.annotations.IdentityType;
import javax.jdo.annotations.Join;
import javax.jdo.annotations.NullValue;
import javax.jdo.annotations.PersistenceCapable;
import javax.jdo.annotations.PersistenceModifier;
import javax.jdo.annotations.Persistent;
import javax.jdo.annotations.PrimaryKey;

import org.cumulus4j.test.account.id.LocalAccountantDelegateID;

@PersistenceCapable(
	objectIdClass=LocalAccountantDelegateID.class,
	identityType=IdentityType.APPLICATION,
	detachable="true",
	table="JFireTrade_LocalAccountantDelegate"
)
@Discriminator(strategy=DiscriminatorStrategy.CLASS_NAME)
public class LocalAccountantDelegate implements Serializable {
	private static final long serialVersionUID = 1L;

	/**
	 * @deprecated Only for JDO
	 */
	@Deprecated
	public LocalAccountantDelegate() {}

	@PrimaryKey
	@Column(length=100)
	private String organisationID;

	@PrimaryKey
	@Column(length=100)
	private String localAccountantDelegateID;

	@Persistent(persistenceModifier=PersistenceModifier.PERSISTENT)
	private LocalAccountantDelegate extendedAccountantDelegate;

	private String name;

	private String name2;

	@Extension(vendorName="datanucleus", key="queryable", value="false")
	private String name3;

	private Date creationDate;

	@Column(jdbcType="CLOB")
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

	public String getName3() {
		return name3;
	}

	public void setName3(String name3) {
		this.name3 = name3;
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

	@Join
	@Persistent(
			nullValue=NullValue.EXCEPTION,
			table="JFireVoucher_VoucherLocalAccountantDelegate_accounts",
			persistenceModifier=PersistenceModifier.PERSISTENT
	)
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
