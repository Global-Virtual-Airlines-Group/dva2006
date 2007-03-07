// Copyright 2005, 2006, 2007 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.dao;

import java.util.*;
import java.sql.*;

import org.deltava.beans.cooler.*;
import org.deltava.beans.system.AirlineInformation;

import org.deltava.util.CollectionUtils;
import org.deltava.util.RoleUtils;

import org.deltava.util.cache.*;

/**
 * A Data Access Object to load Water Cooler channel profiles.
 * @author Luke
 * @version 1.0
 * @since 1.0
 */

public class GetCoolerChannels extends DAO implements CachingDAO {

	private static final Cache<Channel> _cache = new ExpiringCache<Channel>(5, 3600);

	/**
	 * Create this DAO using a JDBC connection.
	 * @param c the JDBC connection to use
	 */
	public GetCoolerChannels(Connection c) {
		super(c);
	}
	
	/**
	 * Returns the number of cache hits.
	 * @return the number of hits
	 */
	public int getRequests() {
		return _cache.getRequests();
	}
	
	/**
	 * Returns the number of cache requests.
	 * @return the number of requests
	 */
	public int getHits() {
		return _cache.getHits();
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
		// Check if we're in the cache
		if (_cache.contains(channelName))
			_cache.get(channelName);

		try {
			setQueryMax(1);
			prepareStatement("SELECT * FROM common.COOLER_CHANNELS WHERE (CHANNEL=?)");
			_ps.setString(1, channelName);

			// Execute the query - if nothing is returned, return null
			ResultSet rs = _ps.executeQuery();
			setQueryMax(0);
			if (!rs.next()) {
				_ps.close();
				rs.close();
				return null;
			}

			// Initialize the channel
			Channel c = new Channel(channelName);
			c.setDescription(rs.getString(2));
			c.setActive(rs.getBoolean(3));

			// Clean up the first set of JDBC resources
			rs.close();
			_ps.close();

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
	 * @param showHidden wether hidden threads should be returned in the last post
	 * @return a List of channels
	 * @throws DAOException if a JDBC error occurs
	 */
	public List<Channel> getChannels(AirlineInformation al, boolean showHidden) throws DAOException {

		// Build the SQL statement optionally showing locked threads
		StringBuilder sqlBuf = new StringBuilder("SELECT C.*, (SELECT T.ID FROM common.COOLER_THREADS T WHERE ");
		if (!showHidden)
			sqlBuf.append("(T.HIDDEN=?) AND ");
		sqlBuf.append("(T.CHANNEL=C.CHANNEL) ORDER BY T.LASTUPDATE DESC LIMIT 1) AS LT, SUM(T.POSTS), "
				+ "COUNT(DISTINCT T.ID), SUM(T.VIEWS) FROM common.COOLER_CHANNELS C LEFT JOIN common.COOLER_THREADS T "
				+ "ON (T.CHANNEL=C.CHANNEL) WHERE (C.ACTIVE=?) GROUP BY C.CHANNEL");

		Map<String, Channel> results = new TreeMap<String, Channel>();
		try {
			prepareStatement(sqlBuf.toString());
			if (showHidden) {
				_ps.setBoolean(1, true);
			} else {
				_ps.setBoolean(1, false);
				_ps.setBoolean(2, true);
			}

			// Execute the query - we store results in a map for now
			ResultSet rs = _ps.executeQuery();

			// Iterate through the results
			while (rs.next()) {
				Channel c = new Channel(rs.getString(1));
				c.setDescription(rs.getString(2));
				c.setActive(rs.getBoolean(3));
				c.setLastThreadID(rs.getInt(4));
				c.setPostCount(rs.getInt(5));
				c.setThreadCount(rs.getInt(6));
				c.setViewCount(rs.getInt(7));

				// Add to the results with the channel name as the key
				results.put(c.getName(), c);
			}

			// Clean up the first set of JDBC resources
			rs.close();
			_ps.close();

			// Load the roles and airlines
			loadInfo(results);
		} catch (SQLException se) {
			throw new DAOException(se);
		}

		// Parse through the results and strip out from the airline - since it's cheaper to do it here than in the query
		Collection<Channel> channels = new TreeSet<Channel>(results.values());
		for (Iterator<Channel> i = channels.iterator(); i.hasNext();) {
			Channel c = i.next();
			if (!c.hasAirline(al.getCode()))
				i.remove();
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
			return getChannels(al, true);

		// Check if we can display locked threads
		List<Channel> channels = getChannels(al, roles.contains("Moderator"));
		for (Iterator<Channel> i = channels.iterator(); i.hasNext();) {
			Channel c = i.next();
			if (!RoleUtils.hasAccess(roles, c.getRoles()))
				i.remove();
		}

		// return results
		return channels;
	}

	/**
	 * Helper method to load Channel roles and airlines.
	 */
	private void loadInfo(Map<String, Channel> channels) throws SQLException {

		// prepare SQL statement
		if (channels.size() == 1) {
			prepareStatementWithoutLimits("SELECT * FROM common.COOLER_CHANNELINFO WHERE (CHANNEL=?)");
			Channel c = channels.values().iterator().next();
			_ps.setString(1, c.getName());
		} else {
			prepareStatementWithoutLimits("SELECT * FROM common.COOLER_CHANNELINFO");
		}

		// Execute the query
		ResultSet rs = _ps.executeQuery();
		while (rs.next()) {
			Channel c = channels.get(rs.getString(1));
			if (c != null) {
				switch (rs.getInt(2)) {
					case Channel.INFOTYPE_ROLE:
						c.addRole(rs.getString(3));
						break;

					case Channel.INFOTYPE_AIRLINE:
						c.addAirline(rs.getString(3));
						break;
				}
			}
		}

		// Clean up after ourselves
		rs.close();
		_ps.close();
	}

	/**
	 * Returns a Map of the last posts in a group of Channels, indexed by Post ID.
	 * @param channels a Collection of channels to query
	 * @return a Map of Messages, keyed by database ID
	 * @throws DAOException if a JDBC error occurs
	 */
	public Map getLastPosts(Collection<Channel> channels) throws DAOException {

		// Build a set of post IDs
		Set<Integer> idSet = new HashSet<Integer>();
		for (Iterator<Channel> i = channels.iterator(); i.hasNext();) {
			Channel ch = i.next();
			if (ch.getLastThreadID() != 0)
				idSet.add(new Integer(ch.getLastThreadID()));
		}

		// If we have no post IDs, then return an empty map
		if (idSet.isEmpty())
			return Collections.EMPTY_MAP;

		// Init the prepared statement
		StringBuilder sqlBuf = new StringBuilder("SELECT T.* FROM common.COOLER_THREADS T WHERE (T.ID IN (");
		for (Iterator<Integer> i = idSet.iterator(); i.hasNext();) {
			Integer id = i.next();
			sqlBuf.append(id.toString());
			if (i.hasNext())
				sqlBuf.append(',');
		}

		// Strip out the trailing comma
		if (sqlBuf.charAt(sqlBuf.length() - 1) == ',')
			sqlBuf.setLength(sqlBuf.length() - 1);

		// Close and prepare the statement
		List<Message> results = new ArrayList<Message>();
		sqlBuf.append("))");
		try {
			prepareStatementWithoutLimits(sqlBuf.toString());

			// Execute the query and build the results list
			ResultSet rs = _ps.executeQuery();
			while (rs.next()) {
				LastPostMessage msg = new LastPostMessage(rs.getInt(13));
				msg.setThreadID(rs.getInt(1));
				msg.setID(msg.getThreadID());
				msg.setSubject(rs.getString(2));
				msg.setCreatedOn(rs.getTimestamp(12));

				// Add to results
				results.add(msg);
			}

			// Clean up
			rs.close();
			_ps.close();
		} catch (SQLException se) {
			throw new DAOException(se);
		}

		// Convert to map and return
		return CollectionUtils.createMap(results, "ID");
	}
}