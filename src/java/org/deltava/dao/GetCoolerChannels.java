// Copyright 2005, 2006, 2007, 2008, 2009, 2011, 2012, 2016, 2017, 2019, 2020, 2021 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.dao;

import java.util.*;
import java.sql.*;
import java.util.stream.Collectors;

import org.deltava.beans.cooler.*;
import org.deltava.beans.system.AirlineInformation;

import org.deltava.util.*;
import org.deltava.util.cache.*;

/**
 * A Data Access Object to load Water Cooler channel profiles.
 * @author Luke
 * @version 10.0
 * @since 1.0
 */

public class GetCoolerChannels extends DAO {

	private static final Cache<Channel> _cache = CacheManager.get(Channel.class, "CoolerChannels");

	/**
	 * Create this DAO using a JDBC connection.
	 * @param c the JDBC connection to use
	 */
	public GetCoolerChannels(Connection c) {
		super(c);
	}
	
	/**
	 * Helper class to allow displaying of last subject in a channel.
	 */
	public class LastPostMessage extends Message {
		private final String _subject;

		LastPostMessage(int authorID, String subject) {
			super(authorID);
			_subject = subject;
		}

		public String getSubject() {
			return _subject;
		}
	}
	
	/**
	 * Retrieves a specific Channel profile. This populate the roles and airlines.
	 * @param channelName the Channel name
	 * @return the Channel profile
	 * @throws DAOException if a JDBC error occurs
	 */
	public Channel get(String channelName) throws DAOException {
		Channel c = _cache.get(channelName);
		if (c != null) return c;

		try (PreparedStatement ps = prepareWithoutLimits("SELECT C.*, (SELECT T.ID FROM common.COOLER_THREADS T WHERE (T.CHANNEL=C.CHANNEL) AND (T.HIDDEN=?) ORDER BY T.LASTUPDATE DESC LIMIT 1) AS LT FROM common.COOLER_CHANNELS C WHERE (C.CHANNEL=?) LIMIT 1")) {
			ps.setBoolean(1, false);
			ps.setString(2, channelName);

			// Execute the query - if nothing is returned, return null
			try (ResultSet rs = ps.executeQuery()) {
				if (!rs.next()) return null;
				c = new Channel(channelName);
				c.setDescription(rs.getString(2));
				c.setActive(rs.getBoolean(3));
				c.setAllowNewPosts(rs.getBoolean(4));
				c.setLastThreadID(rs.getInt(5));
			}

			// Load the roles and airlines
			loadInfo(c);
			_cache.add(c);
			return c;
		} catch (SQLException se) {
			throw new DAOException(se);
		}
	}

	/**
	 * Retrieves all Channels for a particular airline.
	 * @param al the Airline
	 * @param showInactive TRUE if inactive channels should be returned, otherwise FALSE 
	 * @return a List of Channel beans
	 * @throws DAOException if a JDBC error occurs
	 */
	public List<Channel> getChannels(AirlineInformation al, boolean showInactive) throws DAOException {

		// Build the SQL statement optionally showing inactive channels
		StringBuilder sqlBuf = new StringBuilder("SELECT CHANNEL FROM common.COOLER_CHANNELS");
		if (!showInactive)
			sqlBuf.append(" WHERE (ACTIVE=?)");

		Collection<String> names = new LinkedHashSet<String>();
		try (PreparedStatement ps = prepareWithoutLimits(sqlBuf.toString())) {
			if (!showInactive) ps.setBoolean(1, true);
			try (ResultSet rs = ps.executeQuery()) {
				while (rs.next())
					names.add(rs.getString(1));
			}
		} catch (SQLException se) {
			throw new DAOException(se);
		}
		
		// Load from the cache
		List<Channel> channels = new ArrayList<Channel>();
		for (String name : names) {
			Channel c = get(name);
			if ((al == null) || c.hasAirline(al.getCode()))
				channels.add(c);
		}
		
		// Add the "special" channels
		channels.add(0, Channel.ALL);
		channels.add(1, Channel.SHOTS);
		return channels;
	}

	/**
	 * Retrieves all <i>active</i> Channels for a particular airline and available to users with a given Collection of
	 * roles. If any role in the list is &quot;Admin&quot; then all channels for the Airline will be returned.
	 * @param al the Airline
	 * @param roles a Collection of role names
	 * @return a List of channels
	 * @throws DAOException if a JDBC error occurs
	 */
	public List<Channel> getChannels(AirlineInformation al, Collection<String> roles) throws DAOException {

		// Check if we are querying for the admin role; in this case return everything
		if (roles.contains("Developer"))
			return getChannels(null, true);

		// Check if we can display locked threads
		List<Channel> channels = getChannels(al, false);
		return channels.stream().filter(ch -> RoleUtils.hasAccess(roles, ch.getReadRoles())).collect(Collectors.toList());
	}

	/*
	 * Helper method to load Channel roles, airlines and totals.
	 */
	private void loadInfo(Channel c) throws SQLException {
		try (PreparedStatement ps = prepareWithoutLimits("SELECT * FROM common.COOLER_CHANNELINFO WHERE (CHANNEL=?)")) {
			ps.setString(1,  c.getName());
			try (ResultSet rs = ps.executeQuery()) {
				while (rs.next()) {
					Channel.InfoType inf = Channel.InfoType.values()[rs.getInt(2)];
					if (inf == Channel.InfoType.AIRLINE)
						c.addAirline(rs.getString(3));
					else
						c.addRole(inf, rs.getString(3));
				}
			}
		}
		
		try (PreparedStatement ps = prepareWithoutLimits("SELECT SUM(POSTS), COUNT(DISTINCT ID), SUM(VIEWS) FROM common.COOLER_THREADS WHERE (CHANNEL=?)")) {
			ps.setString(1, c.getName());
			try (ResultSet rs = ps.executeQuery()) {
				if (rs.next()) {
					c.setPostCount(rs.getInt(1));
					c.setThreadCount(rs.getInt(2));
					c.setViewCount(rs.getInt(3));
				}
			}
		}
	}

	/**
	 * Returns a Map of the last posts in a group of Channels, indexed by Post ID.
	 * @param channels a Collection of channels to query
	 * @return a Map of Messages, keyed by database ID
	 * @throws DAOException if a JDBC error occurs
	 */
	public Map<Integer, Message> getLastPosts(Collection<Channel> channels) throws DAOException {

		// Build a set of post IDs
		Collection<Integer> idSet = channels.stream().map(Channel::getLastThreadID).filter(id -> (id.intValue() > 0)).collect(Collectors.toSet());
		if (idSet.isEmpty())
			return Collections.emptyMap();

		// Init the SQL statement
		StringBuilder sqlBuf = new StringBuilder("SELECT T.* FROM common.COOLER_THREADS T WHERE (T.ID IN (");
		sqlBuf.append(StringUtils.listConcat(idSet, ","));
		sqlBuf.append("))");

		// Close and prepare the statement
		List<Message> results = new ArrayList<Message>();
		try (PreparedStatement ps = prepareWithoutLimits(sqlBuf.toString())) {
			try (ResultSet rs = ps.executeQuery()) {
				while (rs.next()) {
					LastPostMessage msg = new LastPostMessage(rs.getInt(13), rs.getString(2));
					msg.setThreadID(rs.getInt(1));
					msg.setID(msg.getThreadID());
					msg.setCreatedOn(toInstant(rs.getTimestamp(12)));
					results.add(msg);
				}
			}
		} catch (SQLException se) {
			throw new DAOException(se);
		}

		return CollectionUtils.createMap(results, Message::getID);
	}
}