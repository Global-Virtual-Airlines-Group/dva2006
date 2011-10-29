// Copyright 2010, 2011 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.dao;

import java.sql.*;
import java.util.*;

import org.deltava.beans.mvs.*;

import org.deltava.util.system.SystemData;

/**
 * A Data Access Object to load permanent voice channel data.
 * @author Luke
 * @version 4.1
 * @since 4.0
 */

public class GetMVSChannel extends DAO {
	
	/**
	 * Initializes the Data Access Object.
	 * @param c the JDBC connection to use
	 */
	public GetMVSChannel(Connection c) {
		super(c);
	}

	/**
	 * Loads a Channel profile.
	 * @param id the Channel id
	 * @return a Channel bean, or null if not found
	 * @throws DAOException if a JDBC error occurs
	 */
	public Channel get(int id) throws DAOException {
		try {
			prepareStatementWithoutLimits("SELECT * FROM acars.CHANNELS WHERE (ID=?) LIMIT 1");
			_ps.setInt(1, id);
			List<Channel> results = execute();
			if (results.isEmpty())
				return null;
			
			Channel c = results.get(0);
			loadRoles(c);
			loadAirlines(c);
			return c;
		} catch (SQLException se) {
			throw new DAOException(se);
		}
	}
	
	/**
	 * Loads a Channel profile.
	 * @param name the Channel name
	 * @return a Channel bean, or null if not found
	 * @throws DAOException if a JDBC error occurs
	 */
	public Channel get(String name) throws DAOException {
		try {
			prepareStatementWithoutLimits("SELECT * FROM acars.CHANNELS WHERE (NAME=?) LIMIT 1");
			_ps.setString(1, name);
			List<Channel> results = execute();
			if (results.isEmpty())
				return null;
			
			Channel c = results.get(0);
			loadRoles(c);
			loadAirlines(c);
			return c;
		} catch (SQLException se) {
			throw new DAOException(se);
		}
	}

	/**
	 * Loads all Channel profiles.
	 * @return a Collection of Channel beans
	 * @throws DAOException if a JDBC error occurs
	 */
	public Collection<Channel> getAll() throws DAOException {
		try {
			prepareStatementWithoutLimits("SELECT * FROM acars.CHANNELS ORDER BY NAME");
			List<Channel> results = execute();
			for (Channel c : results) {
				loadRoles(c);
				loadAirlines(c);
			}
			
			return results;
		} catch (SQLException se) {
			throw new DAOException(se);
		}
	}
	
	/**
	 * Helper method to parse Channel result sets.
	 */
	private List<Channel> execute() throws SQLException {
		List<Channel> results = new ArrayList<Channel>();
		try (ResultSet rs = _ps.executeQuery()) {
			while (rs.next()) {
				Channel c = new Channel(rs.getString(2));
				c.setID(rs.getInt(1));
				c.setDescription(rs.getString(3));
				c.setSampleRate(SampleRate.getRate(rs.getInt(4)));
				c.setRange(rs.getInt(5));
				results.add(c);
			}
		}

		_ps.close();
		return results;
	}
	
	/**
	 * Helper method to load channel airlines
	 */
	private void loadAirlines(Channel c) throws SQLException {
		prepareStatementWithoutLimits("SELECT CODE FROM acars.CHANNEL_AIRLINES WHERE (ID=?)");
		_ps.setInt(1, c.getID());
		try (ResultSet rs = _ps.executeQuery()) {
			while (rs.next())
				c.addAirline(SystemData.getApp(rs.getString(1)));
		}
		
		_ps.close();
	}
	
	/**
	 * Helper method to load channel access roles.
	 */
	private void loadRoles(Channel c) throws SQLException {
		prepareStatementWithoutLimits("SELECT TYPE, ROLE FROM acars.CHANNEL_ROLES WHERE (ID=?)");
		_ps.setInt(1, c.getID());
		try (ResultSet rs = _ps.executeQuery()) {
			while (rs.next()) {
				Channel.Access a = Channel.Access.values()[rs.getInt(1)];
				c.addRole(a, rs.getString(2));
			}
		}
		
		_ps.close();
	}
}