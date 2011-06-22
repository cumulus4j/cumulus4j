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
package org.cumulus4j.keymanager.front.webapp;

import java.io.IOException;

import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

import org.cumulus4j.keymanager.front.shared.DateDependentKeyStrategyInitParam;
import org.cumulus4j.keymanager.front.shared.Error;
import org.cumulus4j.keystore.DateDependentKeyStrategy;
import org.cumulus4j.keystore.KeyStore;
import org.cumulus4j.keystore.KeyStoreNotEmptyException;

@Path("DateDependentKeyStrategy")
@Consumes({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
@Produces({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
public class DateDependentKeyStrategyService extends AbstractService
{
	@Path("{keyStoreID}/init")
	@POST
	public void init(@PathParam("keyStoreID") String keyStoreID, DateDependentKeyStrategyInitParam param)
	{
		Auth auth = getAuth();
		try {
			KeyStore keyStore = keyStoreManager.getKeyStore(keyStoreID);
			new DateDependentKeyStrategy(keyStore).init(
					auth.getUserName(), auth.getPassword(),
					param.getKeyActivityPeriodMSec(), param.getKeyStorePeriodMSec()
			);
		} catch (KeyStoreNotEmptyException e) {
			throw new WebApplicationException(Response.status(Status.BAD_REQUEST).entity(new Error(e)).build());
		} catch (IOException e) {
			throw new WebApplicationException(Response.status(Status.INTERNAL_SERVER_ERROR).entity(new Error(e)).build());
		} finally {
			auth.clear();
		}
	}
}
