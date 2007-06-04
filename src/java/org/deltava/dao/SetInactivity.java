// Copyright 2005, 2007 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.dao;

import java.sql.*;

/**
 * A Data Access Object to update the Inactivity Purge table.
 * @author Luke
 * @version 1.0
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
	 * @throws DAOException if a JDBC error occurs
	 */
	public void setInactivity(int pilotID, int days, boolean isNotified) throws DAOException {
		try {
			prepareStatement("REPLACE INTO INACTIVITY (ID, NOTIFY, PURGE_DATE, PURGE_DAYS) VALUES (?, ?, "
					+ "DATE_ADD(CURDATE(), INTERVAL ? DAY), ?)");
			_ps.setInt(1, pilotID);
			_ps.setBoolean(2, isNotified);
			_ps.setInt(3, days);
			_ps.setInt(4, days);
			executeUpdate(1);
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
		try {
			prepareStatement("DELETE FROM INACTIVITY WHERE (ID=?)");
			_ps.setInt(1, pilotID);
			executeUpdate(0);
		} catch (SQLException se) {
			throw new DAOException(se);
		}
	}
	
	/**
	 * Purge all inactivity entries with a purge date in the past.
	 * @throws DAOException if a JDBC error occurs
	 */
	public void purge() throws DAOException {
		try {
			prepareStatement("DELETE FROM INACTIVITY WHERE (PURGE_DATE < CURDATE())");
			executeUpdate(0);
		} catch (SQLException se) {
			throw new DAOException(se);
		}
	}
}