// Copyright 2010, 2011 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.dao;

import java.sql.*;
import java.util.*;

import org.deltava.beans.mvs.*;
import org.deltava.beans.schedule.GeoPosition;

/**
 * A Data Access Object to load permanent voice channel data.
 * @author Luke
 * @version 4.0
 * @since 4.0
 */

public class GetMVSChannel extends DAO {
	
	private static final int JOIN_ROLE = 0;
	private static final int TALK_ROLE = 1;
	private static final int ADMIN_ROLE = 2;

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
			prepareStatementWithoutLimits("SELECT * FROM CHANNELS WHERE (ID=?) LIMIT 1");
			_ps.setInt(1, id);
			List<Channel> results = execute();
			if (results.isEmpty())
				return null;
			
			Channel c = results.get(0);
			loadRoles(c);
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
			prepareStatementWithoutLimits("SELECT * FROM CHANNELS WHERE (NAME=?) LIMIT 1");
			_ps.setString(1, name);
			List<Channel> results = execute();
			if (results.isEmpty())
				return null;
			
			Channel c = results.get(0);
			loadRoles(c);
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
			prepareStatementWithoutLimits("SELECT * FROM CHANNELS ORDER BY NAME");
			List<Channel> results = execute();
			for (Channel c : results)
				loadRoles(c);
			
			return results;
		} catch (SQLException se) {
			throw new DAOException(se);
		}
	}
	
	/**
	 * Helper method to parse Channel result sets.
	 */
	private List<Channel> execute() throws SQLException {
		ResultSet rs = _ps.executeQuery();
		List<Channel> results = new ArrayList<Channel>();
		while (rs.next()) {
			Channel c = new Channel(rs.getString(2));
			c.setID(rs.getInt(1));
			c.setSampleRate(SampleRate.getRate(rs.getInt(3)));
			double lat = rs.getDouble(4);
			double lng = rs.getDouble(5);
			if ((lat != 0.0d) || (lng != 0.0d)) {
				c.setCenter(new GeoPosition(lat, lng));
				c.setRange(rs.getInt(6));
			}
			
			results.add(c);
		}

		rs.close();
		_ps.close();
		return results;
	}
	
	/**
	 * Helper method to load channel access roles.
	 */
	private void loadRoles(Channel c) throws SQLException {
		prepareStatementWithoutLimits("SELECT ROLE, TYPE FROM CHANNEL_ROLES WHERE (ID=?)");
		_ps.setInt(1, c.getID());
		ResultSet rs = _ps.executeQuery();
		while (rs.next()) {
			int roleType = rs.getInt(1);
			String roleName = rs.getString(2);
			switch (roleType) {
				case ADMIN_ROLE:
					c.addAdminRole(roleName);
					break;
				case TALK_ROLE:
					c.addTalkRole(roleName);
					break;
				case JOIN_ROLE:
					c.addViewRole(roleName);
					break;
			}
		}
		
		rs.close();
		_ps.close();
	}
}