package org.cumulus4j.benchmark.bankaccount;

import java.math.BigInteger;

import javax.jdo.annotations.IdGeneratorStrategy;
import javax.jdo.annotations.IdentityType;
import javax.jdo.annotations.PersistenceCapable;
import javax.jdo.annotations.Persistent;
import javax.jdo.annotations.PrimaryKey;

import org.cumulus4j.benchmark.framework.Entity;
import org.cumulus4j.benchmark.simpledatatypescenario.PersonAllQueryable;

/**
 *
 * @author Jan Mortensen - jmortensen at nightlabs dot de
 *
 */
@PersistenceCapable(identityType=IdentityType.APPLICATION, detachable="true")
public class BankAccount extends Entity{

	@PrimaryKey
	@Persistent(valueStrategy=IdGeneratorStrategy.NATIVE)
	private long id;

	@Persistent(defaultFetchGroup="true")
	private BigInteger balance;

	@Persistent
	private PersonAllQueryable owner;

	public BankAccount(PersonAllQueryable owner){
		this.owner = owner;
		balance = BigInteger.valueOf(0);
	}

	public PersonAllQueryable getOwner() {
		return owner;
	}

	public void setOwner(PersonAllQueryable owner) {
		this.owner = owner;
	}

	public BigInteger getBalance() {
		return balance;
	}

	public void setBalance(BigInteger balance) {
		this.balance = balance;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((balance == null) ? 0 : balance.hashCode());
		result = prime * result + (int) (id ^ (id >>> 32));
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
		BankAccount other = (BankAccount) obj;
		if (balance == null) {
			if (other.balance != null)
				return false;
		} else if (!balance.equals(other.balance))
			return false;
		if (id != other.id)
			return false;
		return true;
	}

	@Override
	public long getId() {
		return id;
	}
}
