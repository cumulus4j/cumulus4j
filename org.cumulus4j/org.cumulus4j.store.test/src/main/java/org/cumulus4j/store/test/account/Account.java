/*
 * Cumulus4j - Securing your data in the cloud - http://cumulus4j.org
 * Copyright (C) 2011 NightLabs Consulting GmbH
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.cumulus4j.store.test.account;

import java.util.Collection;
import java.util.Collections;
import java.util.Currency;
import java.util.HashSet;
import java.util.Set;

import javax.jdo.annotations.IdentityType;
import javax.jdo.annotations.Inheritance;
import javax.jdo.annotations.InheritanceStrategy;
import javax.jdo.annotations.Join;
import javax.jdo.annotations.NullValue;
import javax.jdo.annotations.PersistenceCapable;
import javax.jdo.annotations.PersistenceModifier;
import javax.jdo.annotations.Persistent;
import javax.jdo.listener.AttachCallback;

import org.cumulus4j.store.test.account.id.AnchorID;

@PersistenceCapable(
	identityType=IdentityType.APPLICATION,
	detachable="true",
	table="JFireTrade_Account"
)
@Inheritance(strategy=InheritanceStrategy.NEW_TABLE)
public class Account
extends Anchor
implements AttachCallback
{
	private static final long serialVersionUID = 1L;

	@Persistent(persistenceModifier=PersistenceModifier.PERSISTENT)
	private long balance = 0;

	@Persistent
	private Currency currency = Currency.getInstance("EUR");

	/**
	 * @deprecated Only for JDO!
	 */
	@Deprecated
	protected Account() { }

	public static final String ANCHOR_TYPE_ID_ACCOUNT = "Account";

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
	public long getBalance() {
		return balance;
	}

	protected void adjustBalance(boolean isDebit, long amount) {
		if (isDebit)
			this.balance = this.balance - amount;
		else
			this.balance = this.balance + amount;
	}

	public Currency getCurrency() {
		return currency;
	}

	public void setCurrency(Currency curr) {
		this.currency = curr;
	}

	@Persistent(persistenceModifier=PersistenceModifier.NONE)
	protected transient boolean skip_bookAccountMoneyTransfer = false;

	@Join
	@Persistent(
			nullValue=NullValue.EXCEPTION,
			table="JFireTrade_Account_summaryAccounts",
			persistenceModifier=PersistenceModifier.PERSISTENT
	)
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

	@Override
	public void jdoPostAttach(Object object) { }

	@Override
	public void jdoPreAttach() { }
}
