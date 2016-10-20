// Copyright 2005, 2006, 2010, 2016 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.dao;

import java.sql.*;
import java.util.*;

import org.deltava.beans.IMAddress;

import org.deltava.util.cache.CacheManager;
import org.deltava.util.system.SystemData;

/**
 * A Data Access Object to support writing Pilot object(s) to the database.
 * @author Luke
 * @version 7.2
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
		prepareStatementWithoutLimits("DELETE FROM " + db.toLowerCase() + ".ROLES WHERE (ID=?)");
		_ps.setInt(1, id);
		_ps.executeUpdate();
		_ps.close();

		// Write the roles to the database - don't add the "pilot" role
		prepareStatementWithoutLimits("INSERT INTO " + db.toLowerCase() + ".ROLES (ID, ROLE) VALUES (?, ?)");
		_ps.setInt(1, id);
		for (Iterator<String> i = roles.iterator(); i.hasNext();) {
			String role = i.next();
			if (!"Pilot".equals(role)) {
				_ps.setString(2, role);
				_ps.addBatch();
			}
		}

		// Execute the batch update
		_ps.executeBatch();
		_ps.close();
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
			prepareStatementWithoutLimits("DELETE FROM " + dbName + ".RATINGS WHERE (ID=?)");
			_ps.setInt(1, id);
			_ps.executeUpdate();
			_ps.close();
		}

		// Write the ratings to the database
		prepareStatementWithoutLimits("REPLACE INTO " + dbName + ".RATINGS (ID, RATING) VALUES (?, ?)");
		_ps.setInt(1, id);
		for (Iterator<String> i = ratings.iterator(); i.hasNext();) {
			_ps.setString(2, i.next());
			_ps.addBatch();
		}

		// Execute the batch update
		_ps.executeBatch();
		_ps.close();
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
			prepareStatementWithoutLimits("DELETE FROM " + dbName + ".PILOT_IMADDR WHERE (ID=?)");
			_ps.setInt(1, id);
			_ps.executeUpdate();
			_ps.close();
		}
		
		// Write the IM addrs to the database
		prepareStatementWithoutLimits("REPLACE INTO " + dbName + ".PILOT_IMADDR (ID, TYPE, ADDR) VALUES (?, ?, ?)");
		_ps.setInt(1, id);
		for (Iterator<Map.Entry<IMAddress, String>> i = addrs.entrySet().iterator(); i.hasNext();) {
			Map.Entry<IMAddress, String> me = i.next();
			_ps.setString(2, me.getKey().toString());
			_ps.setString(3, me.getValue());
			_ps.addBatch();
		}
		
		// Execute the batch update
		_ps.executeBatch();
		_ps.close();
	}
	
	/**
	 * Writes a Pilot's alias to the AUTH_ALIAS table if present.
	 * @param id the Pilot's database ID
	 * @param uid the alias
	 * @throws SQLException if a JDBC error occurs
	 */
	protected void writeAlias(int id, String uid) throws SQLException {
		if (!SystemData.getBoolean("security.auth_alias"))
			return;
		
		// Write the alias
		prepareStatementWithoutLimits((uid == null) ? "DELETE FROM common.AUTH_ALIAS WHERE (ID=?)" :
			"REPLACE INTO common.AUTH_ALIAS (ID, USERID) VALUES (?, ?)");
		_ps.setInt(1, id);
		if (uid != null)
			_ps.setString(2, uid);
		
		executeUpdate(0);
		CacheManager.invalidate("Pilots", Integer.valueOf(id));
	}
	
	/**
	 * Updates a Pilots' status in the database.
	 * @param id the pilot database ID
	 * @param status the pilot status
	 * @throws DAOException if a JDBC error occurs
	 */
	public void setStatus(int id, int status) throws DAOException {
		try {
			prepareStatementWithoutLimits("UPDATE PILOTS SET STATUS=? WHERE (ID=?)");
			_ps.setInt(1, status);
			_ps.setInt(2, id);
			executeUpdate(1);
		} catch (SQLException se) {
			throw new DAOException(se);
		} finally {
			CacheManager.invalidate("Pilots", Integer.valueOf(id));
		}
	}
}