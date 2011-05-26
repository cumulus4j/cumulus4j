package org.cumulus4j.store.crypto.keymanager.messagebroker.pmf;

/**
 * A <code>PendingRequest</code>'s {@link PendingRequest#getStatus() status}.
 * <p>
 * The {@link MessageBrokerPMF} first assigns the status {@link #waitingForProcessing}
 * when a new request comes in when a thread enters the
 * {@link MessageBrokerPMF#query(Class, org.cumulus4j.keymanager.back.shared.Request)
 * query(...)} method.
 * </p>
 * <p>
 * When a key-manager obtains the request by calling {@link MessageBrokerPMF#pollRequest(String) pollRequest(...)},
 * the status changes to {@link #currentlyBeingProcessed}.
 * </p>
 * <p>
 * Finally, when the {@link MessageBrokerPMF#pushResponse(org.cumulus4j.keymanager.back.shared.Response) pushResponse(...)}
 * method is called, the response is associated with the request
 * (via {@link PendingRequest#setResponse(org.cumulus4j.keymanager.back.shared.Response)}) and the status advances to {@link #completed}.
 * </p>
 * <p>
 * When the {@link MessageBrokerPMF#query(Class, org.cumulus4j.keymanager.back.shared.Request)
 * query(...)} method picks up the response, it finally deletes the {@link PendingRequest} from the datastore.
 * </p>
 *
 * @author Marco หงุ่ยตระกูล-Schulze - marco at nightlabs dot de
 */
public enum PendingRequestStatus
{
	waitingForProcessing,
	currentlyBeingProcessed,
	completed
}
