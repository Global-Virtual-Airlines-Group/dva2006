// Copyright 2005, 2007, 2019, 2025 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.dao;

import java.sql.*;

/**
 * A Data Access Object to update the Inactivity Purge table.
 * @author Luke
 * @version 11.6
 * @since 1.0
 */

public class SetInactivity extends DAO {

	/**
	 * Initializes the Data Access Object.
	 * @param c the JDBC connection to use
	 */
	public SetInactivity(Connection c) {
		super(c);
	}

	/**
	 * Sets the inactivity purge entry for a Pilot.
	 * @param pilotID the Pilot's database ID
	 * @param isNotified TRUE if the warning message has been sent, otherwise FALSE
	 * @param days the number of days from today to purge
	 * @param dbName the database name
	 * @throws DAOException if a JDBC error occurs
	 */
	public void setInactivity(int pilotID, int days, boolean isNotified, String dbName) throws DAOException {
		
		// Build the SQL statement
		StringBuilder buf = new StringBuilder("REPLACE INTO ");
		buf.append(formatDBName(dbName));
		buf.append(".INACTIVITY (ID, NOTIFY, PURGE_DATE, PURGE_DAYS) VALUES (?, ?, DATE_ADD(CURDATE(), INTERVAL ? DAY), ?)");
		
		try (PreparedStatement ps = prepare(buf.toString())) {
			ps.setInt(1, pilotID);
			ps.setBoolean(2, isNotified);
			ps.setInt(3, days);
			ps.setInt(4, days);
			executeUpdate(ps, 1);
		} catch (SQLException se) {
			throw new DAOException(se);
		}
	}
	
	/**
	 * Removes a Pilot's inactivity purge entry.
	 * @param pilotID the Pilot's database ID
	 * @throws DAOException if a JDBC error occurs
	 */
	public void delete(int pilotID) throws DAOException {
		try (PreparedStatement ps = prepare("DELETE FROM INACTIVITY WHERE (ID=?)")) {
			ps.setInt(1, pilotID);
			executeUpdate(ps, 0);
		} catch (SQLException se) {
			throw new DAOException(se);
		}
	}
	
	/**
	 * Purge all inactivity entries with a purge date in the past.
	 * @throws DAOException if a JDBC error occurs
	 */
	public void purge() throws DAOException {
		try (PreparedStatement ps = prepareWithoutLimits("DELETE FROM INACTIVITY WHERE (PURGE_DATE < CURDATE())")) {
			executeUpdate(ps, 0);
		} catch (SQLException se) {
			throw new DAOException(se);
		}
	}
}