// Copyright (c) 2006 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.dao;

import java.sql.*;
import java.util.*;

import org.deltava.beans.ts2.*;

import org.deltava.util.cache.*;

/**
 * A Data Access Object to load TeamSpeak 2 configuration data.
 * @author Luke
 * @version 1.0
 * @since 1.0
 */

public class GetTS2Data extends DAO {
	
	private static final Cache _cache = new ExpiringCache(12, 600);

	/**
	 * Initializes the Data Access Object.
	 * @param c the JDBC connection to use
	 */
	public GetTS2Data(Connection c) {
		super(c);
	}

	/**
	 * Returns an individual TeamSpeak voice Channel.
	 * @param name the Channel name
	 * @return a Channel profile, or null if not found
	 * @throws DAOException if a JDBC error occurs
	 */
	public Channel getChannel(String name) throws DAOException {
		try {
			setQueryMax(1);
			prepareStatement("SELECT * FROM teamspeak.ts2_channels WHERE (s_channel_name=?)");
			_ps.setString(1, name);
			
			// Execute the query and return
			List<Channel> results = executeChannels();
			return results.isEmpty() ? null : results.get(0);
		} catch (SQLException se) {
			throw new DAOException(se);
		}
	}
	
	/**
	 * Returns all TeamSpeak voice Channels.
	 * @return a Collection of Channel profiles
	 * @throws DAOException if a JDBC error occurs
	 */
	public Collection<Channel> getChannels() throws DAOException {
		try {
			prepareStatement("SELECT * FROM teamspeak.ts2_channels ORDER BY s_channel_name");
			return executeChannels();
		} catch (SQLException se) {
			throw new DAOException(se);
		}
	}
	
	/**
	 * Returns an individual TeamSpeak 2 user profile.
	 * @param id the user ID
	 * @return the User profile, or null if not found
	 * @throws DAOException if a JDBC error occurs
	 */
	public User getUser(String id) throws DAOException {
		try {
			setQueryMax(1);
			prepareStatement("SELECT * FROM teamspeak.ts2_clients WHERE (s_client_name=?)");
			_ps.setString(1, id);
			
			// Execute the query and return
			List<User> results = executeUsers();
			return results.isEmpty() ? null : results.get(0);
		} catch (SQLException se) {
			throw new DAOException(se);
		}
	}
	
	/**
	 * Returns all TeamSpeak 2 user profiles.
	 * @return a Collection of User profiles
	 * @throws DAOException if a JDBC error occurs
	 */
	public Collection<User> getUsers() throws DAOException {
		try {
			prepareStatement("SELECT * FROM teamspeak.ts2_clients ORDER BY s_client_name");
			return executeUsers();
		} catch (SQLException se) {
			throw new DAOException(se);
		}
	}
	
	/**
	 * Helper method to parse Channel result sets.
	 */
	private List<Channel> executeChannels() throws SQLException {
		List<Channel> results = new ArrayList<Channel>();
		
		// Execute the query
		ResultSet rs = _ps.executeQuery();
		while (rs.next()) {
			Channel c = new Channel(rs.getString(10));
			c.setID(rs.getInt(1));
			c.setServerID(rs.getInt(2));
			c.setModerated(rs.getInt(4) == -1);
			c.setHierarchical(rs.getInt(5) == -1);
			c.setDefault(rs.getInt(6) == -1);
			c.setCodec(rs.getInt(7));
			c.setMaxUsers(rs.getInt(9));
			c.setPassword(rs.getString(11));
			c.setCreatedOn(rs.getTimestamp(12));
			
			// Add to cache and results
			_cache.add(c);
			results.add(c);
		}
		
		// Clean up and return
		rs.close();
		_ps.close();
		return results;
	}
	
	/**
	 * Helper method to parse User result sets.
	 */
	private List<User> executeUsers() throws SQLException {
		List<User> results = new ArrayList<User>();
		
		// Execute the query
		ResultSet rs = _ps.executeQuery();
		while (rs.next()) {
			User usr = new User(rs.getString(4));
			usr.setID(rs.getInt(1));
			usr.setServerID(rs.getInt(2));
			usr.setServerAdmin(rs.getInt(3) == -1);
			usr.setCreatedOn(rs.getTimestamp(6));
			usr.setLastOnline(rs.getTimestamp(7));
			
			// Add to cache and results
			_cache.add(usr);
			results.add(usr);
		}
		
		// Clean up and return
		rs.close();
		_ps.close();
		return results;
	}
}