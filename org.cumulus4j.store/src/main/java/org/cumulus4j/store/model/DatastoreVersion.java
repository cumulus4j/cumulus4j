package org.cumulus4j.store.model;

import java.util.Date;

import javax.jdo.annotations.Column;
import javax.jdo.annotations.IdentityType;
import javax.jdo.annotations.NullValue;
import javax.jdo.annotations.PersistenceCapable;
import javax.jdo.annotations.Persistent;
import javax.jdo.annotations.PrimaryKey;

import org.cumulus4j.store.datastoreversion.DatastoreVersionManager;

@PersistenceCapable(identityType=IdentityType.APPLICATION, detachable="true")
public class DatastoreVersion {

	@PrimaryKey
	@Persistent(nullValue=NullValue.EXCEPTION)
	@Column(length=255)
	private String datastoreVersionID;

	@Persistent(nullValue=NullValue.EXCEPTION)
	private Date applyTimestamp;

	private int managerVersion;

	private int commandVersion;

	protected DatastoreVersion() { }

	public DatastoreVersion(String datastoreVersionID) {
		this.datastoreVersionID = datastoreVersionID;
	}

	public String getDatastoreVersionID() {
		return datastoreVersionID;
	}

	public Date getApplyTimestamp() {
		return applyTimestamp;
	}
	public void setApplyTimestamp(Date executedTimestamp) {
		this.applyTimestamp = executedTimestamp;
	}

	/**
	 * Get the version of the {@link DatastoreVersionManager manager} by whom this object was written.
	 * @return the version of the {@link DatastoreVersionManager manager} by whom this object was written.
	 */
	public int getManagerVersion() {
		return managerVersion;
	}

	public void setManagerVersion(int version) {
		this.managerVersion = version;
	}

	public int getCommandVersion() {
		return commandVersion;
	}

	public void setCommandVersion(int commandVersion) {
		this.commandVersion = commandVersion;
	}
}
