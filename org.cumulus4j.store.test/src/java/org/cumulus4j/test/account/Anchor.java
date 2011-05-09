package org.cumulus4j.test.account;

import java.io.Serializable;

import javax.jdo.JDOHelper;
import javax.jdo.PersistenceManager;
import javax.jdo.annotations.Column;
import javax.jdo.annotations.IdentityType;
import javax.jdo.annotations.Inheritance;
import javax.jdo.annotations.InheritanceStrategy;
import javax.jdo.annotations.PersistenceCapable;
import javax.jdo.annotations.PrimaryKey;

import org.cumulus4j.test.account.id.AnchorID;

@PersistenceCapable(
	objectIdClass=AnchorID.class,
	identityType=IdentityType.APPLICATION,
	detachable="true",
	table="JFireTrade_Anchor"
)
@Inheritance(strategy=InheritanceStrategy.NEW_TABLE)
public abstract class Anchor
	implements Serializable
{
	private static final long serialVersionUID = 1L;

	@PrimaryKey
	@Column(length=100)
	private String organisationID;

	@PrimaryKey
	@Column(length=100)
	private String anchorTypeID;

	@PrimaryKey
	@Column(length=100)
	private String anchorID;

	protected Anchor() { }

	public Anchor(String organisationID, String anchorTypeID, String anchorID)
	{
		this.organisationID = organisationID;
		this.anchorTypeID = anchorTypeID;
		this.anchorID = anchorID;
	}

	public static String getPrimaryKey(String organisationID, String anchorTypeID, String anchorID)
	{
		return organisationID + '/' + anchorTypeID + "/" + anchorID;
	}

	public String getPrimaryKey()
	{
		return getPrimaryKey(organisationID, anchorTypeID, anchorID);
	}

	public String getOrganisationID()
	{
		return organisationID;
	}

	public String getAnchorTypeID()
	{
		return anchorTypeID;
	}

	public String getAnchorID()
	{
		return anchorID;
	}

	protected PersistenceManager getPersistenceManager()
	{
		PersistenceManager pm = JDOHelper.getPersistenceManager(this);
		if (pm == null)
			throw new IllegalStateException("This instance of Anchor ("+getPrimaryKey()+") is currently not persistent or not attached. Cannot obtain PersistenceManager!");

		return pm;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((organisationID == null) ? 0 : organisationID.hashCode());
		result = prime * result + ((anchorID == null) ? 0 : anchorID.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) return true;
		if (obj == null) return false;
		if (getClass() != obj.getClass()) return false;

		Anchor other = (Anchor) obj;
		if (anchorID == null) {
			if (other.anchorID != null)
				return false;
		} else if (!anchorID.equals(other.anchorID))
			return false;

		if (organisationID == null) {
			if (other.organisationID != null)
				return false;
		} else if (!organisationID.equals(other.organisationID))
			return false;

		return true;
	}

	@Override
	public String toString() {
		return this.getClass().getName() + '@' + Integer.toHexString(System.identityHashCode(this)) + '[' + organisationID + ',' + anchorTypeID + ',' + anchorID + ']';
	}
}
