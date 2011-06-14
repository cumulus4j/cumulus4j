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
package org.cumulus4j.store.crypto.keymanager.rest;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import javax.ws.rs.ApplicationPath;
import javax.ws.rs.core.Application;

import org.cumulus4j.keymanager.back.shared.JAXBContextResolver;

/**
 * REST application providing the API for the communication between key-manager and application-server.
 * The URL to this application must be given to the key-manager (i.e. more precisely the <code>AppServer</code>).
 *
 * @author Marco หงุ่ยตระกูล-Schulze - marco at nightlabs dot de
 */
@ApplicationPath("/org.cumulus4j.keyserver.back.webapp")
public class KeyManagerBackWebApp
extends Application
{
//	private static final Logger logger = LoggerFactory.getLogger(KeyManagerBackWebApp.class);

	private static final Class<?>[] serviceClassesArray = {
		KeyManagerChannelService.class,
		JAXBContextResolver.class
	};

	private static final Set<Class<?>> serviceClassesSet;
	static {
		Set<Class<?>> s = new HashSet<Class<?>>(serviceClassesArray.length);
		for (Class<?> c : serviceClassesArray)
			s.add(c);

		serviceClassesSet = Collections.unmodifiableSet(s);
	}

	@Override
	public Set<Class<?>> getClasses() {
		return serviceClassesSet;
	}

	private Set<Object> singletons;

	@Override
	public Set<Object> getSingletons()
	{
		if (singletons == null) {
			Set<Object> s = new HashSet<Object>();
//			s.add(new KeyStoreProvider(keyStore));
//			s.add(new SessionManagerProvider(new SessionManager(keyStore)));
			singletons = Collections.unmodifiableSet(s);
		}

		return singletons;
	}
}
