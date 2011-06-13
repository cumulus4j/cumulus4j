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

@PersistenceCapable(
	identityType=IdentityType.APPLICATION,
	detachable="true",
	table="JFireTrade_SummaryAccount"
)
@Inheritance(strategy=InheritanceStrategy.NEW_TABLE)
public class SummaryAccount extends Account
{
	private static final long serialVersionUID = 1L;

	/**
	 * @deprecated Only for JDO!
	 */
	@Deprecated
	protected SummaryAccount() {
	}

	@Join
	@Persistent(
		nullValue=NullValue.EXCEPTION,
		table="JFireTrade_SummaryAccount_summedAccounts",
		persistenceModifier=PersistenceModifier.PERSISTENT
	)
	protected Set<Account> summedAccounts;

	public void addSummedAccount(Account account) {
		_addSummedAccount(account);
		account._addSummaryAccount(this);
	}

	protected void _addSummedAccount(Account account) {
		summedAccounts.add(account);
	}

	public void removeSummedAccount(Account account) {
		_removeSummedAccount(account);
		account._removeSummaryAccount(this);
	}

	public void _removeSummedAccount(Account account) {
		summedAccounts.remove(account);
	}

	public Collection<Account> getSummedAccounts() {
		return Collections.unmodifiableCollection(summedAccounts);
	}

	public SummaryAccount(String organisationID, String anchorID)
	{
		super(organisationID, anchorID);
		summedAccounts = new HashSet<Account>();
	}
}
