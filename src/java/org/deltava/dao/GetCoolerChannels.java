// Copyright 2005, 2006, 2007, 2008, 2009, 2011, 2012, 2016, 2017, 2019 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.dao;

import java.util.*;
import java.util.stream.Collectors;
import java.sql.*;

import org.deltava.beans.cooler.*;
import org.deltava.beans.system.AirlineInformation;

import org.deltava.util.*;
import org.deltava.util.cache.*;

/**
 * A Data Access Object to load Water Cooler channel profiles.
 * @author Luke
 * @version 9.0
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
		private String _subject;

		LastPostMessage(int authorID) {
			super(authorID);
		}

		public String getSubject() {
			return _subject;
		}

		public void setSubject(String subj) {
			_subject = subj;
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

		try (PreparedStatement ps = prepareWithoutLimits("SELECT * FROM common.COOLER_CHANNELS WHERE (CHANNEL=?) LIMIT 1")) {
			ps.setString(1, channelName);

			// Execute the query - if nothing is returned, return null
			try (ResultSet rs = ps.executeQuery()) {
				if (!rs.next()) return null;

				// Initialize the channel
				c = new Channel(channelName);
				c.setDescription(rs.getString(2));
				c.setActive(rs.getBoolean(3));
				c.setAllowNewPosts(rs.getBoolean(4));
			}

			// Load the roles and airlines
			Map<String, Channel> results = new HashMap<String, Channel>();
			results.put(c.getName(), c);
			loadInfo(results);
			_cache.add(c);
			return c;
		} catch (SQLException se) {
			throw new DAOException(se);
		}
	}

	/**
	 * Retrieves all Channels for a particular airline.
	 * @param al the Airline
	 * @param showHidden TRUE if hidden threads should be returned in the last post
	 * @param showInactive TRUE if inactive channels should be returned, otherwise FALSE 
	 * @return a List of Channel beans
	 * @throws DAOException if a JDBC error occurs
	 */
	public List<Channel> getChannels(AirlineInformation al, boolean showHidden, boolean showInactive) throws DAOException {

		// Build the SQL statement optionally showing locked threads
		StringBuilder sqlBuf = new StringBuilder("SELECT C.*, (SELECT T.ID FROM common.COOLER_THREADS T WHERE ");
		if (!showHidden)
			sqlBuf.append("(T.HIDDEN=?) AND ");
		sqlBuf.append("(T.CHANNEL=C.CHANNEL) ORDER BY T.LASTUPDATE DESC LIMIT 1) AS LT, SUM(T.POSTS), COUNT(DISTINCT T.ID), SUM(T.VIEWS) FROM common.COOLER_CHANNELS C LEFT JOIN "
			+ "common.COOLER_THREADS T ON (T.CHANNEL=C.CHANNEL) ");
		if (!showInactive)
			sqlBuf.append("WHERE (C.ACTIVE=?) ");
		sqlBuf.append("GROUP BY C.CHANNEL");

		Map<String, Channel> results = new TreeMap<String, Channel>();
		try (PreparedStatement ps = prepareWithoutLimits(sqlBuf.toString())) {
			int pos = 0;
			if (!showHidden)
				ps.setBoolean(++pos, false);
			if (!showInactive)
				ps.setBoolean(++pos, true);

			// Execute the query - we store results in a map for now
			try (ResultSet rs = ps.executeQuery()) {
				while (rs.next()) {
					Channel c = new Channel(rs.getString(1));
					c.setDescription(rs.getString(2));
					c.setActive(rs.getBoolean(3));
					c.setAllowNewPosts(rs.getBoolean(4));
					c.setLastThreadID(rs.getInt(5));
					c.setPostCount(rs.getInt(6));
					c.setThreadCount(rs.getInt(7));
					c.setViewCount(rs.getInt(8));
					results.put(c.getName(), c);
				}
			}

			// Load the roles and airlines
			loadInfo(results);
		} catch (SQLException se) {
			throw new DAOException(se);
		}

		// Parse through the results and strip out from the airline - since it's cheaper to do it here than in the query
		Collection<Channel> channels = new TreeSet<Channel>(results.values());
		if (al != null) {
			for (Iterator<Channel> i = channels.iterator(); i.hasNext();) {
				Channel c = i.next();
				if (!c.hasAirline(al.getCode()))
					i.remove();
			}
		}

		// Add to cache
		_cache.addAll(channels);

		// Add the "special" channels
		channels.add(Channel.ALL);
		channels.add(Channel.SHOTS);
		return new ArrayList<Channel>(channels);
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
		if (roles.contains("Admin"))
			return getChannels(null, true, true);

		// Check if we can display locked threads
		List<Channel> channels = getChannels(al, roles.contains("Moderator"), false);
		for (Iterator<Channel> i = channels.iterator(); i.hasNext();) {
			Channel c = i.next();
			if (!RoleUtils.hasAccess(roles, c.getReadRoles()))
				i.remove();
		}

		// return results
		return channels;
	}

	/*
	 * Helper method to load Channel roles and airlines.
	 */
	private void loadInfo(Map<String, Channel> channels) throws SQLException {
		try (PreparedStatement ps = prepareWithoutLimits("SELECT * FROM common.COOLER_CHANNELINFO")) {
			try (ResultSet rs = ps.executeQuery()) {
				while (rs.next()) {
					Channel c = channels.get(rs.getString(1));
					if (c != null) {
						Channel.InfoType inf = Channel.InfoType.values()[rs.getInt(2)];
						if (inf == Channel.InfoType.AIRLINE)
							c.addAirline(rs.getString(3));
						else
							c.addRole(inf, rs.getString(3));
					}
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

		// Init the prepared statement
		StringBuilder sqlBuf = new StringBuilder("SELECT T.* FROM common.COOLER_THREADS T WHERE (T.ID IN (");
		for (Iterator<Integer> i = idSet.iterator(); i.hasNext();) {
			Integer id = i.next();
			sqlBuf.append(id.toString());
			if (i.hasNext())
				sqlBuf.append(',');
		}

		// Close and prepare the statement
		List<Message> results = new ArrayList<Message>();
		sqlBuf.append("))");
		try (PreparedStatement ps = prepareWithoutLimits(sqlBuf.toString())) {
			try (ResultSet rs = ps.executeQuery()) {
				while (rs.next()) {
					LastPostMessage msg = new LastPostMessage(rs.getInt(13));
					msg.setThreadID(rs.getInt(1));
					msg.setID(msg.getThreadID());
					msg.setSubject(rs.getString(2));
					msg.setCreatedOn(rs.getTimestamp(12).toInstant());
					results.add(msg);
				}
			}
		} catch (SQLException se) {
			throw new DAOException(se);
		}

		return CollectionUtils.createMap(results, Message::getID);
	}
}