// Copyright 2005, 2006 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.dao;

import java.sql.*;

import org.deltava.beans.Staff;

/**
 * A Data Access Object to write Staff Profiles to the database.
 * @author Luke
 * @version 1.0
 * @since 1.0
 */

public class SetStaff extends DAO {

	/**
	 * Initializes the Data Access Object.
	 * @param c the JDBC connection to use
	 */
	public SetStaff(Connection c) {
		super(c);
	}

	/**
	 * Writes/Updates a Staff Profile to the database.
	 * @param s the Staff Profile
	 * @throws DAOException if a JDBC error occurs
	 */
	public void write(Staff s) throws DAOException {
		try {
			prepareStatementWithoutLimits("REPLACE INTO STAFF (ID, TITLE, SORT_ORDER, BIO, AREA) VALUES (?, ?, ?, ?, ?)");
			_ps.setInt(1, s.getID());
			_ps.setString(2, s.getTitle());
			_ps.setInt(3, s.getSortOrder());
			_ps.setString(4, s.getBody());
			_ps.setString(5, s.getArea());
			
			executeUpdate(1);
		} catch (SQLException se) {
			throw new DAOException(se);
		}
	}
	
	/**
	 * Removes a Staff Profile from the databse.
	 * @param id the Staff Profile ID
	 * @throws DAOException if a JDBC error occurs
	 */
	public void delete(int id) throws DAOException {
		try {
			prepareStatementWithoutLimits("DELETE FROM STAFF WHERE (ID=?)");
			_ps.setInt(1, id);
			executeUpdate(1);
		} catch (SQLException se) {
			throw new DAOException(se);
		}
	}
}