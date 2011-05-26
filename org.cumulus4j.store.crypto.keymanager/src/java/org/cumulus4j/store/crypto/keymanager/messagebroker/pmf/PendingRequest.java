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
//	@Index(members={"cryptoSessionIDPrefix", "status"}),
	@Index(members={"cryptoSessionIDPrefix", "status", "lastStatusChangeTimestamp"})
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
	)
})
public class PendingRequest
{
	public static final class FetchGroup {
		public static final String request = "PendingRequest.request";
		public static final String response = "PendingRequest.response";
	}

	/**
	 * Get the oldest <code>PendingRequest</code> matching the given criteria.
	 * @param pm the {@link PersistenceManager} for accessing the message-transfer-database.
	 * @param cryptoSessionIDPrefix the {@link #getCryptoSessionIDPrefix() cryptoSessionIDPrefix} used
	 * as criterion to filter the candidate-<code>PendingRequest</code>s.
	 * @param status the {@link #getStatus() status} used as criterion to filter the candidate-<code>PendingRequest</code>s.
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
	 * @param pm the {@link PersistenceManager} for accessing the message-transfer-database.
	 * @param requestID the unique identifier of the {@link PendingRequest} to obtain.
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

	protected PendingRequest() { }

	/**
	 * Create an instance of <code>PendingRequest</code> for the given <code>request</code>.
	 * @param request the request to be processed and thus temporarily stored in the database.
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

	public String getRequestID() {
		return requestID;
	}

	public String getCryptoSessionIDPrefix() {
		return cryptoSessionIDPrefix;
	}

	public PendingRequestStatus getStatus() {
		return status;
	}
	public void setStatus(PendingRequestStatus status) {
		this.status = status;
		this.lastStatusChangeTimestamp = new Date();
	}

	public Request getRequest() {
		return request;
	}

	public Response getResponse() {
		return response;
	}

	public void setResponse(Response response) {
		this.response = response;
	}

	public Date getCreationTimestamp() {
		return creationTimestamp;
	}

	public Date getLastStatusChangeTimestamp() {
		return lastStatusChangeTimestamp;
	}
}
