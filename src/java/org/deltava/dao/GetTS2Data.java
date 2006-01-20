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
	 * Returns all TeamSpeak 2 users authorized to access a virtual server.
	 * @param serverID the server database ID
	 * @return a Collection of User profiles
	 * @throws DAOException if a JDBC error occurs
	 */
	public Collection<User> getUsers(int serverID) throws DAOException {
		try {
			prepareStatement("SELECT * FROM teamspeak.ts2_clients WHERE (i_client_server_id=?) ORDER BY s_client_name");
			_ps.setInt(1, serverID);
			return executeUsers();
		} catch (SQLException se) {
			throw new DAOException(se);
		}
	}
	
	/**
	 * Returns all TeamSpeak 2 virtual server profiles.
	 * @return a Collection of Server beans
	 * @throws DAOException if a JDBC error occurs
	 */
	public Collection<Server> getServers() throws DAOException {
		try {
			prepareStatement("SELECT i_server_id, s_server_name, s_server_welcomemessage, i_server_maxusers, "
					+ "i_server_udpport, s_server_password, b_server_active, dt_server_created, s_server_description "
					+ "FROM teamspeak.ts2_servers");
			return executeServers();
		} catch (SQLException se) {
			throw new DAOException(se);
		}
	}
	
	/**
	 * Returns a TeamSpeak 2 virtual server profile.
	 * @param id the Server database ID
	 * @return a Server bean, or null if not found
	 * @throws DAOException if a JDBC error occurs
	 */
	public Server getServer(int id) throws DAOException {
		try {
			setQueryMax(1);
			prepareStatement("SELECT i_server_id, s_server_name, s_server_welcomemessage, i_server_maxusers, "
					+ "i_server_udpport, s_server_password, b_server_active, dt_server_created, s_server_description "
					+ "FROM teamspeak.ts2_servers WHERE (i_server_id=?)");
			_ps.setInt(1, id);
			List<Server> results = executeServers();
			return results.isEmpty() ? null : results.get(0);
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
			usr.setPassword(rs.getString(5));
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
	
	private List<Server> executeServers() throws SQLException {
		Map<Integer, Server> results = new TreeMap<Integer, Server>();
		
		// Execute the query
		ResultSet rs = _ps.executeQuery();
		while (rs.next()) {
			Server srv = new Server(rs.getString(2));
			srv.setID(rs.getInt(1));
			srv.setWelcomeMessage(rs.getString(3));
			srv.setMaxUsers(rs.getInt(4));
			srv.setPort(rs.getInt(5));
			srv.setPassword(rs.getString(6));
			srv.setActive(rs.getInt(7) == -1);
			srv.setCreatedOn(rs.getTimestamp(8));
			srv.setDescription(rs.getString(9));
			
			// Add to results
			results.put(new Integer(srv.getID()), srv);
		}
		
		// Clean up first query
		rs.close();
		_ps.close();
		
		// Load roles for each server
		prepareStatementWithoutLimits("SELECT * FROM ts2_server_roles");
		rs = _ps.executeQuery();
		while (rs.next()) {
			Server srv = results.get(new Integer(rs.getInt(1)));
			if (srv != null)
				srv.addRole(rs.getString(2));
		}
		
		// Clean up and return
		rs.close();
		_ps.close();
		return new ArrayList<Server>(results.values());
	}
}