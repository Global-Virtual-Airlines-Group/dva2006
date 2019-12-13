// Copyright 2005, 2006, 2010, 2016, 2017, 2019 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.dao;

import java.sql.*;
import java.util.*;

import org.deltava.beans.IMAddress;
import org.deltava.util.StringUtils;
import org.deltava.util.cache.CacheManager;
import org.deltava.util.system.SystemData;

/**
 * A Data Access Object to support writing Pilot objects to the database.
 * @author Luke
 * @version 9.0
 * @since 1.0
 */

public abstract class PilotWriteDAO extends DAO {

	/**
	 * Initializes the Data Access Object.
	 * @param c the JDBC connection to use
	 */
	protected PilotWriteDAO(Connection c) {
		super(c);
	}

	/**
	 * Writes a Pilot's security roles to the database.
	 * @param id the Pilot's database ID
	 * @param roles a Collection of security role names
	 * @param db the database to write to
	 * @throws SQLException if a JDBC error occurs
	 */
	protected void writeRoles(int id, Collection<String> roles, String db) throws SQLException {

		// Clear existing roles
		String dbName = formatDBName(db);
		try (PreparedStatement ps = prepareWithoutLimits("DELETE FROM " + dbName + ".ROLES WHERE (ID=?)")) {
			ps.setInt(1, id);
			executeUpdate(ps, 0);
		}

		// Write the roles to the database - don't add the "pilot" role
		try (PreparedStatement ps = prepareWithoutLimits("INSERT INTO " + dbName + ".ROLES (ID, ROLE) VALUES (?, ?)")) {
			ps.setInt(1, id);
			for (String role : roles) {
				if (!"Pilot".equals(role)) {
					ps.setString(2, role);
					ps.addBatch();
				}
			}
			
			executeUpdate(ps, 1, 0);
		}
	}

	/**
	 * Writes a Pilot's equipment type ratings to the database.
	 * @param id the Pilot's database ID
	 * @param ratings a Collection of aircraft types
	 * @param db the database to write to
	 * @param doClear TRUE if existing ratings should be cleared, otherwise FALSE
	 * @throws SQLException if a JDBC error occurs
	 */
	protected void writeRatings(int id, Collection<String> ratings, String db, boolean doClear) throws SQLException {

		// Clear existing ratings
		String dbName = formatDBName(db);
		if (doClear) {
			try (PreparedStatement ps = prepareWithoutLimits("DELETE FROM " + dbName + ".RATINGS WHERE (ID=?)")) {
				ps.setInt(1, id);
				executeUpdate(ps, 0);
			}
		}

		// Write the ratings to the database
		try (PreparedStatement ps = prepareWithoutLimits("REPLACE INTO " + dbName + ".RATINGS (ID, RATING) VALUES (?, ?)")) {
			ps.setInt(1, id);
			for (String r : ratings) {
				ps.setString(2, r);
				ps.addBatch();
			}

			executeUpdate(ps, 1, ratings.size());
		}
	}
	
	/**
	 * Writes a Pilot's social meda and instant messaging IDs to the database.
	 * @param id the Pilot's database ID
	 * @param addrs a Map of IM addresses, keyed by type 
	 * @param db the database to write to
	 * @param doClear TRUE if existing ratings should be cleared, otherwise FALSE
	 * @throws SQLException if a JDBC error occurs
	 */
	protected void writeIMAddrs(int id, Map<IMAddress, String> addrs, String db, boolean doClear) throws SQLException {
		
		// Clear existing ratings
		String dbName = formatDBName(db);
		if (doClear) {
			try (PreparedStatement ps = prepareWithoutLimits("DELETE FROM " + dbName + ".PILOT_IMADDR WHERE (ID=?)")) {
				ps.setInt(1, id);
				executeUpdate(ps, 0);
			}
		}
		
		// Write the IM addrs to the database
		try (PreparedStatement ps = prepareWithoutLimits("REPLACE INTO " + dbName + ".PILOT_IMADDR (ID, TYPE, ADDR) VALUES (?, ?, ?)")) {
			ps.setInt(1, id);
			for (Map.Entry<IMAddress, String> me : addrs.entrySet()) {
				ps.setString(2, me.getKey().toString());
				ps.setString(3, me.getValue());
				ps.addBatch();
			}
		
			executeUpdate(ps, 1, addrs.size());
		}
	}
	
	/**
	 * Writes a Pilot's alias to the AUTH_ALIAS table if present.
	 * @param id the Pilot's database ID
	 * @param aliases an array of aliases
	 * @throws SQLException if a JDBC error occurs
	 */
	protected void writeAlias(int id, String... aliases) throws SQLException {
		if (!SystemData.getBoolean("security.auth_alias")) return;
		
		try (PreparedStatement ps = prepareWithoutLimits("DELETE FROM common.AUTH_ALIAS WHERE (ID=?)")) {
			ps.setInt(1, id);
			executeUpdate(ps, 0);
		}
			
		try (PreparedStatement ps = prepareWithoutLimits("INSERT INTO common.AUTH_ALIAS (ID, USERID, ISCODE) VALUES (?, ?, ?)")) {
			ps.setInt(1, id);
			for (int x = 0; x < aliases.length; x++) {
				String uid = aliases[x];
				if (!StringUtils.isEmpty(uid)) {
					ps.setString(2, uid);
					ps.setBoolean(3, uid.startsWith(SystemData.get("airline.code")));
					ps.addBatch();
				}
			}
			
			executeUpdate(ps, 1, 0);
		}
	}
	
	/**
	 * Updates a Pilots' status in the database.
	 * @param id the pilot database ID
	 * @param status the pilot status
	 * @throws DAOException if a JDBC error occurs
	 */
	public void setStatus(int id, int status) throws DAOException {
		try (PreparedStatement ps = prepareWithoutLimits("UPDATE PILOTS SET STATUS=? WHERE (ID=?)")) {
			ps.setInt(1, status);
			ps.setInt(2, id);
			executeUpdate(ps, 1);
		} catch (SQLException se) {
			throw new DAOException(se);
		} finally {
			CacheManager.invalidate("Pilots", Integer.valueOf(id));
		}
	}
}