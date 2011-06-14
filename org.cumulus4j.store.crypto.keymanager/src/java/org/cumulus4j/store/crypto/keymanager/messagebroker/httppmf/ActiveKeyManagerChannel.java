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
//package org.cumulus4j.store.crypto.keymanager.messagebroker.httppmf;
//
//import java.util.ArrayList;
//import java.util.Collection;
//import java.util.Date;
//
//import javax.jdo.PersistenceManager;
//import javax.jdo.annotations.Column;
//import javax.jdo.annotations.Index;
//import javax.jdo.annotations.Indices;
//import javax.jdo.annotations.NullValue;
//import javax.jdo.annotations.PersistenceCapable;
//import javax.jdo.annotations.Persistent;
//import javax.jdo.annotations.Queries;
//import javax.jdo.annotations.Query;
//import javax.jdo.annotations.Unique;
//
///**
// * @author Marco หงุ่ยตระกูล-Schulze - marco at nightlabs dot de
// * @deprecated This is unfinished work - an experiment so to say - and probably will never be finished as this doesn't work in GAE anyway.
// */
//@Deprecated
//@PersistenceCapable
//@Unique(members={"clusterNodeID", "cryptoSessionIDPrefix"})
//@Indices({
//	@Index(members="cryptoSessionIDPrefix"),
//	@Index(members="expiryTimestamp")
//})
//@Queries({
//		@Query(
//				name="getActiveKeyManagerChannelByUniqueIndex",
//				value="SELECT UNIQUE WHERE this.clusterNodeID == :clusterNodeID && this.cryptoSessionIDPrefix == :cryptoSessionIDPrefix"
//		),
//		@Query(
//				name="getActiveKeyManagerChannelsForCryptoSessionIDPrefix",
//				value="SELECT WHERE this.cryptoSessionIDPrefix == :cryptoSessionIDPrefix"
//		),
//		@Query(
//				name="getExpiredActiveKeyManagerChannels",
//				value="SELECT WHERE this.expiryTimestamp < :expiryTimestamp"
//		)
//})
//public class ActiveKeyManagerChannel
//{
//	public static ActiveKeyManagerChannel getActiveKeyManagerChannel(PersistenceManager pm, String clusterNodeID, String cryptoSessionIDPrefix)
//	{
//		javax.jdo.Query q = pm.newNamedQuery(ActiveKeyManagerChannel.class, "getActiveKeyManagerChannelByUniqueIndex");
//		return (ActiveKeyManagerChannel) q.execute(clusterNodeID, cryptoSessionIDPrefix);
//	}
//
//	public static Collection<ActiveKeyManagerChannel> getActiveKeyManagerChannelsForCryptoSessionIDPrefix(PersistenceManager pm, String cryptoSessionIDPrefix)
//	{
//		javax.jdo.Query q = pm.newNamedQuery(ActiveKeyManagerChannel.class, "getActiveKeyManagerChannelsForCryptoSessionIDPrefix");
//		@SuppressWarnings("unchecked")
//		Collection<ActiveKeyManagerChannel> c = (Collection<ActiveKeyManagerChannel>) q.execute(cryptoSessionIDPrefix);
//		ArrayList<ActiveKeyManagerChannel> result = new ArrayList<ActiveKeyManagerChannel>(c);
//		q.closeAll();
//		return result;
//	}
//
//	public static Collection<ActiveKeyManagerChannel> getExpiredActiveKeyManagerChannels(PersistenceManager pm)
//	{
//		Date now = new Date();
//		javax.jdo.Query q = pm.newNamedQuery(ActiveKeyManagerChannel.class, "getExpiredActiveKeyManagerChannels");
//		@SuppressWarnings("unchecked")
//		Collection<ActiveKeyManagerChannel> c = (Collection<ActiveKeyManagerChannel>) q.execute(now);
//		ArrayList<ActiveKeyManagerChannel> result = new ArrayList<ActiveKeyManagerChannel>(c);
//		q.closeAll();
//		return result;
//	}
//
//	public static void deleteExpiredActiveKeyManagerChannels(PersistenceManager pm)
//	{
//		Collection<ActiveKeyManagerChannel> expiredActiveKeyManagerChannels = getExpiredActiveKeyManagerChannels(pm);
//		pm.deletePersistentAll(expiredActiveKeyManagerChannels);
//		pm.flush();
//	}
//
//	@Persistent(nullValue=NullValue.EXCEPTION)
//	private String clusterNodeID;
//
//	@Persistent(nullValue=NullValue.EXCEPTION)
//	private String cryptoSessionIDPrefix;
//
//	@Persistent(nullValue=NullValue.EXCEPTION)
//	@Column(jdbcType="CLOB")
//	private String internalKeyManagerChannelURL;
//
//	@Persistent(nullValue=NullValue.EXCEPTION)
//	private Date registrationTimestamp;
//
//	@Persistent(nullValue=NullValue.EXCEPTION)
//	private Date expiryTimestamp;
//
//	public ActiveKeyManagerChannel() { }
//
//	public ActiveKeyManagerChannel(String clusterNodeID, String cryptoSessionIDPrefix)
//	{
//		if (clusterNodeID == null)
//			throw new IllegalArgumentException("clusterNodeID == null");
//
//		if (cryptoSessionIDPrefix == null)
//			throw new IllegalArgumentException("cryptoSessionIDPrefix == null");
//
//		this.clusterNodeID = clusterNodeID;
//		this.cryptoSessionIDPrefix = cryptoSessionIDPrefix;
//	}
//
//	public String getClusterNodeID() {
//		return clusterNodeID;
//	}
//
//	public String getCryptoSessionIDPrefix() {
//		return cryptoSessionIDPrefix;
//	}
//
//	public String getInternalKeyManagerChannelURL() {
//		return internalKeyManagerChannelURL;
//	}
//	public void setInternalKeyManagerChannelURL(String baseURL) {
//		this.internalKeyManagerChannelURL = baseURL;
//	}
//
//	public Date getRegistrationTimestamp() {
//		return registrationTimestamp;
//	}
//
//	public void setRegistrationTimestamp(Date registrationTimestamp) {
//		this.registrationTimestamp = registrationTimestamp;
//	}
//
//	public Date getExpiryTimestamp() {
//		return expiryTimestamp;
//	}
//	public void setExpiryTimestamp(Date expireTimestamp) {
//		this.expiryTimestamp = expireTimestamp;
//	}
//}
