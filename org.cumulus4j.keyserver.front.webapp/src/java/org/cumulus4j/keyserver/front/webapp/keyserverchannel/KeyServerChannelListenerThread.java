package org.cumulus4j.keyserver.front.webapp.keyserverchannel;

import java.net.URL;

import javax.ws.rs.core.MediaType;

import org.cumulus4j.keyserver.back.shared.ErrorResponse;
import org.cumulus4j.keyserver.back.shared.Request;
import org.cumulus4j.keyserver.back.shared.Response;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.WebResource;

public class KeyServerChannelListenerThread
extends Thread
{
	private static final Logger logger = LoggerFactory.getLogger(KeyServerChannelListenerThread.class);
	private KeyServerChannelManager keyServerChannelManager;

	public KeyServerChannelListenerThread(KeyServerChannelManager keyServerChannelManager) {
		if (keyServerChannelManager == null)
			throw new IllegalArgumentException("keyServerChannelManager == null");

		this.keyServerChannelManager = keyServerChannelManager;
		setDaemon(true);
	}

	private volatile boolean interruptForced;

	@Override
	public void interrupt() {
		interruptForced = true;
		super.interrupt();
	}

	@Override
	public boolean isInterrupted() {
		return super.isInterrupted() || interruptForced;
	}

	private Client client = new Client();
	private WebResource.Builder nextRequestWebResourceBuilder;

	@Override
	public void run() {
		Response response = null;
		while (!isInterrupted()) {
			try {
				if (keyServerChannelManager.unregisterThreadIfMoreThanDesiredThreadCount(this))
					return;

				if (nextRequestWebResourceBuilder == null) {
					nextRequestWebResourceBuilder = client.resource(
							new URL(keyServerChannelManager.getKeyServerChannelURL(), "nextRequest/" + keyServerChannelManager.getSessionManager().getCryptoSessionIDPrefix()).toURI()
					).accept(MediaType.APPLICATION_XML_TYPE);
				}

				Request request = nextRequestWebResourceBuilder.post(Request.class, response);
				response = null; // we processed the last response (if any) and thus have to clear this now to prevent sending it again.

				if (request != null) {
					try {
						RequestHandler<Request> requestHandler = keyServerChannelManager.getRequestHandler(request);
						response = requestHandler.handle(request);
					} catch (Throwable x) {
						logger.error("run: " + x, x);
						response = new ErrorResponse(request, x);
					}
				}
			} catch (Exception x) {
				logger.error("run: " + x, x);
			}
		}
	}

}
