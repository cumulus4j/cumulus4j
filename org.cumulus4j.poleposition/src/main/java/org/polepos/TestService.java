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
package org.polepos;

import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;

import org.cumulus4j.store.crypto.CryptoManager;
import org.cumulus4j.store.crypto.CryptoSession;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Path("Test")
@Consumes( { MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON })
@Produces( { MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON })
public class TestService {
	private static Logger logger = LoggerFactory.getLogger(TestService.class);

	@POST
	@Produces(MediaType.TEXT_PLAIN)
	public String testPost(
			@QueryParam("cryptoManagerID") String cryptoManagerID,
			@QueryParam("cryptoSessionID") String cryptoSessionID) {

		logger.info("#################################");
		logger.info("Starting PolePosition benchmark.");
		logger.info("#################################");

		if (cryptoManagerID == null || cryptoManagerID.isEmpty())
			cryptoManagerID = "keyManager";

//		RunSeason.main(new String[]{cryptoManagerID, cryptoSessionID});
		RunSeason runSeason = new RunSeason();
		runSeason.getRuntimeProperties().setProperty(CryptoManager.PROPERTY_CRYPTO_MANAGER_ID, cryptoManagerID);
		runSeason.getRuntimeProperties().setProperty(CryptoSession.PROPERTY_CRYPTO_SESSION_ID, cryptoSessionID);

		runSeason.run();

		return "OK:";
	}
}
