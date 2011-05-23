package org.cumulus4j.keymanager.back.shared;

import javax.xml.bind.annotation.XmlRootElement;

/**
 * @author Marco หงุ่ยตระกูล-Schulze - marco at nightlabs dot de
 */
@XmlRootElement
public class NullResponse
extends Response
{
	private static final long serialVersionUID = 1L;

	/**
	 * Create a <code>NullResponse</code> without a prior request. This is just used as filler
	 * without being forwarded to any requester.
	 */
	public NullResponse() { }

	/**
	 * Create a <code>NullResponse</code> as answer to a prior request. It is
	 * processed like any other response, i.e. forwarded to the requester, but finally
	 * transformed to <code>null</code>.
	 * @param request the request that is answered by this new <code>NullResponse</code> instance.
	 */
	public NullResponse(Request request)
	{
		super(request);
	}
}
