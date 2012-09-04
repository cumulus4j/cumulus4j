package org.cumulus4j.store.model;

import java.util.Date;

import javax.jdo.annotations.Column;
import javax.jdo.annotations.IdGeneratorStrategy;
import javax.jdo.annotations.IdentityType;
import javax.jdo.annotations.NullValue;
import javax.jdo.annotations.PersistenceCapable;
import javax.jdo.annotations.Persistent;
import javax.jdo.annotations.PrimaryKey;
import javax.jdo.annotations.Unique;
import javax.jdo.annotations.Uniques;

import org.cumulus4j.store.datastoreversion.DatastoreVersionCommand;
import org.cumulus4j.store.datastoreversion.DatastoreVersionManager;

@PersistenceCapable(identityType=IdentityType.APPLICATION, detachable="true")
@Uniques({
	@Unique(members={"keyStoreRefID", "commandID"})
})
public class DatastoreVersion {

	@PrimaryKey
	private long datastoreVersionID;

	@Persistent(nullValue=NullValue.EXCEPTION)
	@Column(length=255)
	private String commandID;

	private int keyStoreRefID;

	@Persistent(nullValue=NullValue.EXCEPTION)
	private Date applyTimestamp;

	private int managerVersion;

	private int commandVersion;

	private Integer workInProgressManagerVersion;

	private Integer workInProgressCommandVersion;

	@Persistent(nullValue=NullValue.EXCEPTION)
	@Column(jdbcType="CLOB")
	private String workInProgressStateProperties;

	protected DatastoreVersion() { }

	public DatastoreVersion(String commandID, int keyStoreRefID) {
		if (commandID == null)
			throw new IllegalArgumentException("commandID == null");

		this.commandID = commandID;
		this.keyStoreRefID = keyStoreRefID;
	}

	public String getCommandID() {
		return commandID;
	}

	public Integer getKeyStoreRefID() {
		return keyStoreRefID;
	}

	/**
	 * Get the timestamp of the last time, the corresponding {@link DatastoreVersionCommand} was applied.
	 * It is measured, when the command either completed or threw a
	 * @return
	 */
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

	public Integer getWorkInProgressManagerVersion() {
		return workInProgressManagerVersion;
	}
	public void setWorkInProgressManagerVersion(Integer workInProgressManagerVersion) {
		this.workInProgressManagerVersion = workInProgressManagerVersion;
	}

	public String getWorkInProgressStateProperties() {
		return workInProgressStateProperties;
	}
	public void setWorkInProgressStateProperties(String workInPrgressStateProperties) {
		this.workInProgressStateProperties = workInPrgressStateProperties;
	}

	public Integer getWorkInProgressCommandVersion() {
		return workInProgressCommandVersion;
	}
	public void setWorkInProgressCommandVersion(Integer newCommandVersion) {
		this.workInProgressCommandVersion = newCommandVersion;
	}
}
