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
