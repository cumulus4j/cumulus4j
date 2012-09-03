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
package org.cumulus4j.store.crypto.keymanager.messagebroker.pmf;

import java.util.Collection;
import java.util.Date;
import java.util.Iterator;

import javax.jdo.JDOObjectNotFoundException;
import javax.jdo.PersistenceManager;
import javax.jdo.annotations.FetchGroup;
import javax.jdo.annotations.FetchGroups;
import javax.jdo.annotations.IdentityType;
import javax.jdo.annotations.Index;
import javax.jdo.annotations.Indices;
import javax.jdo.annotations.NullValue;
import javax.jdo.annotations.PersistenceCapable;
import javax.jdo.annotations.Persistent;
import javax.jdo.annotations.PrimaryKey;
import javax.jdo.annotations.Queries;
import javax.jdo.annotations.Query;
import javax.jdo.annotations.Version;
import javax.jdo.annotations.VersionStrategy;
import javax.jdo.identity.StringIdentity;

import org.cumulus4j.keymanager.back.shared.Request;
import org.cumulus4j.keymanager.back.shared.Response;

/**
 * Persistent container holding a {@link Request} and optionally
 * the corresponding {@link Response}. Used by {@link MessageBrokerPMF}
 * to transmit messages via a backing-database.
 * @author Marco หงุ่ยตระกูล-Schulze - marco at nightlabs dot de
 */
@PersistenceCapable(identityType=IdentityType.APPLICATION)
@Indices({
	@Index(members={"cryptoSessionIDPrefix", "status"}),
	@Index(members={"cryptoSessionIDPrefix", "status", "lastStatusChangeTimestamp"}),
	@Index(members={"lastStatusChangeTimestamp"})
})
@Version(strategy=VersionStrategy.VERSION_NUMBER)
@FetchGroups({
		@FetchGroup(name=PendingRequest.FetchGroup.request, members=@Persistent(name="request")),
		@FetchGroup(name=PendingRequest.FetchGroup.response, members=@Persistent(name="response"))
})
@Queries({
	@Query(
			name="getOldestPendingRequestWithStatus",
			value="SELECT WHERE this.cryptoSessionIDPrefix == :cryptoSessionIDPrefix && this.status == :status ORDER BY this.lastStatusChangeTimestamp ASCENDING RANGE 0, 1"
	),
	@Query(
			name="getPendingRequestsWithLastStatusChangeTimestampOlderThanTimestamp",
			value="SELECT WHERE this.lastStatusChangeTimestamp < :timestamp"
	)
})
public class PendingRequest
{
	/**
	 * <a target="_blank" href="http://www.datanucleus.org/products/accessplatform_3_0/jdo/fetchgroup.html">Fetch-groups</a> for
	 * {@link PendingRequest}.
	 * @author Marco หงุ่ยตระกูล-Schulze - marco at nightlabs dot de
	 */
	public static final class FetchGroup {
		/**
		 * Indicates fetching the {@link PendingRequest#getRequest() request} property of <code>PendingRequest</code>.
		 */
		public static final String request = "PendingRequest.request";
		/**
		 * Indicates fetching the {@link PendingRequest#getResponse() response} property of <code>PendingRequest</code>.
		 */
		public static final String response = "PendingRequest.response";
	}

	public static Collection<PendingRequest> getPendingRequestsWithLastStatusChangeTimestampOlderThanTimestamp(PersistenceManager pm, Date timestamp)
	{
		if (pm == null)
			throw new IllegalArgumentException("pm == null");

		if (timestamp == null)
			throw new IllegalArgumentException("timestamp == null");

		javax.jdo.Query q = pm.newNamedQuery(PendingRequest.class, "getPendingRequestsWithLastStatusChangeTimestampOlderThanTimestamp");
		@SuppressWarnings("unchecked")
		Collection<PendingRequest> c = (Collection<PendingRequest>) q.execute(timestamp);
		// We return this directly and don't copy it (and thus do not close the query), because we delete all of them anyway
		// and the tx is very short (no need to close the result-set, before tx-end). This way, the JDO impl has the chance
		// to optimize (i.e. not to load anything from the DB, but really *directly* delete it).
		return c;
	}

	/**
	 * Get the oldest <code>PendingRequest</code> matching the given criteria.
	 * @param pm the {@link PersistenceManager} for accessing the message-transfer-database. Must not be <code>null</code>.
	 * @param cryptoSessionIDPrefix the {@link #getCryptoSessionIDPrefix() cryptoSessionIDPrefix} used
	 * as criterion to filter the candidate-<code>PendingRequest</code>s. Must not be <code>null</code>.
	 * @param status the {@link #getStatus() status} used as criterion to filter the candidate-<code>PendingRequest</code>s.
	 * Must not be <code>null</code>.
	 * @return the oldest <code>PendingRequest</code> matching the given criteria or <code>null</code> if there is
	 * no <code>PendingRequest</code> in the datastore which matches the criteria.
	 */
	public static PendingRequest getOldestPendingRequest(PersistenceManager pm, String cryptoSessionIDPrefix, PendingRequestStatus status)
	{
		if (pm == null)
			throw new IllegalArgumentException("pm == null");

		if (cryptoSessionIDPrefix == null)
			throw new IllegalArgumentException("cryptoSessionIDPrefix == null");

		if (status == null)
			throw new IllegalArgumentException("status == null");

		javax.jdo.Query q = pm.newNamedQuery(PendingRequest.class, "getOldestPendingRequestWithStatus");
		try {
			@SuppressWarnings("unchecked")
			Collection<PendingRequest> c = (Collection<PendingRequest>) q.execute(cryptoSessionIDPrefix, status);
			Iterator<PendingRequest> it = c.iterator();
			if (it.hasNext())
				return it.next();
			else
				return null;
		} finally {
			q.closeAll();
		}
	}

	/**
	 * Get the <code>PendingRequest</code> uniquely identified by the given <code>requestID</code>.
	 * If no such  <code>PendingRequest</code> exists, return <code>null</code>.
	 * @param pm the {@link PersistenceManager} for accessing the message-transfer-database. Must not be <code>null</code>.
	 * @param requestID the unique identifier of the {@link PendingRequest} to obtain. Must not be <code>null</code>.
	 * @return the {@link PendingRequest} identified by the given <code>requestID</code> or <code>null</code>, if
	 * no such object exists in the datastore.
	 */
	public static PendingRequest getPendingRequest(PersistenceManager pm, String requestID)
	{
		if (pm == null)
			throw new IllegalArgumentException("pm == null");

		if (requestID == null)
			throw new IllegalArgumentException("requestID == null");

		StringIdentity identity = new StringIdentity(PendingRequest.class, requestID);
		try {
			return (PendingRequest) pm.getObjectById(identity);
		} catch (JDOObjectNotFoundException x) {
			return null;
		}
	}

	@PrimaryKey
	@Persistent(nullValue=NullValue.EXCEPTION)
	private String requestID;

	@Persistent(nullValue=NullValue.EXCEPTION)
	private String cryptoSessionIDPrefix;

	@Persistent(nullValue=NullValue.EXCEPTION)
	private PendingRequestStatus status;

	@Persistent(serialized="true", nullValue=NullValue.EXCEPTION)
	private Request request;

	@Persistent(serialized="true")
	private Response response;

	@Persistent(nullValue=NullValue.EXCEPTION)
	private Date creationTimestamp;

	@Persistent(nullValue=NullValue.EXCEPTION)
	private Date lastStatusChangeTimestamp;

	/**
	 * Internal constructor only used by JDO. Never call this constructor directly!
	 */
	protected PendingRequest() { }

	/**
	 * Create an instance of <code>PendingRequest</code> for the given <code>request</code>.
	 * @param request the request to be processed and thus temporarily stored in the database;
	 * must not be <code>null</code>.
	 */
	public PendingRequest(Request request)
	{
		this.requestID = request.getRequestID();
		this.cryptoSessionIDPrefix = request.getCryptoSessionIDPrefix();
		this.request = request;
		this.status = PendingRequestStatus.waitingForProcessing;
		this.creationTimestamp = new Date();
		this.lastStatusChangeTimestamp = new Date();
	}

	/**
	 * Get the {@link Request#getRequestID() requestID} of the <code>Request</code> that was passed to
	 * {@link #PendingRequest(Request)}. This property is the primary key of this class.
	 * @return the request's {@link Request#getRequestID() requestID}.
	 */
	public String getRequestID() {
		return requestID;
	}

	/**
	 * Get the {@link Request#getCryptoSessionIDPrefix() cryptoSessionIDPrefix} of the <code>Request</code> that was passed to
	 * {@link #PendingRequest(Request)}.
	 * @return the request's {@link Request#getCryptoSessionIDPrefix() cryptoSessionIDPrefix}.
	 */
	public String getCryptoSessionIDPrefix() {
		return cryptoSessionIDPrefix;
	}

	/**
	 * Get the current status.
	 * @return the current status. Can be <code>null</code>, if the instance was not yet
	 * persisted.
	 * @see #setStatus(PendingRequestStatus)
	 */
	public PendingRequestStatus getStatus() {
		return status;
	}
	/**
	 * Set the current status. This method will automatically update the
	 * {@link #getLastStatusChangeTimestamp() lastStatusChangeTimestamp}.
	 * @param status the new status; must not be <code>null</code> (because of {@link NullValue#EXCEPTION}).
	 * @see #getStatus()
	 */
	public void setStatus(PendingRequestStatus status) {
		this.status = status;
		this.lastStatusChangeTimestamp = new Date();
	}

	/**
	 * Get the {@link Request} that was passed to {@link #PendingRequest(Request)}.
	 * @return the request; never <code>null</code>.
	 */
	public Request getRequest() {
		return request;
	}

	/**
	 * Get the {@link Response} previously {@link #setResponse(Response) set} or <code>null</code>, if none is set, yet.
	 * @return the response or <code>null</code>.
	 */
	public Response getResponse() {
		return response;
	}

	/**
	 * Set the {@link Response}.
	 * @param response the response.
	 */
	public void setResponse(Response response) {
		this.response = response;
	}

	/**
	 * Get the timestamp when this <code>PendingRequest</code> was instantiated.
	 * @return when was this <code>PendingRequest</code> created.
	 */
	public Date getCreationTimestamp() {
		return creationTimestamp;
	}

	/**
	 * Get the timestamp when the {@link #getStatus() status} was changed
	 * the last time.
	 * @return the timestamp of the last {@link #getStatus() status}-change.

	 */
	public Date getLastStatusChangeTimestamp() {
		return lastStatusChangeTimestamp;
	}

	@Override
	public int hashCode() {
		return (requestID == null) ? 0 : requestID.hashCode();
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) return true;
		if (obj == null) return false;
		if (getClass() != obj.getClass()) return false;
		PendingRequest other = (PendingRequest) obj;
		return (
				this.requestID == other.requestID ||
				(this.requestID != null && this.requestID.equals(other.requestID))
		);
	}
}
