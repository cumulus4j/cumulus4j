package org.cumulus4j.store.test.jpa.account;

import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import javax.persistence.Entity;
import javax.persistence.Inheritance;
import javax.persistence.InheritanceType;
import javax.persistence.JoinTable;
import javax.persistence.OneToMany;
import javax.persistence.Table;
import javax.persistence.Transient;

import org.cumulus4j.store.test.jpa.account.id.AnchorID;

@Entity
@Table(name="JFireTrade_Account")
@Inheritance(strategy=InheritanceType.JOINED)
public class Account
extends Anchor
{
	private static final long serialVersionUID = 1L;

    public static final String ANCHOR_TYPE_ID_ACCOUNT = "Account";

	private long balance = 0;

	public Account(AnchorID anchorID)
	{
		this(anchorID.organisationID, anchorID.anchorID);
		if (!ANCHOR_TYPE_ID_ACCOUNT.equals(anchorID.anchorTypeID))
			throw new IllegalArgumentException("anchorID.anchorTypeID != ANCHOR_TYPE_ID_ACCOUNT");
	}

	public Account(String organisationID, String anchorID)
	{
		super(organisationID, ANCHOR_TYPE_ID_ACCOUNT, anchorID);
		this.summaryAccounts = new HashSet<SummaryAccount>();
	}

	/**
	 * The balance in the smallest unit available in the Currency of this Account. This is e.g.
	 * Cent for EUR.
	 *
	 * @return Returns the balance.
	 */
	public long getBalance()
	{
		return balance;
	}

	protected void adjustBalance(boolean isDebit, long amount) {
		if (isDebit)
			this.balance = this.balance - amount;
		else
			this.balance = this.balance + amount;
	}

	@Transient
	protected transient boolean skip_bookAccountMoneyTransfer = false;

	@OneToMany
	@JoinTable(name="JFireTrade_Account_summaryAccounts")
	protected Set<SummaryAccount> summaryAccounts;

	public void addSummaryAccount(SummaryAccount summaryAccount) {
		_addSummaryAccount(summaryAccount);
		summaryAccount._addSummedAccount(this);
	}

	protected void _addSummaryAccount(SummaryAccount summaryAccount) {
		summaryAccounts.add(summaryAccount);
	}

	public void removeSummaryAccount(SummaryAccount summaryAccount) {
		_removeSummaryAccount(summaryAccount);
		summaryAccount._removeSummedAccount(this);
	}

	public void _removeSummaryAccount(SummaryAccount summaryAccount) {
		summaryAccounts.remove(summaryAccount);
	}

	public Collection<SummaryAccount> getSummaryAccounts() {
		return Collections.unmodifiableCollection(summaryAccounts);
	}
}
