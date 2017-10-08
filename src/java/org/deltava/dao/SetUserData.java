// Copyright 2005, 2007, 2012, 2015, 2017 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.dao;

import java.sql.*;
import java.util.*;

import org.deltava.beans.UserData;
import org.deltava.beans.system.AirlineInformation;
import org.deltava.util.cache.CacheManager;

/**
 * A Data Access Object to write cross-applicaton User data.
 * @author Luke
 * @version 8.0
 * @since 1.0
 */

public class SetUserData extends DAO {

	/**
	 * Initialize the Data Access Object.
	 * @param c the JDBC connection to use
	 */
	public SetUserData(Connection c) {
		super(c);
	}

	/**
	 * Writes a new UserData entry to the database.
	 * @param usr the UserData object
	 * @throws DAOException if a JDBC error occurs
	 */
	public void write(UserData usr) throws DAOException {
		try {
			startTransaction();
			prepareStatement("INSERT INTO common.USERDATA (ID, AIRLINE, TABLENAME) VALUES (?, ?, ?)");
			_ps.setInt(1, usr.getID());
			_ps.setString(2, usr.getAirlineCode());
			_ps.setString(3, usr.getTable());
			executeUpdate(1);
			usr.setID(getNewID());
			
			// Write the child rows if present
			prepareStatement("REPLACE INTO common.XDB_IDS (ID, OTHER_ID) VALUES(?, ?)");
			for (Iterator<Integer> i = usr.getIDs().iterator(); i.hasNext(); ) {
				int id = i.next().intValue();
				if (id != usr.getID()) {
					CacheManager.invalidate("UserData", Integer.valueOf(id));
					_ps.setInt(1, usr.getID());
					_ps.setInt(2, id);
					_ps.addBatch();
					
					// Create the reverse mapping
					_ps.setInt(1, id);
					_ps.setInt(2, usr.getID());
					_ps.addBatch();
				}
			}
			
			// Write child rows and commit
			executeBatchUpdate(1, 0);
			commitTransaction();
		} catch (SQLException se) {
			rollbackTransaction();
			throw new DAOException(se);
		}
	}
	
	/**
	 * Removes a User Data entry from the database.
	 * @param id the user's database ID
	 * @throws DAOException if a JDBC error occurs
	 */
	public void delete(int id) throws DAOException {
		try {
			prepareStatement("DELETE FROM common.USERDATA WHERE (ID=?)");
			_ps.setInt(1, id);
			executeUpdate(1);
		} catch (SQLException se) {
			throw new DAOException(se);
		} finally {
			CacheManager.invalidate("UserData", Integer.valueOf(id));
		}
	}

	/**
	 * Updates Airline Information in the database. <i>This cannot update the airline code or domain name.</i>
	 * @param info the Airline Information bean
	 * @throws DAOException if a JDBC error occurs
	 */
	public void update(AirlineInformation info) throws DAOException {
		try {
			prepareStatement("UPDATE common.AIRLINEINFO SET NAME=?, DOMAIN=?, CAN_TX=?, HIST_RESTRICT=?, HAS_SSL=? WHERE (CODE=?)");
			_ps.setString(1, info.getName());
			_ps.setString(2, info.getDomain());
			_ps.setBoolean(3, info.getCanTransfer());
			_ps.setBoolean(4, info.getHistoricRestricted());
			_ps.setBoolean(5, info.getSSL());
			_ps.setString(6, info.getCode());
			executeUpdate(1);
		} catch (SQLException se) {
			throw new DAOException(se);
		} finally {
			CacheManager.invalidate("AirlineInfo", info.cacheKey());
		}
	}
}