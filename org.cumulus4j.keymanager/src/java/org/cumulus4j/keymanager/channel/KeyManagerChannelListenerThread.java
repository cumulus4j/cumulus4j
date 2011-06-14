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
package org.cumulus4j.keymanager.channel;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.StringWriter;
import java.net.URI;
import java.net.URL;

import javax.ws.rs.core.MediaType;

import org.cumulus4j.keymanager.back.shared.ErrorResponse;
import org.cumulus4j.keymanager.back.shared.JAXBContextResolver;
import org.cumulus4j.keymanager.back.shared.NullResponse;
import org.cumulus4j.keymanager.back.shared.Request;
import org.cumulus4j.keymanager.back.shared.Response;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.UniformInterfaceException;
import com.sun.jersey.api.client.WebResource;
import com.sun.jersey.api.client.config.ClientConfig;
import com.sun.jersey.api.client.config.DefaultClientConfig;

/**
 * <p>
 * Thread that listens to incoming {@link Request}s and processes them.
 * </p>
 *
 * @author Marco หงุ่ยตระกูล-Schulze - marco at nightlabs dot de
 */
public class KeyManagerChannelListenerThread
extends Thread
{
	private static final Logger logger = LoggerFactory.getLogger(KeyManagerChannelListenerThread.class);
	private KeyManagerChannelManager keyManagerChannelManager;

	/**
	 * Instantiate a new listener thread.
	 * @param keyManagerChannelManager the manager which instantiates this thread and manages
	 * the {@link RequestHandler}s to dispatch the incoming requests to.
	 */
	public KeyManagerChannelListenerThread(KeyManagerChannelManager keyManagerChannelManager)
	{
		if (keyManagerChannelManager == null)
			throw new IllegalArgumentException("keyManagerChannelManager == null");

		this.keyManagerChannelManager = keyManagerChannelManager;
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

	private Client client;
	private URI nextRequestURI = null;

	@Override
	public void run() {
		Response response = null;
		while (!isInterrupted()) {
			try {
				if (keyManagerChannelManager.unregisterThreadIfMoreThanDesiredThreadCount(this))
					return;

				if (client == null) {
					ClientConfig clientConfig = new DefaultClientConfig(JAXBContextResolver.class);
					client = Client.create(clientConfig);
				}

				if (nextRequestURI == null) {
					String s = keyManagerChannelManager.getKeyManagerChannelURL().toString();
					if (!s.endsWith("/"))
						s += '/';

					nextRequestURI = new URL(s + "nextRequest/" + keyManagerChannelManager.getSessionManager().getCryptoSessionIDPrefix()).toURI();
				}

				WebResource.Builder nextRequestWebResourceBuilder = client.resource(
						nextRequestURI
				).type(MediaType.APPLICATION_XML_TYPE).accept(MediaType.APPLICATION_XML_TYPE);

				if (response == null)
					response = new NullResponse(); // It seems Jersey does not allow null as entity :-(

				Request request = nextRequestWebResourceBuilder.post(Request.class, response);

				response = null; // we processed the last response (if any) and thus have to clear this now to prevent sending it again.

				if (request != null) {
					try {
						RequestHandler<Request> requestHandler = keyManagerChannelManager.getRequestHandler(request);
						response = requestHandler.handle(request);
					} catch (Throwable t) {
						logger.error("run: " + t, t);
						response = new ErrorResponse(request, t);
					}

					if (response == null)
						response = new NullResponse(request);
				}
			} catch (UniformInterfaceException x) {
				try {
					InputStream in = x.getResponse().getEntityInputStream();
					BufferedReader r = new BufferedReader(new InputStreamReader(in, "UTF-8"));
					StringWriter sw = new StringWriter();
					String s;
					while (null != (s = r.readLine()))
						sw.append(s);

					in.close();
					logger.error("run: " + x + "\n" + sw, x);
				} catch (Exception y) {
					logger.error("run: Caught exception while processing UniformInterfaceException: " + y, y);
				}
				try { Thread.sleep(5000); } catch (InterruptedException e) { doNothing(); } // prevent hammering on the server
			} catch (Exception x) {
				logger.error("run: " + x, x);
				try { Thread.sleep(5000); } catch (InterruptedException e) { doNothing(); } // prevent hammering on the server
			}
		}
	}

	private static final void doNothing() { }
}
